package com.example.project3;

import java.util.List;

public class CheckoutRequest {
    private List<CartItemDTO> cartItems;
    private String couponCode; // Optional coupon code for discount

    public List<CartItemDTO> getCartItems() {
        return cartItems;
    }

    public void setCartItems(List<CartItemDTO> cartItems) {
        this.cartItems = cartItems;
    }
    
    public String getCouponCode() {
        return couponCode;
    }
    
    public void setCouponCode(String couponCode) {
        this.couponCode = couponCode;
    }
} 