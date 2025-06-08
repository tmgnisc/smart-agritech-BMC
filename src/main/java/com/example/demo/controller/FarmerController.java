package com.example.demo.controller;

import com.example.demo.model.Feedback;
import com.example.demo.model.Profile;
import com.example.demo.model.Role;
import com.example.demo.model.SensorData;
import com.example.demo.model.User;
import com.example.demo.model.Device;
import com.example.demo.model.Notification;
import com.example.demo.repository.FeedbackRepository;
import com.example.demo.repository.ProfileRepository;
import com.example.demo.repository.SensorDataRepository;
import com.example.demo.repository.DeviceRepository;
import com.example.demo.repository.NotificationRepository;
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

    @Autowired
    private DeviceRepository deviceRepository;
    
    @Autowired
    private NotificationRepository notificationRepository;

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
        User user = (User) session.getAttribute("user");
        Profile profile = profileRepository.findById(user.getEmail()).orElse(new Profile());
        List<Notification> notifications = notificationRepository.findByEmailOrderByTimestampDesc(user.getEmail());

        model.addAttribute("profile", profile);
        model.addAttribute("notifications", notifications);
        addSidebarAttributes(model, "notifications");
        return "farmer/notifications.html";
    }

    @GetMapping("/farmer-device-status")
    public String farmerDeviceStatus(Model model, HttpSession session) {
        if (!isFarmer(session)) {
            return "redirect:/login";
        }
        User user = (User) session.getAttribute("user");
        Profile profile = profileRepository.findById(user.getEmail()).orElse(new Profile());
        List<Device> devices = deviceRepository.findByEmail(user.getEmail());

        // Update device status based on SensorData for all sensorIds
        LocalDateTime now = LocalDateTime.now();
        for (Device device : devices) {
            boolean allActive = true;
            boolean anyMaintenance = false;
            for (String sensorId : device.getSensorIds()) {
                List<SensorData> sensorDataList = sensorDataRepository.findBySensorIdOrderByTimestampDesc(sensorId);
                if (sensorDataList.isEmpty()) {
                    anyMaintenance = true;
                    allActive = false;
                } else {
                    SensorData latestData = sensorDataList.get(0);
                    if (latestData.getTimestamp().isBefore(now.minusHours(24))) {
                        anyMaintenance = true;
                        allActive = false;
                    } else if (latestData.getTimestamp().isBefore(now.minusHours(1))) {
                        allActive = false;
                    }
                }
            }
            if (anyMaintenance) {
                device.setStatus(Device.DeviceStatus.MAINTENANCE);
            } else if (!allActive) {
                device.setStatus(Device.DeviceStatus.INACTIVE);
            } else {
                device.setStatus(Device.DeviceStatus.ACTIVE);
            }
            deviceRepository.save(device);
        }

        long totalDevices = devices.size();
        long activeDevices = devices.stream().filter(d -> d.getStatus() == Device.DeviceStatus.ACTIVE).count();
        long maintenanceRequired = devices.stream().filter(d -> d.getStatus() == Device.DeviceStatus.MAINTENANCE).count();

        model.addAttribute("profile", profile);
        model.addAttribute("devices", devices);
        model.addAttribute("totalDevices", totalDevices);
        model.addAttribute("activeDevices", activeDevices);
        model.addAttribute("maintenanceRequired", maintenanceRequired);
        addSidebarAttributes(model, "deviceStatus");
        return "farmer/deviceStatus.html";
    }
}