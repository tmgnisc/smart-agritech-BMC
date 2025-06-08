package com.example.demo.restcontroller;

import com.example.demo.model.SensorData;
import com.example.demo.model.User;
import com.example.demo.model.Role;
import com.example.demo.repository.SensorDataRepository;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/sensors")
public class SensorDataController {

    @Autowired
    private SensorDataRepository sensorDataRepository;

    // POST endpoint for ESP32 to send sensor data
    @PostMapping("/data")
    public ResponseEntity<String> addSensorData(@Valid @RequestBody SensorData sensorData) {
        sensorData.setTimestamp(LocalDateTime.now());
        sensorDataRepository.save(sensorData);
        return ResponseEntity.ok("Sensor data saved successfully");
    }

    // GET endpoint to retrieve all sensor data (Admin and Municipality only)
    @GetMapping("/data")
    public ResponseEntity<List<SensorData>> getAllSensorData(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null || (user.getRole() != Role.ADMIN && user.getRole() != Role.MUNICIPALITY)) {
            return ResponseEntity.status(403).build();
        }
        List<SensorData> data = sensorDataRepository.findAll();
        return ResponseEntity.ok(data);
    }

    // GET endpoint to retrieve sensor data by sensor ID (Admin and Municipality only)
    @GetMapping("/data/{sensorId}")
    public ResponseEntity<List<SensorData>> getSensorDataById(@PathVariable String sensorId, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null || (user.getRole() != Role.ADMIN && user.getRole() != Role.MUNICIPALITY)) {
            return ResponseEntity.status(403).build();
        }
        List<SensorData> data = sensorDataRepository.findBySensorId(sensorId);
        return ResponseEntity.ok(data);
    }
}
