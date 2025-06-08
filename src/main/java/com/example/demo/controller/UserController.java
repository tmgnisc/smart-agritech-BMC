package com.example.demo.controller;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.ui.Model;
import jakarta.servlet.http.HttpSession;

import com.example.demo.model.User;
import com.example.demo.model.Profile;
import com.example.demo.model.Role;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.ProfileRepository;

import java.util.List;

@Controller
public class UserController {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ProfileRepository profileRepository;
    
    @GetMapping("/login")
    public String showLoginForm(Model model) {
        model.addAttribute("user", new User());
        return "login.html";
    }
    
    @PostMapping("/login")
    public String loginUser(@ModelAttribute User user, Model model, HttpSession session) {
        List<User> users = userRepository.findByEmail(user.getEmail());
        if (!users.isEmpty() && users.get(0).getPassword().equals(DigestUtils.sha256Hex(user.getEmail() + user.getPassword()))) {
            session.setAttribute("user", users.get(0));
            Role role = users.get(0).getRole();
            if (role == Role.ADMIN) {
                return "redirect:/admin-dashboard";
            } else if (role == Role.MUNICIPALITY) {
                return "redirect:/municipality-dashboard";
            } else if (role == Role.FARMER) {
                return "redirect:/farmer-dashboard";
            }
        }
        model.addAttribute("error", "Invalid email or password");
        return "login.html";
    }
    
    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new User());
        model.addAttribute("profile", new Profile());
        model.addAttribute("roles", Role.values());
        return "register.html";
    }
    
    @PostMapping("/register")
    public String registerUser(@ModelAttribute User user, @ModelAttribute Profile profile, Model model) {
        if (userRepository.findByEmail(user.getEmail()).isEmpty()) {
            user.setPassword(DigestUtils.sha256Hex(user.getEmail() + user.getPassword()));
            profile.setEmail(user.getEmail());
            userRepository.save(user);
            profileRepository.save(profile);
            return "redirect:/login";
        } else {
            model.addAttribute("error", "Email already registered");
            return "register.html";
        }
    }
    
    @GetMapping("/admin-dashboard")
    public String adminDashboard(Model model, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null || user.getRole() != Role.ADMIN) {
            return "redirect:/login";
        }
        model.addAttribute("user", new User());
        model.addAttribute("profile", new Profile());
        return "admin-dashboard.html";
    }
    
    @PostMapping("/admin/add-municipality")
    public String addMunicipality(@ModelAttribute User user, @ModelAttribute Profile profile, Model model, HttpSession session) {
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null || currentUser.getRole() != Role.ADMIN) {
            return "redirect:/login";
        }
        if (userRepository.findByEmail(user.getEmail()).isEmpty()) {
            user.setRole(Role.MUNICIPALITY);
            user.setPassword(DigestUtils.sha256Hex(user.getEmail() + user.getPassword()));
            profile.setEmail(user.getEmail());
            userRepository.save(user);
            profileRepository.save(profile);
            model.addAttribute("success", "Municipality added successfully");
        } else {
            model.addAttribute("error", "Email already registered");
        }
        return "admin-dashboard.html";
    }
    
    @GetMapping("/municipality-dashboard")
    public String municipalityDashboard(Model model, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null || user.getRole() != Role.MUNICIPALITY) {
            return "redirect:/login";
        }
        model.addAttribute("user", new User());
        model.addAttribute("profile", new Profile());
        return "municipality-dashboard.html";
    }
    
    @PostMapping("/municipality/add-farmer")
    public String addFarmer(@ModelAttribute User user, @ModelAttribute Profile profile, Model model, HttpSession session) {
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null || currentUser.getRole() != Role.MUNICIPALITY) {
            return "redirect:/login";
        }
        if (userRepository.findByEmail(user.getEmail()).isEmpty()) {
            user.setRole(Role.FARMER);
            user.setPassword(DigestUtils.sha256Hex(user.getEmail() + user.getPassword()));
            profile.setEmail(user.getEmail());
            userRepository.save(user);
            profileRepository.save(profile);
            model.addAttribute("success", "Farmer added successfully");
        } else {
            model.addAttribute("error", "Email already registered");
        }
        return "municipality-dashboard.html";
    }
    
    @GetMapping("/farmer-dashboard")
    public String farmerDashboard(Model model, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null || user.getRole() != Role.FARMER) {
            return "redirect:/login";
        }
        Profile profile = profileRepository.findById(user.getEmail()).orElse(new Profile());
        model.addAttribute("profile", profile);
        return "farmer-dashboard.html";
    }
    
    @PostMapping("/farmer/update-profile")
    public String updateFarmerProfile(@ModelAttribute Profile profile, Model model, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null || user.getRole() != Role.FARMER) {
            return "redirect:/login";
        }
        profile.setEmail(user.getEmail());
        profileRepository.save(profile);
        model.addAttribute("success", "Profile updated successfully");
        model.addAttribute("profile", profile);
        return "farmer-dashboard.html";
    }
    
    @DeleteMapping("/user/{id}")
    public String deleteUser(@PathVariable("id") int id, HttpSession session) {
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null || (currentUser.getRole() != Role.ADMIN && currentUser.getRole() != Role.MUNICIPALITY)) {
            return "redirect:/login";
        }
        userRepository.findById(id).ifPresent(user -> {
            profileRepository.deleteById(user.getEmail());
            userRepository.deleteById(id);
        });
        return "redirect:/login";
    }
}