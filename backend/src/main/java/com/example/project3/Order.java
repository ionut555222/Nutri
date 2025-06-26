package com.example.project3;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    @JsonBackReference
    private Customer customer;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JsonManagedReference
    private Set<OrderItem> orderItems = new HashSet<>();

    private LocalDateTime orderDate;

    private BigDecimal totalAmount;
    
    private BigDecimal originalAmount = BigDecimal.ZERO; // Amount before any discounts
    
    private BigDecimal discountAmount = BigDecimal.ZERO; // Total discount applied
    
    private String couponCode; // Applied coupon code
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coupon_id")
    private Coupon appliedCoupon; // Reference to the applied coupon

    private boolean fulfilled = false;

    private LocalDateTime fulfilledDate;

    private LocalDateTime deliveryDate; // Expected delivery date
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "delivery_zone_id")
    private DeliveryZone deliveryZone; // Assigned delivery zone

    private String orderNotes;

    public Order() {
        this.orderDate = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    // Backward compatibility methods
    public User getUser() {
        // Deprecated - use getCustomer() instead
        return null;
    }

    public void setUser(User user) {
        // Deprecated - use setCustomer() instead
    }

    public Set<OrderItem> getOrderItems() {
        return orderItems;
    }

    public void setOrderItems(Set<OrderItem> orderItems) {
        this.orderItems = orderItems;
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

    public boolean isFulfilled() {
        return fulfilled;
    }

    public void setFulfilled(boolean fulfilled) {
        this.fulfilled = fulfilled;
        if (fulfilled && this.fulfilledDate == null) {
            this.fulfilledDate = LocalDateTime.now();
        }
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
    
    public Coupon getAppliedCoupon() {
        return appliedCoupon;
    }
    
    public void setAppliedCoupon(Coupon appliedCoupon) {
        this.appliedCoupon = appliedCoupon;
        if (appliedCoupon != null) {
            this.couponCode = appliedCoupon.getCouponCode();
        }
    }
    
    public LocalDateTime getDeliveryDate() {
        return deliveryDate;
    }
    
    public void setDeliveryDate(LocalDateTime deliveryDate) {
        this.deliveryDate = deliveryDate;
    }
    
    public DeliveryZone getDeliveryZone() {
        return deliveryZone;
    }
    
    public void setDeliveryZone(DeliveryZone deliveryZone) {
        this.deliveryZone = deliveryZone;
    }
    
    // Utility methods
    public BigDecimal calculateDiscountPercentage() {
        if (originalAmount == null || originalAmount.signum() == 0) {
            return BigDecimal.ZERO;
        }
        return discountAmount.divide(originalAmount, 4, java.math.RoundingMode.HALF_UP)
                           .multiply(BigDecimal.valueOf(100));
    }
    
    public boolean hasDiscount() {
        return discountAmount != null && discountAmount.signum() > 0;
    }
} 