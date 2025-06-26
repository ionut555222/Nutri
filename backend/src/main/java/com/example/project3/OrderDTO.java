package com.example.project3;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class OrderDTO {
    private Long id;
    private LocalDateTime orderDate;
    private BigDecimal totalAmount;
    private BigDecimal originalAmount;
    private BigDecimal discountAmount;
    private String couponCode;
    private String username;
    private String customerEmail;
    private List<OrderItemDTO> orderItems;
    private boolean fulfilled;
    private LocalDateTime fulfilledDate;
    private String orderNotes;
    private int totalItems;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(LocalDateTime orderDate) {
        this.orderDate = orderDate;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public List<OrderItemDTO> getOrderItems() {
        return orderItems;
    }

    public void setOrderItems(List<OrderItemDTO> orderItems) {
        this.orderItems = orderItems;
    }

    public boolean isFulfilled() {
        return fulfilled;
    }

    public void setFulfilled(boolean fulfilled) {
        this.fulfilled = fulfilled;
    }

    public BigDecimal getOriginalAmount() {
        return originalAmount;
    }

    public void setOriginalAmount(BigDecimal originalAmount) {
        this.originalAmount = originalAmount;
    }

    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(BigDecimal discountAmount) {
        this.discountAmount = discountAmount;
    }

    public String getCouponCode() {
        return couponCode;
    }

    public void setCouponCode(String couponCode) {
        this.couponCode = couponCode;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail;
    }

    public LocalDateTime getFulfilledDate() {
        return fulfilledDate;
    }

    public void setFulfilledDate(LocalDateTime fulfilledDate) {
        this.fulfilledDate = fulfilledDate;
    }

    public String getOrderNotes() {
        return orderNotes;
    }

    public void setOrderNotes(String orderNotes) {
        this.orderNotes = orderNotes;
    }

    public int getTotalItems() {
        return totalItems;
    }

    public void setTotalItems(int totalItems) {
        this.totalItems = totalItems;
    }

    // Utility methods
    public boolean hasDiscount() {
        return discountAmount != null && discountAmount.compareTo(BigDecimal.ZERO) > 0;
    }

    public BigDecimal getDiscountPercentage() {
        if (originalAmount != null && originalAmount.compareTo(BigDecimal.ZERO) > 0 && hasDiscount()) {
            return discountAmount.divide(originalAmount, 4, java.math.RoundingMode.HALF_UP)
                               .multiply(BigDecimal.valueOf(100));
        }
        return BigDecimal.ZERO;
    }

    public int calculateTotalItems() {
        if (orderItems != null) {
            return orderItems.stream().mapToInt(OrderItemDTO::getQuantity).sum();
        }
        return 0;
    }
} 