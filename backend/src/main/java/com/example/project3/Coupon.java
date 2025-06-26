package com.example.project3;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "coupons")
public class Coupon {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String couponCode;
    
    @Enumerated(EnumType.STRING)
    private CouponType couponType;
    
    @Column(precision = 19, scale = 2)
    private BigDecimal discountValue;
    
    @Column(precision = 19, scale = 2)
    private BigDecimal minimumOrderValue = BigDecimal.ZERO;
    
    private LocalDateTime expirationDate;
    
    private Integer maxUses = 1;
    
    private Integer currentUses = 0;
    
    @Column(name = "customer_id")
    private Long customerId; // null for public coupons
    
    @Enumerated(EnumType.STRING)
    private CouponSource generatedBy;
    
    private Boolean isActive = true;
    
    private String applicableCategories; // JSON array of category names
    
    @Column(columnDefinition = "TEXT")
    private String restrictions; // JSON object with additional rules
    
    private LocalDateTime createdAt;
    
    private LocalDateTime usedAt;
    
    public enum CouponType {
        PERCENTAGE,
        FIXED_AMOUNT,
        FREE_SHIPPING,
        BOGO // Buy One Get One
    }
    
    public enum CouponSource {
        AI_NEGOTIATION,
        MANUAL_ADMIN,
        EMAIL_CAMPAIGN,
        LOYALTY_REWARD,
        WELCOME_BONUS
    }
    
    public Coupon() {
        this.createdAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getCouponCode() {
        return couponCode;
    }
    
    public void setCouponCode(String couponCode) {
        this.couponCode = couponCode;
    }
    
    public CouponType getCouponType() {
        return couponType;
    }
    
    public void setCouponType(CouponType couponType) {
        this.couponType = couponType;
    }
    
    public BigDecimal getDiscountValue() {
        return discountValue;
    }
    
    public void setDiscountValue(BigDecimal discountValue) {
        this.discountValue = discountValue;
    }
    
    public BigDecimal getMinimumOrderValue() {
        return minimumOrderValue;
    }
    
    public void setMinimumOrderValue(BigDecimal minimumOrderValue) {
        this.minimumOrderValue = minimumOrderValue;
    }
    
    public LocalDateTime getExpirationDate() {
        return expirationDate;
    }
    
    public void setExpirationDate(LocalDateTime expirationDate) {
        this.expirationDate = expirationDate;
    }
    
    public Integer getMaxUses() {
        return maxUses;
    }
    
    public void setMaxUses(Integer maxUses) {
        this.maxUses = maxUses;
    }
    
    public Integer getCurrentUses() {
        return currentUses;
    }
    
    public void setCurrentUses(Integer currentUses) {
        this.currentUses = currentUses;
    }
    
    public Long getCustomerId() {
        return customerId;
    }
    
    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }
    
    public CouponSource getGeneratedBy() {
        return generatedBy;
    }
    
    public void setGeneratedBy(CouponSource generatedBy) {
        this.generatedBy = generatedBy;
    }
    
    public Boolean getIsActive() {
        return isActive;
    }
    
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
    
    public String getApplicableCategories() {
        return applicableCategories;
    }
    
    public void setApplicableCategories(String applicableCategories) {
        this.applicableCategories = applicableCategories;
    }
    
    public String getRestrictions() {
        return restrictions;
    }
    
    public void setRestrictions(String restrictions) {
        this.restrictions = restrictions;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUsedAt() {
        return usedAt;
    }
    
    public void setUsedAt(LocalDateTime usedAt) {
        this.usedAt = usedAt;
    }
    
    // Utility methods
    public boolean isExpired() {
        return expirationDate != null && LocalDateTime.now().isAfter(expirationDate);
    }
    
    public boolean isUsageLimitReached() {
        return currentUses >= maxUses;
    }
    
    public boolean isValid() {
        return isActive && !isExpired() && !isUsageLimitReached();
    }
    
    public void incrementUsage() {
        this.currentUses++;
        if (this.usedAt == null) {
            this.usedAt = LocalDateTime.now();
        }
    }
} 