package com.example.demo.utility;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.example.demo.model.User;
import com.example.demo.model.Profile;
import com.example.demo.model.Role;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.ProfileRepository;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProfileRepository profileRepository;

    @Override
    public void run(String... args) throws Exception {
        // Check if admin exists, create if not
        if (userRepository.findByEmail("admin@gmail.com").isEmpty()) {
            User admin = new User();
            admin.setEmail("admin@example.com");
            admin.setPassword(DigestUtils.sha256Hex("admin@gmail.com" + "admin123"));
            admin.setRole(Role.ADMIN);
            userRepository.save(admin);

            Profile adminProfile = new Profile();
            adminProfile.setEmail("admin@gmail.com");
            adminProfile.setUsername("Admin User");
            profileRepository.save(adminProfile);
        }
    }
    
   
}