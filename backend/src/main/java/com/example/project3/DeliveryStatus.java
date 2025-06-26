package com.example.project3;

public enum DeliveryStatus {
    PENDING("Pending", "Order is being prepared"),
    CONFIRMED("Confirmed", "Delivery date and time confirmed"),
    IN_TRANSIT("In Transit", "Order is out for delivery"),
    DELIVERED("Delivered", "Order has been delivered"),
    FAILED("Failed", "Delivery attempt failed"),
    CANCELLED("Cancelled", "Delivery was cancelled"),
    RESCHEDULED("Rescheduled", "Delivery has been rescheduled");
    
    private final String displayName;
    private final String description;
    
    DeliveryStatus(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getDescription() {
        return description;
    }
    
    public boolean isCompleted() {
        return this == DELIVERED;
    }
    
    public boolean isFailed() {
        return this == FAILED || this == CANCELLED;
    }
    
    public boolean isActive() {
        return this == PENDING || this == CONFIRMED || this == IN_TRANSIT || this == RESCHEDULED;
    }
} 