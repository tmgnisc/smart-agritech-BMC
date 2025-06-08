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

import com.example.demo.model.Farmer;
import com.example.demo.repository.FarmerRepository;

import java.util.List;

@Controller
public class FarmerController {
    
    @Autowired
    private FarmerRepository farmerRepository;
    
    @GetMapping("/farmer-login")
    public String farmerLogin() {
        return "farmer-login.html";
    }
    
    @GetMapping("/farmer-register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("farmer", new Farmer());
        return "farmer-register.html";
    }
    
    @PostMapping("/farmer")
    public String addFarmer(@ModelAttribute Farmer farmer, Model model) {
        if (farmerRepository.findByEmail(farmer.getEmail()).isEmpty()) {
            farmer.setPassword(DigestUtils.sha256Hex(farmer.getPassword()));
            farmerRepository.save(farmer);
            return "redirect:/farmer-login";
        } else {
            model.addAttribute("error", "Email already registered");
            return "farmer-register.html";
        }
    }
    
    @PostMapping("/farmer-login")
    public String loginFarmer(@ModelAttribute Farmer farmer, Model model) {
        List<Farmer> farmers = farmerRepository.findByEmail(farmer.getEmail());
        if (!farmers.isEmpty() && farmers.get(0).getPassword().equals(DigestUtils.sha256Hex(farmer.getPassword()))) {
            return "redirect:/farmer-dashboard";
        } else {
            model.addAttribute("error", "Invalid email or password");
            return "farmer-login.html";
        }
    }
    
    @DeleteMapping("/farmer/{id}")
    public String deleteFarmer(@PathVariable("id") int id) {
        farmerRepository.deleteById(id);
        return "redirect:/farmer-login";
    }
}