package com.example.project3;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;

@Component
@Order(3) // Run after other seeders
public class DeliveryDataSeeder implements CommandLineRunner {
    
    @Autowired
    private DeliveryZoneRepository deliveryZoneRepository;
    
    @Override
    public void run(String... args) throws Exception {
        if (deliveryZoneRepository.count() == 0) {
            seedDeliveryZones();
        }
    }
    
    private void seedDeliveryZones() {
        // Central Bucharest Zone
        DeliveryZone centralZone = new DeliveryZone(
            "Central Bucharest", 
            "Central city area with fastest delivery", 
            4, // 4 hours base delivery
            new BigDecimal("3.99")
        );
        centralZone.setZipCodes("010001,010002,010003,010004,010005,010006,010007,010008,010009,010010");
        centralZone.setCityNames("Bucharest,București");
        centralZone.setMaxDistanceKm(5.0);
        centralZone.setMinDeliveryHours(2);
        centralZone.setMaxDeliveryHours(8);
        centralZone.setExpressDeliveryCost(new BigDecimal("7.99"));
        centralZone.setPremiumDeliveryCost(new BigDecimal("12.99"));
        centralZone.setDeliveryStartTime(LocalTime.of(8, 0));
        centralZone.setDeliveryEndTime(LocalTime.of(20, 0));
        centralZone.setDailyCapacity(100);
        centralZone.setPriorityOrder(1);
        centralZone.setWeatherImpactFactor(1.0);
        
        // Greater Bucharest Zone
        DeliveryZone greaterZone = new DeliveryZone(
            "Greater Bucharest", 
            "Extended Bucharest area", 
            8, // 8 hours base delivery
            new BigDecimal("5.99")
        );
        greaterZone.setZipCodes("011001,012001,013001,014001,015001,020001,030001,040001,050001,060001");
        greaterZone.setCityNames("Bucharest,București,Voluntari,Pantelimon,Bragadiru,Popești-Leordeni");
        greaterZone.setMaxDistanceKm(15.0);
        greaterZone.setMinDeliveryHours(4);
        greaterZone.setMaxDeliveryHours(24);
        greaterZone.setExpressDeliveryCost(new BigDecimal("9.99"));
        greaterZone.setPremiumDeliveryCost(new BigDecimal("15.99"));
        greaterZone.setDeliveryStartTime(LocalTime.of(9, 0));
        greaterZone.setDeliveryEndTime(LocalTime.of(18, 0));
        greaterZone.setDailyCapacity(50);
        greaterZone.setPriorityOrder(2);
        greaterZone.setWeatherImpactFactor(1.1);
        
        // Ilfov County Zone
        DeliveryZone ilfovZone = new DeliveryZone(
            "Ilfov County", 
            "Surrounding areas of Bucharest", 
            24, // 24 hours base delivery
            new BigDecimal("8.99")
        );
        ilfovZone.setZipCodes("077001,077002,077003,077004,077005");
        ilfovZone.setCityNames("Otopeni,Chitila,Buftea,Măgurele,Cernica,Corbeanca,Dobroești,Domnești");
        ilfovZone.setMaxDistanceKm(30.0);
        ilfovZone.setMinDeliveryHours(8);
        ilfovZone.setMaxDeliveryHours(48);
        ilfovZone.setExpressDeliveryCost(new BigDecimal("15.99"));
        ilfovZone.setDeliveryStartTime(LocalTime.of(10, 0));
        ilfovZone.setDeliveryEndTime(LocalTime.of(17, 0));
        ilfovZone.setDailyCapacity(20);
        ilfovZone.setPriorityOrder(3);
        ilfovZone.setWeatherImpactFactor(1.2);
        // No premium delivery for this zone
        
        // Regional Zone (Cluj, Timișoara, etc.)
        DeliveryZone regionalZone = new DeliveryZone(
            "Major Cities", 
            "Major Romanian cities", 
            48, // 48 hours base delivery
            new BigDecimal("12.99")
        );
        regionalZone.setZipCodes("400001,300001,700001,900001,200001");
        regionalZone.setCityNames("Cluj-Napoca,Timișoara,Iași,Constanța,Craiova,Brașov,Galați,Ploiești");
        regionalZone.setMaxDistanceKm(500.0);
        regionalZone.setMinDeliveryHours(24);
        regionalZone.setMaxDeliveryHours(96);
        regionalZone.setExpressDeliveryCost(new BigDecimal("19.99"));
        regionalZone.setDeliveryStartTime(LocalTime.of(9, 0));
        regionalZone.setDeliveryEndTime(LocalTime.of(17, 0));
        regionalZone.setDailyCapacity(10);
        regionalZone.setPriorityOrder(4);
        regionalZone.setWeatherImpactFactor(1.3);
        // No premium delivery for this zone
        
        // Weekend delivery restrictions for some zones
        int weekdaysOnly = 31; // Monday-Friday (1+2+4+8+16)
        ilfovZone.setDeliveryDaysMask(weekdaysOnly);
        regionalZone.setDeliveryDaysMask(weekdaysOnly);
        
        // Save all zones
        List<DeliveryZone> zones = List.of(centralZone, greaterZone, ilfovZone, regionalZone);
        deliveryZoneRepository.saveAll(zones);
        
        System.out.println("✅ Delivery zones seeded successfully!");
        System.out.println("   - Central Bucharest: 2-8 hours, €3.99-€12.99");
        System.out.println("   - Greater Bucharest: 4-24 hours, €5.99-€15.99");
        System.out.println("   - Ilfov County: 8-48 hours, €8.99-€15.99");
        System.out.println("   - Major Cities: 24-96 hours, €12.99-€19.99");
    }
} 