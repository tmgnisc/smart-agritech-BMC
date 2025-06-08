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
    
    @PostMapping("/login")
    public String loginUser(@ModelAttribute User user, Model model) {
        List<User> users = userRepository.findByEmail(user.getEmail());
        if (!users.isEmpty() && users.get(0).getPassword().equals(DigestUtils.sha256Hex(user.getEmail() + user.getPassword()))) {
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
    
    @DeleteMapping("/user/{id}")
    public String deleteUser(@PathVariable("id") int id) {
        userRepository.findById(id).ifPresent(user -> {
            profileRepository.deleteById(user.getEmail());
            userRepository.deleteById(id);
        });
        return "redirect:/login";
    }
}