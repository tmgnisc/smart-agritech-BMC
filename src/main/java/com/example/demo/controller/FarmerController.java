package com.example.demo.controller;

import com.example.demo.model.Feedback;
import com.example.demo.model.Profile;
import com.example.demo.model.Role;
import com.example.demo.model.SensorData;
import com.example.demo.model.User;
import com.example.demo.repository.FeedbackRepository;
import com.example.demo.repository.ProfileRepository;
import com.example.demo.repository.SensorDataRepository;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;

@Controller
public class FarmerController {

    @Autowired
    private SensorDataRepository sensorDataRepository;

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private FeedbackRepository feedbackRepository;

    private boolean isFarmer(HttpSession session) {
        User user = (User) session.getAttribute("user");
        return user != null && user.getRole() == Role.FARMER;
    }

    private void addSidebarAttributes(Model model, String activePage) {
        model.addAttribute("activePage", activePage);
    }

    @GetMapping("/farmer-dashboard")
    public String farmerDashboard(Model model, HttpSession session) {
        if (!isFarmer(session)) {
            return "redirect:/login";
        }
        User user = (User) session.getAttribute("user");
        Profile profile = profileRepository.findById(user.getEmail()).orElse(new Profile());
        model.addAttribute("profile", profile);

        // Fetch latest sensor data (placeholder: latest DHT and MQ135 data)
        List<SensorData> dhtData = sensorDataRepository.findBySensorType("DHT");
        List<SensorData> mq135Data = sensorDataRepository.findBySensorType("MQ135");
        SensorData latestDht = dhtData.isEmpty() ? null : dhtData.get(dhtData.size() - 1);
        SensorData latestMq135 = mq135Data.isEmpty() ? null : mq135Data.get(mq135Data.size() - 1);

        model.addAttribute("temperature", latestDht != null ? latestDht.getTemperature() : 28.0);
        model.addAttribute("humidity", latestDht != null ? latestDht.getHumidity() : 65.0);
        model.addAttribute("airQuality", latestMq135 != null ? (latestMq135.getCo2() < 400 ? "Good" : "Poor") : "Good");

        addSidebarAttributes(model, "dashboard");
        return "farmer/farmer-dashboard.html";
    }

    @GetMapping("/farmer-details")
    public String farmerDetails(Model model, HttpSession session) {
        if (!isFarmer(session)) {
            return "redirect:/login";
        }
        User user = (User) session.getAttribute("user");
        Profile profile = profileRepository.findById(user.getEmail()).orElse(new Profile());
        model.addAttribute("profile", profile);
        addSidebarAttributes(model, "farmerDetails");
        return "farmer/farmerDetails.html";
    }

    @PostMapping("/farmer-details")
    public String updateFarmerDetails(@ModelAttribute Profile profile, Model model, HttpSession session) {
        if (!isFarmer(session)) {
            return "redirect:/login";
        }
        User user = (User) session.getAttribute("user");
        profile.setEmail(user.getEmail());
        profileRepository.save(profile);
        model.addAttribute("success", "Profile updated successfully");
        model.addAttribute("profile", profile);
        addSidebarAttributes(model, "farmerDetails");
        return "farmer/farmerDetails.html";
    }

    @GetMapping("/farmer-feedback")
    public String farmerFeedback(Model model, HttpSession session) {
        if (!isFarmer(session)) {
            return "redirect:/login";
        }
        model.addAttribute("feedback", new Feedback());
        addSidebarAttributes(model, "feedback");
        return "farmer/feedback.html";
    }

    @PostMapping("/farmer-feedback")
    public String submitFeedback(@Valid @ModelAttribute Feedback feedback, @ModelAttribute("file") MultipartFile file, Model model, HttpSession session) {
        if (!isFarmer(session)) {
            return "redirect:/login";
        }
        User user = (User) session.getAttribute("user");
        feedback.setEmail(user.getEmail());
        feedback.setTimestamp(LocalDateTime.now());
        if (!file.isEmpty()) {
            try {
                feedback.setImageBase64(Base64.getEncoder().encodeToString(file.getBytes()));
            } catch (IOException e) {
                model.addAttribute("error", "Failed to upload image");
                model.addAttribute("feedback", feedback);
                addSidebarAttributes(model, "feedback");
                return "farmer/feedback.html";
            }
        }
        feedbackRepository.save(feedback);
        model.addAttribute("success", "Feedback submitted successfully");
        model.addAttribute("feedback", new Feedback());
        addSidebarAttributes(model, "feedback");
        return "farmer/feedback.html";
    }

    @GetMapping("/farmer-notifications")
    public String farmerNotifications(Model model, HttpSession session) {
        if (!isFarmer(session)) {
            return "redirect:/login";
        }
        addSidebarAttributes(model, "notifications");
        return "farmer/notifications.html";
    }
}