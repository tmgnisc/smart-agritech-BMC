package com.example.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.demo.model.Farmer;
import java.util.List;

public interface FarmerRepository extends JpaRepository<Farmer, Integer> {
    
    Boolean existsByEmailAndPassword(String email, String password);
    
    List<Farmer> findByEmail(String email);
}