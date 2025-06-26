package com.example.project3;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/delivery")
public class DeliveryController {
    
    @Autowired
    private AIDeliveryEstimationService deliveryEstimationService;
    
    @Autowired
    private DeliveryAddressRepository deliveryAddressRepository;
    
    @Autowired
    private DeliveryZoneRepository deliveryZoneRepository;
    
    /**
     * Estimate delivery dates for a customer's cart
     */
    @PostMapping("/estimate")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('USER')")
    public ResponseEntity<AIDeliveryEstimationService.DeliveryEstimation> estimateDelivery(
            @RequestBody DeliveryEstimationRequest request) {
        
        try {
            AIDeliveryEstimationService.DeliveryEstimation estimation = 
                deliveryEstimationService.estimateDeliveryDates(
                    request.getCustomerId(),
                    request.getAddressId(),
                    request.getCartItems(),
                    request.getPreferredMethod()
                );
            
            return ResponseEntity.ok(estimation);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Get customer's delivery addresses
     */
    @GetMapping("/addresses/{customerId}")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('USER')")
    public ResponseEntity<List<DeliveryAddress>> getCustomerAddresses(@PathVariable Long customerId) {
        List<DeliveryAddress> addresses = deliveryAddressRepository
                .findByCustomerIdOrderByIsDefaultDescCreatedAtAsc(customerId);
        return ResponseEntity.ok(addresses);
    }
    
    /**
     * Add a new delivery address
     */
    @PostMapping("/addresses")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('USER')")
    public ResponseEntity<DeliveryAddress> addDeliveryAddress(@RequestBody DeliveryAddressRequest request) {
        try {
            DeliveryAddress address = new DeliveryAddress();
            address.setStreetAddress(request.getStreetAddress());
            address.setApartmentUnit(request.getApartmentUnit());
            address.setCity(request.getCity());
            address.setStateProvince(request.getStateProvince());
            address.setZipCode(request.getZipCode());
            address.setCountry(request.getCountry());
            address.setDeliveryInstructions(request.getDeliveryInstructions());
            address.setContactPhone(request.getContactPhone());
            address.setAddressNickname(request.getAddressNickname());
            address.setIsDefault(request.getIsDefault());
            
            // Set delivery characteristics
            address.setIsResidential(request.getIsResidential());
            address.setRequiresSignature(request.getRequiresSignature());
            address.setFloorNumber(request.getFloorNumber());
            address.setHasElevator(request.getHasElevator());
            
            DeliveryAddress saved = deliveryAddressRepository.save(address);
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Get available delivery zones
     */
    @GetMapping("/zones")
    public ResponseEntity<List<DeliveryZone>> getAvailableZones() {
        List<DeliveryZone> zones = deliveryZoneRepository.findByIsActiveTrueOrderByPriorityOrderAsc();
        return ResponseEntity.ok(zones);
    }
    
    /**
     * Check if delivery is available for an address
     */
    @PostMapping("/check-availability")
    public ResponseEntity<Map<String, Object>> checkDeliveryAvailability(@RequestBody AddressCheckRequest request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<DeliveryZone> zones = deliveryZoneRepository
                    .findBestZonesForAddress(request.getZipCode(), request.getCity());
            
            if (!zones.isEmpty()) {
                DeliveryZone bestZone = zones.get(0);
                response.put("available", true);
                response.put("zoneName", bestZone.getName());
                response.put("baseDeliveryHours", bestZone.getBaseDeliveryHours());
                response.put("baseDeliveryCost", bestZone.getBaseDeliveryCost());
                response.put("expressAvailable", bestZone.getExpressDeliveryCost() != null);
                response.put("premiumAvailable", bestZone.getPremiumDeliveryCost() != null);
            } else {
                response.put("available", false);
                response.put("message", "Delivery not available for this location");
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("available", false);
            response.put("message", "Error checking delivery availability");
            return ResponseEntity.ok(response);
        }
    }
    
    // Request DTOs
    public static class DeliveryEstimationRequest {
        private Long customerId;
        private Long addressId;
        private List<CartItemDTO> cartItems;
        private DeliveryMethod preferredMethod = DeliveryMethod.STANDARD;
        
        // Getters and setters
        public Long getCustomerId() { return customerId; }
        public void setCustomerId(Long customerId) { this.customerId = customerId; }
        
        public Long getAddressId() { return addressId; }
        public void setAddressId(Long addressId) { this.addressId = addressId; }
        
        public List<CartItemDTO> getCartItems() { return cartItems; }
        public void setCartItems(List<CartItemDTO> cartItems) { this.cartItems = cartItems; }
        
        public DeliveryMethod getPreferredMethod() { return preferredMethod; }
        public void setPreferredMethod(DeliveryMethod preferredMethod) { this.preferredMethod = preferredMethod; }
    }
    
    public static class DeliveryAddressRequest {
        private String streetAddress;
        private String apartmentUnit;
        private String city;
        private String stateProvince;
        private String zipCode;
        private String country;
        private String deliveryInstructions;
        private String contactPhone;
        private String addressNickname;
        private Boolean isDefault = false;
        private Boolean isResidential = true;
        private Boolean requiresSignature = false;
        private Integer floorNumber;
        private Boolean hasElevator;
        
        // Getters and setters
        public String getStreetAddress() { return streetAddress; }
        public void setStreetAddress(String streetAddress) { this.streetAddress = streetAddress; }
        
        public String getApartmentUnit() { return apartmentUnit; }
        public void setApartmentUnit(String apartmentUnit) { this.apartmentUnit = apartmentUnit; }
        
        public String getCity() { return city; }
        public void setCity(String city) { this.city = city; }
        
        public String getStateProvince() { return stateProvince; }
        public void setStateProvince(String stateProvince) { this.stateProvince = stateProvince; }
        
        public String getZipCode() { return zipCode; }
        public void setZipCode(String zipCode) { this.zipCode = zipCode; }
        
        public String getCountry() { return country; }
        public void setCountry(String country) { this.country = country; }
        
        public String getDeliveryInstructions() { return deliveryInstructions; }
        public void setDeliveryInstructions(String deliveryInstructions) { this.deliveryInstructions = deliveryInstructions; }
        
        public String getContactPhone() { return contactPhone; }
        public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }
        
        public String getAddressNickname() { return addressNickname; }
        public void setAddressNickname(String addressNickname) { this.addressNickname = addressNickname; }
        
        public Boolean getIsDefault() { return isDefault; }
        public void setIsDefault(Boolean isDefault) { this.isDefault = isDefault; }
        
        public Boolean getIsResidential() { return isResidential; }
        public void setIsResidential(Boolean isResidential) { this.isResidential = isResidential; }
        
        public Boolean getRequiresSignature() { return requiresSignature; }
        public void setRequiresSignature(Boolean requiresSignature) { this.requiresSignature = requiresSignature; }
        
        public Integer getFloorNumber() { return floorNumber; }
        public void setFloorNumber(Integer floorNumber) { this.floorNumber = floorNumber; }
        
        public Boolean getHasElevator() { return hasElevator; }
        public void setHasElevator(Boolean hasElevator) { this.hasElevator = hasElevator; }
    }
    
    public static class AddressCheckRequest {
        private String zipCode;
        private String city;
        
        public String getZipCode() { return zipCode; }
        public void setZipCode(String zipCode) { this.zipCode = zipCode; }
        
        public String getCity() { return city; }
        public void setCity(String city) { this.city = city; }
    }
} 