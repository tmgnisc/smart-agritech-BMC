package com.example.demo.repository;

import com.example.demo.model.SensorData;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SensorDataRepository extends JpaRepository<SensorData, Long> {
    List<SensorData> findBySensorId(String sensorId);
}