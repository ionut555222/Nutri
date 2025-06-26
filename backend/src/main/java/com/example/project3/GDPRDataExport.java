package com.example.project3;

import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.List;

public class GDPRDataExport {
    private Long customerId;
    private LocalDateTime exportDate;
    private String requestReason;
    private PersonalDataExport personalData;
    private List<OrderExport> orderHistory;
    private List<AddressExport> deliveryAddresses;
    private CustomerProfileExport customerProfile;
    private List<ChatExport> chatHistory;
    
    // Getters and setters
    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }
    
    public LocalDateTime getExportDate() { return exportDate; }
    public void setExportDate(LocalDateTime exportDate) { this.exportDate = exportDate; }
    
    public String getRequestReason() { return requestReason; }
    public void setRequestReason(String requestReason) { this.requestReason = requestReason; }
    
    public PersonalDataExport getPersonalData() { return personalData; }
    public void setPersonalData(PersonalDataExport personalData) { this.personalData = personalData; }
    
    public List<OrderExport> getOrderHistory() { return orderHistory; }
    public void setOrderHistory(List<OrderExport> orderHistory) { this.orderHistory = orderHistory; }
    
    public List<AddressExport> getDeliveryAddresses() { return deliveryAddresses; }
    public void setDeliveryAddresses(List<AddressExport> deliveryAddresses) { this.deliveryAddresses = deliveryAddresses; }
    
    public CustomerProfileExport getCustomerProfile() { return customerProfile; }
    public void setCustomerProfile(CustomerProfileExport customerProfile) { this.customerProfile = customerProfile; }
    
    public List<ChatExport> getChatHistory() { return chatHistory; }
    public void setChatHistory(List<ChatExport> chatHistory) { this.chatHistory = chatHistory; }
}

class PersonalDataExport {
    private Long customerId;
    private String email;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private LocalDateTime dateOfBirth;
    private LocalDateTime registrationDate;
    private Boolean marketingConsent;
    private Boolean analyticsConsent;
    private Boolean profilingConsent;
    private LocalDateTime consentDate;
    private LocalDateTime consentWithdrawnDate;
    
    // Getters and setters
    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    
    public LocalDateTime getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDateTime dateOfBirth) { this.dateOfBirth = dateOfBirth; }
    
    public LocalDateTime getRegistrationDate() { return registrationDate; }
    public void setRegistrationDate(LocalDateTime registrationDate) { this.registrationDate = registrationDate; }
    
    public Boolean getMarketingConsent() { return marketingConsent; }
    public void setMarketingConsent(Boolean marketingConsent) { this.marketingConsent = marketingConsent; }
    
    public Boolean getAnalyticsConsent() { return analyticsConsent; }
    public void setAnalyticsConsent(Boolean analyticsConsent) { this.analyticsConsent = analyticsConsent; }
    
    public Boolean getProfilingConsent() { return profilingConsent; }
    public void setProfilingConsent(Boolean profilingConsent) { this.profilingConsent = profilingConsent; }
    
    public LocalDateTime getConsentDate() { return consentDate; }
    public void setConsentDate(LocalDateTime consentDate) { this.consentDate = consentDate; }
    
    public LocalDateTime getConsentWithdrawnDate() { return consentWithdrawnDate; }
    public void setConsentWithdrawnDate(LocalDateTime consentWithdrawnDate) { this.consentWithdrawnDate = consentWithdrawnDate; }
}

class OrderExport {
    private Long orderId;
    private LocalDateTime orderDate;
    private BigDecimal totalAmount;
    private boolean fulfilled;
    private LocalDateTime fulfilledDate;
    private String orderNotes;
    
    // Getters and setters
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    
    public LocalDateTime getOrderDate() { return orderDate; }
    public void setOrderDate(LocalDateTime orderDate) { this.orderDate = orderDate; }
    
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    
    public boolean isFulfilled() { return fulfilled; }
    public void setFulfilled(boolean fulfilled) { this.fulfilled = fulfilled; }
    
    public LocalDateTime getFulfilledDate() { return fulfilledDate; }
    public void setFulfilledDate(LocalDateTime fulfilledDate) { this.fulfilledDate = fulfilledDate; }
    
    public String getOrderNotes() { return orderNotes; }
    public void setOrderNotes(String orderNotes) { this.orderNotes = orderNotes; }
}

class AddressExport {
    private Long addressId;
    private String streetAddress;
    private String city;
    private String zipCode;
    private String country;
    private String deliveryInstructions;
    private Boolean isDefault;
    private LocalDateTime createdAt;
    
    // Getters and setters
    public Long getAddressId() { return addressId; }
    public void setAddressId(Long addressId) { this.addressId = addressId; }
    
    public String getStreetAddress() { return streetAddress; }
    public void setStreetAddress(String streetAddress) { this.streetAddress = streetAddress; }
    
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    
    public String getZipCode() { return zipCode; }
    public void setZipCode(String zipCode) { this.zipCode = zipCode; }
    
    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }
    
    public String getDeliveryInstructions() { return deliveryInstructions; }
    public void setDeliveryInstructions(String deliveryInstructions) { this.deliveryInstructions = deliveryInstructions; }
    
    public Boolean getIsDefault() { return isDefault; }
    public void setIsDefault(Boolean isDefault) { this.isDefault = isDefault; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}

class CustomerProfileExport {
    private BigDecimal totalSpent;
    private Integer orderCount;
    private BigDecimal averageOrderValue;
    private LocalDateTime lastOrderDate;
    private String preferredCategories;
    private String customerTier;
    
    // Getters and setters
    public BigDecimal getTotalSpent() { return totalSpent; }
    public void setTotalSpent(BigDecimal totalSpent) { this.totalSpent = totalSpent; }
    
    public Integer getOrderCount() { return orderCount; }
    public void setOrderCount(Integer orderCount) { this.orderCount = orderCount; }
    
    public BigDecimal getAverageOrderValue() { return averageOrderValue; }
    public void setAverageOrderValue(BigDecimal averageOrderValue) { this.averageOrderValue = averageOrderValue; }
    
    public LocalDateTime getLastOrderDate() { return lastOrderDate; }
    public void setLastOrderDate(LocalDateTime lastOrderDate) { this.lastOrderDate = lastOrderDate; }
    
    public String getPreferredCategories() { return preferredCategories; }
    public void setPreferredCategories(String preferredCategories) { this.preferredCategories = preferredCategories; }
    
    public String getCustomerTier() { return customerTier; }
    public void setCustomerTier(String customerTier) { this.customerTier = customerTier; }
}

class ChatExport {
    private Long chatId;
    private String message;
    private String response;
    private LocalDateTime timestamp;
    
    // Getters and setters
    public Long getChatId() { return chatId; }
    public void setChatId(Long chatId) { this.chatId = chatId; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    
    public String getResponse() { return response; }
    public void setResponse(String response) { this.response = response; }
    
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
} 