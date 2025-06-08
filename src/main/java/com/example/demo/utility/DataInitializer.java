//package com.example.demo.utility;
//
//import org.apache.commons.codec.digest.DigestUtils;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.stereotype.Component;
//
//import com.example.demo.model.User;
//import com.example.demo.model.Profile;
//import com.example.demo.model.Role;
//import com.example.demo.model.Device;
//
//import com.example.demo.model.SensorData;
//import com.example.demo.repository.UserRepository;
//import com.example.demo.repository.ProfileRepository;
//import com.example.demo.repository.DeviceRepository;
//
//import com.example.demo.repository.SensorDataRepository;
//
//import java.time.LocalDate;
//import java.time.LocalDateTime;
//import java.util.Arrays;
//
//@Component
//public class DataInitializer implements CommandLineRunner {
//
//    @Autowired
//    private UserRepository userRepository;
//
//    @Autowired
//    private ProfileRepository profileRepository;
//
//    @Autowired
//    private DeviceRepository deviceRepository;
//
//
//    @Autowired
//    private SensorDataRepository sensorDataRepository;
//
//    @Override
//    public void run(String... args) throws Exception {
//        // Seed admin user
//        if (userRepository.findByEmail("admin@gmail.com").isEmpty()) {
//            User admin = new User();
//            admin.setEmail("admin@gmail.com");
//            admin.setPassword(DigestUtils.sha256Hex("admin@gmail.com" + "admin123"));
//            admin.setRole(Role.ADMIN);
//            userRepository.save(admin);
//
//            Profile adminProfile = new Profile();
//            adminProfile.setEmail("admin@gmail.com");
//            adminProfile.setUsername("Admin User");
//            profileRepository.save(adminProfile);
//        }
//
//        // Seed sample farmer, devices, sensors, and sensor data
//        if (userRepository.findByEmail("farmer@gmail.com").isEmpty()) {
//            User farmer = new User();
//            farmer.setEmail("farmer@gmail.com");
//            farmer.setPassword(DigestUtils.sha256Hex("farmer@gmail.com" + "farmer123"));
//            farmer.setRole(Role.FARMER);
//            userRepository.save(farmer);
//
//            Profile farmerProfile = new Profile();
//            farmerProfile.setEmail("farmer@gmail.com");
//            farmerProfile.setUsername("John Doe");
//            farmerProfile.setFarmerId("FARM123456");
//            farmerProfile.setMemberSince(LocalDate.of(2024, 1, 1));
//            farmerProfile.setAddress("Bharatpur, Ward No: 11");
//            farmerProfile.setTotalArea(5.0);
//            farmerProfile.setMainVegetables(Arrays.asList("Tomatoes", "Potatoes", "Spinach", "Cauliflower"));
//            profileRepository.save(farmerProfile);
//
//            // Seed devices with multiple sensors
//            Device station1 = new Device();
//            station1.setEmail("farmer@gmail.com");
//            station1.setStatus(Device.DeviceStatus.ACTIVE);
//            deviceRepository.save(station1);
//
//        
//
//            // Seed sensor data
//            SensorData dhtData = new SensorData();
//            dhtData.setSensorId("DHT_001");
//            dhtData.setSensorType("DHT");
//            dhtData.setTemperature(28.0);
//            dhtData.setHumidity(65.0);
//            dhtData.setTimestamp(LocalDateTime.now().minusMinutes(30));
//            sensorDataRepository.save(dhtData);
//
//            SensorData mq135Data = new SensorData();
//            mq135Data.setSensorId("MQ135_001");
//            mq135Data.setSensorType("MQ135");
//            mq135Data.setCo2(450.0);
//            mq135Data.setTimestamp(LocalDateTime.now().minusHours(2));
//            sensorDataRepository.save(mq135Data);
//
//            SensorData soilData = new SensorData();
//            soilData.setSensorId("SOIL_001");
//            soilData.setSensorType("SOIL");
//            soilData.setHumidity(42.0);
//            soilData.setTimestamp(LocalDateTime.now().minusHours(25));
//            sensorDataRepository.save(soilData);
//        }
//    }
//}