package com.example.project3;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.Set;

@Entity
@Table(name = "delivery_zones")
public class DeliveryZone {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    @Column(nullable = false)
    private String description;
    
    // Geographic boundaries
    @Column(name = "zip_codes")
    private String zipCodes; // Comma-separated list of zip codes
    
    @Column(name = "city_names")
    private String cityNames; // Comma-separated list of cities
    
    @Column(name = "max_distance_km")
    private Double maxDistanceKm;
    
    // Delivery timing
    @Column(name = "base_delivery_hours")
    private Integer baseDeliveryHours; // Base delivery time in hours
    
    @Column(name = "min_delivery_hours")
    private Integer minDeliveryHours; // Minimum delivery time
    
    @Column(name = "max_delivery_hours")
    private Integer maxDeliveryHours; // Maximum delivery time
    
    // Delivery costs
    @Column(name = "base_delivery_cost", precision = 10, scale = 2)
    private BigDecimal baseDeliveryCost;
    
    @Column(name = "express_delivery_cost", precision = 10, scale = 2)
    private BigDecimal expressDeliveryCost;
    
    @Column(name = "premium_delivery_cost", precision = 10, scale = 2)
    private BigDecimal premiumDeliveryCost;
    
    // Operating hours
    @Column(name = "delivery_start_time")
    private LocalTime deliveryStartTime;
    
    @Column(name = "delivery_end_time")
    private LocalTime deliveryEndTime;
    
    // Capacity and availability
    @Column(name = "daily_capacity")
    private Integer dailyCapacity; // Max deliveries per day
    
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    @Column(name = "priority_order")
    private Integer priorityOrder = 0; // For zone selection priority
    
    // Delivery days (bit mask: 1=Monday, 2=Tuesday, 4=Wednesday, etc.)
    @Column(name = "delivery_days_mask")
    private Integer deliveryDaysMask = 127; // Default: all days (1+2+4+8+16+32+64)
    
    // Weather impact factor (0.0 to 2.0, where 1.0 is normal)
    @Column(name = "weather_impact_factor")
    private Double weatherImpactFactor = 1.0;
    
    // Constructors
    public DeliveryZone() {}
    
    public DeliveryZone(String name, String description, Integer baseDeliveryHours, BigDecimal baseDeliveryCost) {
        this.name = name;
        this.description = description;
        this.baseDeliveryHours = baseDeliveryHours;
        this.baseDeliveryCost = baseDeliveryCost;
        this.minDeliveryHours = baseDeliveryHours - 2;
        this.maxDeliveryHours = baseDeliveryHours + 4;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getZipCodes() {
        return zipCodes;
    }
    
    public void setZipCodes(String zipCodes) {
        this.zipCodes = zipCodes;
    }
    
    public String getCityNames() {
        return cityNames;
    }
    
    public void setCityNames(String cityNames) {
        this.cityNames = cityNames;
    }
    
    public Double getMaxDistanceKm() {
        return maxDistanceKm;
    }
    
    public void setMaxDistanceKm(Double maxDistanceKm) {
        this.maxDistanceKm = maxDistanceKm;
    }
    
    public Integer getBaseDeliveryHours() {
        return baseDeliveryHours;
    }
    
    public void setBaseDeliveryHours(Integer baseDeliveryHours) {
        this.baseDeliveryHours = baseDeliveryHours;
    }
    
    public Integer getMinDeliveryHours() {
        return minDeliveryHours;
    }
    
    public void setMinDeliveryHours(Integer minDeliveryHours) {
        this.minDeliveryHours = minDeliveryHours;
    }
    
    public Integer getMaxDeliveryHours() {
        return maxDeliveryHours;
    }
    
    public void setMaxDeliveryHours(Integer maxDeliveryHours) {
        this.maxDeliveryHours = maxDeliveryHours;
    }
    
    public BigDecimal getBaseDeliveryCost() {
        return baseDeliveryCost;
    }
    
    public void setBaseDeliveryCost(BigDecimal baseDeliveryCost) {
        this.baseDeliveryCost = baseDeliveryCost;
    }
    
    public BigDecimal getExpressDeliveryCost() {
        return expressDeliveryCost;
    }
    
    public void setExpressDeliveryCost(BigDecimal expressDeliveryCost) {
        this.expressDeliveryCost = expressDeliveryCost;
    }
    
    public BigDecimal getPremiumDeliveryCost() {
        return premiumDeliveryCost;
    }
    
    public void setPremiumDeliveryCost(BigDecimal premiumDeliveryCost) {
        this.premiumDeliveryCost = premiumDeliveryCost;
    }
    
    public LocalTime getDeliveryStartTime() {
        return deliveryStartTime;
    }
    
    public void setDeliveryStartTime(LocalTime deliveryStartTime) {
        this.deliveryStartTime = deliveryStartTime;
    }
    
    public LocalTime getDeliveryEndTime() {
        return deliveryEndTime;
    }
    
    public void setDeliveryEndTime(LocalTime deliveryEndTime) {
        this.deliveryEndTime = deliveryEndTime;
    }
    
    public Integer getDailyCapacity() {
        return dailyCapacity;
    }
    
    public void setDailyCapacity(Integer dailyCapacity) {
        this.dailyCapacity = dailyCapacity;
    }
    
    public Boolean getIsActive() {
        return isActive;
    }
    
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
    
    public Integer getPriorityOrder() {
        return priorityOrder;
    }
    
    public void setPriorityOrder(Integer priorityOrder) {
        this.priorityOrder = priorityOrder;
    }
    
    public Integer getDeliveryDaysMask() {
        return deliveryDaysMask;
    }
    
    public void setDeliveryDaysMask(Integer deliveryDaysMask) {
        this.deliveryDaysMask = deliveryDaysMask;
    }
    
    public Double getWeatherImpactFactor() {
        return weatherImpactFactor;
    }
    
    public void setWeatherImpactFactor(Double weatherImpactFactor) {
        this.weatherImpactFactor = weatherImpactFactor;
    }
    
    // Utility methods
    public boolean isDeliveryDay(int dayOfWeek) {
        // dayOfWeek: 1=Monday, 2=Tuesday, ..., 7=Sunday
        int dayMask = 1 << (dayOfWeek - 1);
        return (deliveryDaysMask & dayMask) != 0;
    }
    
    public boolean servesZipCode(String zipCode) {
        if (zipCodes == null || zipCode == null) return false;
        return zipCodes.toLowerCase().contains(zipCode.toLowerCase());
    }
    
    public boolean servesCity(String city) {
        if (cityNames == null || city == null) return false;
        return cityNames.toLowerCase().contains(city.toLowerCase());
    }
    
    public BigDecimal getDeliveryCostForMethod(DeliveryMethod method) {
        switch (method) {
            case EXPRESS:
                return expressDeliveryCost != null ? expressDeliveryCost : baseDeliveryCost;
            case PREMIUM:
                return premiumDeliveryCost != null ? premiumDeliveryCost : baseDeliveryCost;
            case STANDARD:
            default:
                return baseDeliveryCost;
        }
    }
    
    public Integer getDeliveryHoursForMethod(DeliveryMethod method) {
        switch (method) {
            case EXPRESS:
                return Math.max(minDeliveryHours, baseDeliveryHours / 2);
            case PREMIUM:
                return minDeliveryHours;
            case STANDARD:
            default:
                return baseDeliveryHours;
        }
    }
} 