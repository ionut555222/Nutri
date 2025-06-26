package com.example.project3;

public enum DeliveryMethod {
    STANDARD("Standard Delivery", "Regular delivery within standard timeframe"),
    EXPRESS("Express Delivery", "Faster delivery with reduced timeframe"),
    PREMIUM("Premium Delivery", "Fastest delivery with priority handling");
    
    private final String displayName;
    private final String description;
    
    DeliveryMethod(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getDescription() {
        return description;
    }
    
    public boolean isExpress() {
        return this == EXPRESS || this == PREMIUM;
    }
    
    public boolean isPremium() {
        return this == PREMIUM;
    }
} 