package com.example.project3;

import java.math.BigDecimal;
import java.util.List;

public class NegotiationRequest {
    
    private String message;
    private BigDecimal cartValue;
    private List<CartItem> cartItems;
    private String negotiationType; // "price_reduction", "free_shipping", "bundle_deal", "general"
    private BigDecimal requestedDiscount; // Optional: specific discount amount requested
    private String requestedDiscountType; // "percentage", "fixed_amount"
    
    public NegotiationRequest() {}
    
    public NegotiationRequest(String message, BigDecimal cartValue, List<CartItem> cartItems) {
        this.message = message;
        this.cartValue = cartValue;
        this.cartItems = cartItems;
    }
    
    // Getters and Setters
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public BigDecimal getCartValue() {
        return cartValue;
    }
    
    public void setCartValue(BigDecimal cartValue) {
        this.cartValue = cartValue;
    }
    
    public List<CartItem> getCartItems() {
        return cartItems;
    }
    
    public void setCartItems(List<CartItem> cartItems) {
        this.cartItems = cartItems;
    }
    
    public String getNegotiationType() {
        return negotiationType;
    }
    
    public void setNegotiationType(String negotiationType) {
        this.negotiationType = negotiationType;
    }
    
    public BigDecimal getRequestedDiscount() {
        return requestedDiscount;
    }
    
    public void setRequestedDiscount(BigDecimal requestedDiscount) {
        this.requestedDiscount = requestedDiscount;
    }
    
    public String getRequestedDiscountType() {
        return requestedDiscountType;
    }
    
    public void setRequestedDiscountType(String requestedDiscountType) {
        this.requestedDiscountType = requestedDiscountType;
    }
    
    // Helper class for cart items in the request
    public static class CartItem {
        private Long productId;
        private String productName;
        private int quantity;
        private BigDecimal unitPrice;
        
        public CartItem() {}
        
        public CartItem(Long productId, String productName, int quantity, BigDecimal unitPrice) {
            this.productId = productId;
            this.productName = productName;
            this.quantity = quantity;
            this.unitPrice = unitPrice;
        }
        
        public Long getProductId() {
            return productId;
        }
        
        public void setProductId(Long productId) {
            this.productId = productId;
        }
        
        public String getProductName() {
            return productName;
        }
        
        public void setProductName(String productName) {
            this.productName = productName;
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
            return unitPrice != null ? unitPrice.multiply(BigDecimal.valueOf(quantity)) : BigDecimal.ZERO;
        }
    }
} 