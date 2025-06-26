package com.example.project3;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class NegotiationContext {
    
    private Customer customer;
    private CustomerProfile customerProfile;
    private NegotiationProfile negotiationProfile;
    private CustomerTierService.CustomerTier customerTier;
    private CustomerTierService.NegotiationCapability negotiationCapability;
    
    private BigDecimal cartValue;
    private List<CartItemInfo> cartItems;
    private String negotiationMessage;
    
    private LocalDateTime negotiationTime;
    private String negotiationChannel; // "chat", "email", "phone"
    
    // Market context
    private boolean isSeasonalPeak;
    private boolean hasInventoryPressure;
    private BigDecimal competitivePrice;
    
    // Customer behavior context
    private boolean isRepeatNegotiator;
    private boolean hasRecentPurchase;
    private int negotiationAttemptsThisMonth;
    
    public NegotiationContext() {
        this.negotiationTime = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Customer getCustomer() {
        return customer;
    }
    
    public void setCustomer(Customer customer) {
        this.customer = customer;
    }
    
    public CustomerProfile getCustomerProfile() {
        return customerProfile;
    }
    
    public void setCustomerProfile(CustomerProfile customerProfile) {
        this.customerProfile = customerProfile;
    }
    
    public NegotiationProfile getNegotiationProfile() {
        return negotiationProfile;
    }
    
    public void setNegotiationProfile(NegotiationProfile negotiationProfile) {
        this.negotiationProfile = negotiationProfile;
    }
    
    public CustomerTierService.CustomerTier getCustomerTier() {
        return customerTier;
    }
    
    public void setCustomerTier(CustomerTierService.CustomerTier customerTier) {
        this.customerTier = customerTier;
    }
    
    public CustomerTierService.NegotiationCapability getNegotiationCapability() {
        return negotiationCapability;
    }
    
    public void setNegotiationCapability(CustomerTierService.NegotiationCapability negotiationCapability) {
        this.negotiationCapability = negotiationCapability;
    }
    
    public BigDecimal getCartValue() {
        return cartValue;
    }
    
    public void setCartValue(BigDecimal cartValue) {
        this.cartValue = cartValue;
    }
    
    public List<CartItemInfo> getCartItems() {
        return cartItems;
    }
    
    public void setCartItems(List<CartItemInfo> cartItems) {
        this.cartItems = cartItems;
    }
    
    public String getNegotiationMessage() {
        return negotiationMessage;
    }
    
    public void setNegotiationMessage(String negotiationMessage) {
        this.negotiationMessage = negotiationMessage;
    }
    
    public LocalDateTime getNegotiationTime() {
        return negotiationTime;
    }
    
    public void setNegotiationTime(LocalDateTime negotiationTime) {
        this.negotiationTime = negotiationTime;
    }
    
    public String getNegotiationChannel() {
        return negotiationChannel;
    }
    
    public void setNegotiationChannel(String negotiationChannel) {
        this.negotiationChannel = negotiationChannel;
    }
    
    public boolean isSeasonalPeak() {
        return isSeasonalPeak;
    }
    
    public void setSeasonalPeak(boolean seasonalPeak) {
        isSeasonalPeak = seasonalPeak;
    }
    
    public boolean isHasInventoryPressure() {
        return hasInventoryPressure;
    }
    
    public void setHasInventoryPressure(boolean hasInventoryPressure) {
        this.hasInventoryPressure = hasInventoryPressure;
    }
    
    public BigDecimal getCompetitivePrice() {
        return competitivePrice;
    }
    
    public void setCompetitivePrice(BigDecimal competitivePrice) {
        this.competitivePrice = competitivePrice;
    }
    
    public boolean isRepeatNegotiator() {
        return isRepeatNegotiator;
    }
    
    public void setRepeatNegotiator(boolean repeatNegotiator) {
        isRepeatNegotiator = repeatNegotiator;
    }
    
    public boolean isHasRecentPurchase() {
        return hasRecentPurchase;
    }
    
    public void setHasRecentPurchase(boolean hasRecentPurchase) {
        this.hasRecentPurchase = hasRecentPurchase;
    }
    
    public int getNegotiationAttemptsThisMonth() {
        return negotiationAttemptsThisMonth;
    }
    
    public void setNegotiationAttemptsThisMonth(int negotiationAttemptsThisMonth) {
        this.negotiationAttemptsThisMonth = negotiationAttemptsThisMonth;
    }
    
    // Utility methods
    public String getCustomerTierDisplayName() {
        return customerTier != null ? customerTier.getDisplayName() : "Unknown";
    }
    
    public int getMaxDiscountPercentage() {
        return negotiationCapability != null ? negotiationCapability.getMaxDiscountPercentage() : 0;
    }
    
    public boolean canNegotiate() {
        return negotiationCapability != null && negotiationCapability.canNegotiate() &&
               (negotiationProfile == null || !negotiationProfile.isCurrentlyBlocked());
    }
    
    public String getNegotiationStrategy() {
        return negotiationCapability != null ? negotiationCapability.getNegotiationStrategy() : "STANDARD";
    }
    
    public String getCartSummary() {
        if (cartItems == null || cartItems.isEmpty()) {
            return "Empty cart";
        }
        
        StringBuilder summary = new StringBuilder();
        for (CartItemInfo item : cartItems) {
            if (summary.length() > 0) summary.append(", ");
            summary.append(item.getQuantity()).append("x ").append(item.getProductName());
        }
        return summary.toString();
    }
    
    // Helper class for cart item information
    public static class CartItemInfo {
        private String productName;
        private String categoryName;
        private int quantity;
        private BigDecimal unitPrice;
        private BigDecimal totalPrice;
        private BigDecimal margin;
        
        public CartItemInfo() {}
        
        public CartItemInfo(String productName, String categoryName, int quantity, 
                           BigDecimal unitPrice, BigDecimal totalPrice, BigDecimal margin) {
            this.productName = productName;
            this.categoryName = categoryName;
            this.quantity = quantity;
            this.unitPrice = unitPrice;
            this.totalPrice = totalPrice;
            this.margin = margin;
        }
        
        // Getters and Setters
        public String getProductName() {
            return productName;
        }
        
        public void setProductName(String productName) {
            this.productName = productName;
        }
        
        public String getCategoryName() {
            return categoryName;
        }
        
        public void setCategoryName(String categoryName) {
            this.categoryName = categoryName;
        }
        
        public int getQuantity() {
            return quantity;
        }
        
        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }
        
        public BigDecimal getUnitPrice() {
            return unitPrice;
        }
        
        public void setUnitPrice(BigDecimal unitPrice) {
            this.unitPrice = unitPrice;
        }
        
        public BigDecimal getTotalPrice() {
            return totalPrice;
        }
        
        public void setTotalPrice(BigDecimal totalPrice) {
            this.totalPrice = totalPrice;
        }
        
        public BigDecimal getMargin() {
            return margin;
        }
        
        public void setMargin(BigDecimal margin) {
            this.margin = margin;
        }
    }
} 