package com.example.project3;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "delivery_addresses")
public class DeliveryAddress {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    // Address components
    @Column(name = "street_address", nullable = false)
    private String streetAddress;
    
    @Column(name = "apartment_unit")
    private String apartmentUnit;
    
    @Column(nullable = false)
    private String city;
    
    @Column(name = "state_province")
    private String stateProvince;
    
    @Column(name = "zip_code", nullable = false)
    private String zipCode;
    
    @Column(nullable = false)
    private String country;
    
    // Geographic coordinates (for distance calculations)
    @Column
    private Double latitude;
    
    @Column
    private Double longitude;
    
    // Delivery instructions
    @Column(name = "delivery_instructions", length = 500)
    private String deliveryInstructions;
    
    @Column(name = "access_code")
    private String accessCode;
    
    @Column(name = "contact_phone")
    private String contactPhone;
    
    // Address validation and geocoding
    @Column(name = "is_validated")
    private Boolean isValidated = false;
    
    @Column(name = "is_geocoded")
    private Boolean isGeocoded = false;
    
    @Column(name = "formatted_address")
    private String formattedAddress; // Standardized address from geocoding service
    
    // Delivery characteristics
    @Column(name = "is_residential")
    private Boolean isResidential = true;
    
    @Column(name = "requires_signature")
    private Boolean requiresSignature = false;
    
    @Column(name = "has_elevator")
    private Boolean hasElevator;
    
    @Column(name = "floor_number")
    private Integer floorNumber;
    
    @Column(name = "delivery_difficulty_score")
    private Integer deliveryDifficultyScore = 1; // 1-5 scale, 1=easy, 5=difficult
    
    // Metadata
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "last_validated_at")
    private LocalDateTime lastValidatedAt;
    
    // Relationship to customer
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private Customer customer;
    
    @Column(name = "is_default")
    private Boolean isDefault = false;
    
    @Column(name = "address_nickname")
    private String addressNickname; // e.g., "Home", "Office", "Mom's House"
    
    // Constructors
    public DeliveryAddress() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public DeliveryAddress(String streetAddress, String city, String zipCode, String country) {
        this();
        this.streetAddress = streetAddress;
        this.city = city;
        this.zipCode = zipCode;
        this.country = country;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getStreetAddress() {
        return streetAddress;
    }
    
    public void setStreetAddress(String streetAddress) {
        this.streetAddress = streetAddress;
    }
    
    public String getApartmentUnit() {
        return apartmentUnit;
    }
    
    public void setApartmentUnit(String apartmentUnit) {
        this.apartmentUnit = apartmentUnit;
    }
    
    public String getCity() {
        return city;
    }
    
    public void setCity(String city) {
        this.city = city;
    }
    
    public String getStateProvince() {
        return stateProvince;
    }
    
    public void setStateProvince(String stateProvince) {
        this.stateProvince = stateProvince;
    }
    
    public String getZipCode() {
        return zipCode;
    }
    
    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }
    
    public String getCountry() {
        return country;
    }
    
    public void setCountry(String country) {
        this.country = country;
    }
    
    public Double getLatitude() {
        return latitude;
    }
    
    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }
    
    public Double getLongitude() {
        return longitude;
    }
    
    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }
    
    public String getDeliveryInstructions() {
        return deliveryInstructions;
    }
    
    public void setDeliveryInstructions(String deliveryInstructions) {
        this.deliveryInstructions = deliveryInstructions;
    }
    
    public String getAccessCode() {
        return accessCode;
    }
    
    public void setAccessCode(String accessCode) {
        this.accessCode = accessCode;
    }
    
    public String getContactPhone() {
        return contactPhone;
    }
    
    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }
    
    public Boolean getIsValidated() {
        return isValidated;
    }
    
    public void setIsValidated(Boolean isValidated) {
        this.isValidated = isValidated;
    }
    
    public Boolean getIsGeocoded() {
        return isGeocoded;
    }
    
    public void setIsGeocoded(Boolean isGeocoded) {
        this.isGeocoded = isGeocoded;
    }
    
    public String getFormattedAddress() {
        return formattedAddress;
    }
    
    public void setFormattedAddress(String formattedAddress) {
        this.formattedAddress = formattedAddress;
    }
    
    public Boolean getIsResidential() {
        return isResidential;
    }
    
    public void setIsResidential(Boolean isResidential) {
        this.isResidential = isResidential;
    }
    
    public Boolean getRequiresSignature() {
        return requiresSignature;
    }
    
    public void setRequiresSignature(Boolean requiresSignature) {
        this.requiresSignature = requiresSignature;
    }
    
    public Boolean getHasElevator() {
        return hasElevator;
    }
    
    public void setHasElevator(Boolean hasElevator) {
        this.hasElevator = hasElevator;
    }
    
    public Integer getFloorNumber() {
        return floorNumber;
    }
    
    public void setFloorNumber(Integer floorNumber) {
        this.floorNumber = floorNumber;
    }
    
    public Integer getDeliveryDifficultyScore() {
        return deliveryDifficultyScore;
    }
    
    public void setDeliveryDifficultyScore(Integer deliveryDifficultyScore) {
        this.deliveryDifficultyScore = deliveryDifficultyScore;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public LocalDateTime getLastValidatedAt() {
        return lastValidatedAt;
    }
    
    public void setLastValidatedAt(LocalDateTime lastValidatedAt) {
        this.lastValidatedAt = lastValidatedAt;
    }
    
    public Customer getCustomer() {
        return customer;
    }
    
    public void setCustomer(Customer customer) {
        this.customer = customer;
    }
    
    public Boolean getIsDefault() {
        return isDefault;
    }
    
    public void setIsDefault(Boolean isDefault) {
        this.isDefault = isDefault;
    }
    
    public String getAddressNickname() {
        return addressNickname;
    }
    
    public void setAddressNickname(String addressNickname) {
        this.addressNickname = addressNickname;
    }
    
    // Utility methods
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
    
    public String getFullAddress() {
        StringBuilder sb = new StringBuilder();
        sb.append(streetAddress);
        
        if (apartmentUnit != null && !apartmentUnit.trim().isEmpty()) {
            sb.append(", ").append(apartmentUnit);
        }
        
        sb.append(", ").append(city);
        
        if (stateProvince != null && !stateProvince.trim().isEmpty()) {
            sb.append(", ").append(stateProvince);
        }
        
        sb.append(" ").append(zipCode);
        sb.append(", ").append(country);
        
        return sb.toString();
    }
    
    public boolean hasCoordinates() {
        return latitude != null && longitude != null;
    }
    
    public double calculateDistance(DeliveryAddress other) {
        if (!this.hasCoordinates() || !other.hasCoordinates()) {
            return -1; // Cannot calculate distance without coordinates
        }
        
        // Haversine formula for calculating distance between two points on Earth
        double lat1Rad = Math.toRadians(this.latitude);
        double lat2Rad = Math.toRadians(other.latitude);
        double deltaLatRad = Math.toRadians(other.latitude - this.latitude);
        double deltaLonRad = Math.toRadians(other.longitude - this.longitude);
        
        double a = Math.sin(deltaLatRad / 2) * Math.sin(deltaLatRad / 2) +
                   Math.cos(lat1Rad) * Math.cos(lat2Rad) *
                   Math.sin(deltaLonRad / 2) * Math.sin(deltaLonRad / 2);
        
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        // Earth's radius in kilometers
        double earthRadiusKm = 6371.0;
        
        return earthRadiusKm * c;
    }
    
    public boolean isCompleteAddress() {
        return streetAddress != null && !streetAddress.trim().isEmpty() &&
               city != null && !city.trim().isEmpty() &&
               zipCode != null && !zipCode.trim().isEmpty() &&
               country != null && !country.trim().isEmpty();
    }
} 