package com.example.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class FarmerController {
	
	
	@GetMapping("/farmer-login")
	public String FarmerLogin() {
		return "farmer-login.html";
	}
	

}
