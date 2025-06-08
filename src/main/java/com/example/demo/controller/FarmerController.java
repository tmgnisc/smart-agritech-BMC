package com.example.demo.controller;

import com.example.demo.model.*;
import com.example.demo.repository.*;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

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
    public String updateFarmerDetails(@Valid @ModelAttribute Profile profile, BindingResult bindingResult,
                                      @RequestParam(value = "image", required = false) MultipartFile image,
                                      @RequestParam(value = "mainVegetables", required = false) String mainVegetables,
                                      Model model, HttpSession session) {
        if (!isFarmer(session)) {
            return "redirect:/login";
        }
        User user = (User) session.getAttribute("user");

        if (bindingResult.hasErrors()) {
            model.addAttribute("profile", profile);
            addSidebarAttributes(model, "farmerDetails");
            return "farmer/farmerDetails.html";
        }

        profile.setEmail(user.getEmail());

        // Parse mainVegetables from comma-separated string
        if (mainVegetables != null && !mainVegetables.trim().isEmpty()) {
            List<String> vegetables = Arrays.stream(mainVegetables.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList());
            profile.setMainVegetables(vegetables);
        } else {
            profile.setMainVegetables(new ArrayList<>());
        }

        // Handle image upload
        if (image != null && !image.isEmpty()) {
            try {
                String contentType = image.getContentType();
                if (contentType == null || !contentType.startsWith("image/")) {
                    model.addAttribute("error", "Only image files (PNG, JPG, GIF) are allowed");
                    model.addAttribute("profile", profile);
                    addSidebarAttributes(model, "farmerDetails");
                    return "farmer/farmerDetails.html";
                }

                String originalFilename = image.getOriginalFilename();
                String extension = originalFilename != null ? originalFilename.substring(originalFilename.lastIndexOf(".")) : ".jpg";
                String uniqueFilename = UUID.randomUUID().toString() + extension;

                Path uploadDir = Paths.get("src/main/resources/static/assets/profile");
                Files.createDirectories(uploadDir);
                Path filePath = uploadDir.resolve(uniqueFilename);
                Files.write(filePath, image.getBytes());

                String imageUrl = "/assets/profile/" + uniqueFilename;
                profile.setImageUrl(imageUrl);
                System.out.println("Uploaded profile image: " + imageUrl);
            } catch (IOException e) {
                model.addAttribute("error", "Failed to upload image: " + e.getMessage());
                model.addAttribute("profile", profile);
                addSidebarAttributes(model, "farmerDetails");
                return "farmer/farmerDetails.html";
            }
        }

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
        User user = (User) session.getAttribute("user");
        Profile profile = profileRepository.findById(user.getEmail()).orElse(new Profile());
        model.addAttribute("profile", profile);
        model.addAttribute("feedback", new Feedback());
        addSidebarAttributes(model, "feedback");
        return "farmer/feedback.html";
    }

    @PostMapping("/farmer-feedback")
    public String submitFeedback(@Valid @ModelAttribute Feedback feedback, BindingResult bindingResult,
                                @RequestParam("file") MultipartFile file, Model model, HttpSession session) {
        if (!isFarmer(session)) {
            return "redirect:/login";
        }
        User user = (User) session.getAttribute("user");
        Profile profile = profileRepository.findById(user.getEmail()).orElse(new Profile());

        if (bindingResult.hasErrors()) {
            model.addAttribute("profile", profile);
            model.addAttribute("feedback", feedback);
            addSidebarAttributes(model, "feedback");
            return "farmer/feedback.html";
        }

        feedback.setEmail(user.getEmail());
        feedback.setTimestamp(LocalDateTime.now());

        if (!file.isEmpty()) {
            try {
                String contentType = file.getContentType();
                if (contentType == null || !contentType.startsWith("image/")) {
                    model.addAttribute("error", "Only image files (PNG, JPG, GIF) are allowed");
                    model.addAttribute("feedback", feedback);
                    model.addAttribute("profile", profile);
                    addSidebarAttributes(model, "feedback");
                    return "farmer/feedback.html";
                }

                String originalFilename = file.getOriginalFilename();
                String extension = originalFilename != null ? originalFilename.substring(originalFilename.lastIndexOf(".")) : ".jpg";
                String uniqueFilename = UUID.randomUUID().toString() + extension;

                Path uploadDir = Paths.get("src/main/resources/static/uploads/feedback");
                Files.createDirectories(uploadDir);
                Path filePath = uploadDir.resolve(uniqueFilename);
                Files.write(filePath, file.getBytes());

                String imagePath = "/uploads/feedback/" + uniqueFilename;
                feedback.setImagePath(imagePath);
                System.out.println("Uploaded image path: " + imagePath);
            } catch (IOException e) {
                model.addAttribute("error", "Failed to upload image: " + e.getMessage());
                model.addAttribute("feedback", feedback);
                model.addAttribute("profile", profile);
                addSidebarAttributes(model, "feedback");
                return "farmer/feedback.html";
            }
        }

        feedbackRepository.save(feedback);
        model.addAttribute("success", "Feedback submitted successfully");
        model.addAttribute("feedback", new Feedback());
        model.addAttribute("profile", profile);
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

        List<Notification> municipalityNotifications = notifications.stream()
                .filter(n -> n.getType() == Notification.NotificationType.MUNICIPALITY)
                .collect(Collectors.toList());
        List<Notification> sensorNotifications = notifications.stream()
                .filter(n -> n.getType() == Notification.NotificationType.SENSOR)
                .collect(Collectors.toList());

        model.addAttribute("profile", profile);
        model.addAttribute("municipalityNotifications", municipalityNotifications);
        model.addAttribute("sensorNotifications", sensorNotifications);
        addSidebarAttributes(model, "notifications");
        return "farmer/notifications.html";
    }

    @PostMapping("/farmer-notifications/{id}/mark-read")
    public String markNotificationRead(@PathVariable Long id, HttpSession session) {
        if (!isFarmer(session)) {
            return "redirect:/login";
        }
        User user = (User) session.getAttribute("user");
        Notification notification = notificationRepository.findById(id).orElse(null);
        if (notification != null && notification.getEmail().equals(user.getEmail())) {
            notification.setRead(true);
            notificationRepository.save(notification);
        }
        return "redirect:/farmer-notifications";
    }

    @GetMapping("/farmer-device-status")
    public String farmerDeviceStatus(Model model, HttpSession session) {
        if (!isFarmer(session)) {
            return "redirect:/login";
        }
        User user = (User) session.getAttribute("user");
        Profile profile = profileRepository.findById(user.getEmail()).orElse(new Profile());
        List<Device> devices = deviceRepository.findByEmail(user.getEmail());

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