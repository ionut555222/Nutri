package com.example.project3;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "customer_profiles")
public class CustomerProfile {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;
    
    // Purchase Behavior Metrics
    @Column(name = "total_orders")
    private Integer totalOrders = 0;
    
    @Column(name = "total_spent", precision = 19, scale = 2)
    private BigDecimal totalSpent = BigDecimal.ZERO;
    
    @Column(name = "average_order_value", precision = 19, scale = 2)
    private BigDecimal averageOrderValue = BigDecimal.ZERO;
    
    @Column(name = "order_frequency_days")
    private Double orderFrequencyDays = 0.0;
    
    @Column(name = "last_order_date")
    private LocalDateTime lastOrderDate;
    
    @Column(name = "first_order_date")
    private LocalDateTime firstOrderDate;
    
    // Preferences and Behavior
    @ElementCollection
    @CollectionTable(name = "customer_favorite_categories", joinColumns = @JoinColumn(name = "profile_id"))
    @Column(name = "category_name")
    private List<String> favoriteCategories;
    
    @ElementCollection
    @CollectionTable(name = "customer_category_spending", joinColumns = @JoinColumn(name = "profile_id"))
    @MapKeyColumn(name = "category_name")
    @Column(name = "amount_spent")
    private Map<String, BigDecimal> categorySpending;
    
    // AI-generated insights
    @Column(name = "customer_segment")
    private String customerSegment; // "Premium", "Regular", "Budget", "New"
    
    @Column(name = "risk_level")
    private String riskLevel; // "Low", "Medium", "High" (churn risk)
    
    @Column(name = "preferred_communication_time")
    private String preferredCommunicationTime; // "Morning", "Afternoon", "Evening"
    
    @Column(name = "ai_personality_profile", columnDefinition = "TEXT")
    private String aiPersonalityProfile; // AI-generated customer personality description
    
    @Column(name = "seasonal_preferences", columnDefinition = "TEXT")
    private String seasonalPreferences; // AI-analyzed seasonal buying patterns
    
    @Column(name = "price_sensitivity")
    private String priceSensitivity; // "High", "Medium", "Low"
    
    // Email Marketing Metrics
    @Column(name = "emails_sent")
    private Integer emailsSent = 0;
    
    @Column(name = "emails_opened")
    private Integer emailsOpened = 0;
    
    @Column(name = "emails_clicked")
    private Integer emailsClicked = 0;
    
    @Column(name = "last_email_sent")
    private LocalDateTime lastEmailSent;
    
    @Column(name = "opt_out")
    private Boolean optOut = false;
    
    // Negotiation-specific fields
    @Column(name = "negotiation_tier")
    private String negotiationTier; // VIP_PLATINUM, VIP_GOLD, etc.
    
    @Column(name = "max_discount_percentage", precision = 5, scale = 2)
    private BigDecimal maxDiscountPercentage = BigDecimal.ZERO;
    
    @Column(name = "negotiation_attempts")
    private Integer negotiationAttempts = 0;
    
    @Column(name = "successful_negotiations")
    private Integer successfulNegotiations = 0;
    
    @Column(name = "last_negotiation_date")
    private LocalDateTime lastNegotiationDate;
    
    @Column(name = "total_savings_from_negotiations", precision = 19, scale = 2)
    private BigDecimal totalSavingsFromNegotiations = BigDecimal.ZERO;
    
    @Column(name = "negotiation_behavior")
    private String negotiationBehavior; // "aggressive", "reasonable", "passive"
    
    @Column(name = "eligible_for_automatic_discounts")
    private Boolean eligibleForAutomaticDiscounts = true;
    
    @Column(name = "monthly_negotiation_count")
    private Integer monthlyNegotiationCount = 0;
    
    @Column(name = "last_monthly_reset")
    private LocalDateTime lastMonthlyReset;
    
    // Timestamps
    @Column(name = "profile_created_at")
    private LocalDateTime profileCreatedAt;
    
    @Column(name = "profile_updated_at")
    private LocalDateTime profileUpdatedAt;
    
    public CustomerProfile() {
        this.profileCreatedAt = LocalDateTime.now();
        this.profileUpdatedAt = LocalDateTime.now();
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

    // Backward compatibility method
    public User getUser() {
        // This method exists for backward compatibility with existing code
        // In the restructured system, we use getCustomer() instead
        return null;
    }
    
    public void setUser(User user) {
        // This method exists for backward compatibility with existing code
        // In the restructured system, we use setCustomer() instead
    }
    
    public Integer getTotalOrders() {
        return totalOrders;
    }
    
    public void setTotalOrders(Integer totalOrders) {
        this.totalOrders = totalOrders;
    }
    
    public BigDecimal getTotalSpent() {
        return totalSpent;
    }
    
    public void setTotalSpent(BigDecimal totalSpent) {
        this.totalSpent = totalSpent;
    }
    
    public BigDecimal getAverageOrderValue() {
        return averageOrderValue;
    }
    
    public void setAverageOrderValue(BigDecimal averageOrderValue) {
        this.averageOrderValue = averageOrderValue;
    }
    
    public Double getOrderFrequencyDays() {
        return orderFrequencyDays;
    }
    
    public void setOrderFrequencyDays(Double orderFrequencyDays) {
        this.orderFrequencyDays = orderFrequencyDays;
    }
    
    public LocalDateTime getLastOrderDate() {
        return lastOrderDate;
    }
    
    public void setLastOrderDate(LocalDateTime lastOrderDate) {
        this.lastOrderDate = lastOrderDate;
    }
    
    public LocalDateTime getFirstOrderDate() {
        return firstOrderDate;
    }
    
    public void setFirstOrderDate(LocalDateTime firstOrderDate) {
        this.firstOrderDate = firstOrderDate;
    }
    
    public List<String> getFavoriteCategories() {
        return favoriteCategories;
    }
    
    public void setFavoriteCategories(List<String> favoriteCategories) {
        this.favoriteCategories = favoriteCategories;
    }
    
    public Map<String, BigDecimal> getCategorySpending() {
        return categorySpending;
    }
    
    public void setCategorySpending(Map<String, BigDecimal> categorySpending) {
        this.categorySpending = categorySpending;
    }
    
    public String getCustomerSegment() {
        return customerSegment;
    }
    
    public void setCustomerSegment(String customerSegment) {
        this.customerSegment = customerSegment;
    }
    
    public String getRiskLevel() {
        return riskLevel;
    }
    
    public void setRiskLevel(String riskLevel) {
        this.riskLevel = riskLevel;
    }
    
    public String getPreferredCommunicationTime() {
        return preferredCommunicationTime;
    }
    
    public void setPreferredCommunicationTime(String preferredCommunicationTime) {
        this.preferredCommunicationTime = preferredCommunicationTime;
    }
    
    public String getAiPersonalityProfile() {
        return aiPersonalityProfile;
    }
    
    public void setAiPersonalityProfile(String aiPersonalityProfile) {
        this.aiPersonalityProfile = aiPersonalityProfile;
    }
    
    public String getSeasonalPreferences() {
        return seasonalPreferences;
    }
    
    public void setSeasonalPreferences(String seasonalPreferences) {
        this.seasonalPreferences = seasonalPreferences;
    }
    
    public String getPriceSensitivity() {
        return priceSensitivity;
    }
    
    public void setPriceSensitivity(String priceSensitivity) {
        this.priceSensitivity = priceSensitivity;
    }
    
    public Integer getEmailsSent() {
        return emailsSent;
    }
    
    public void setEmailsSent(Integer emailsSent) {
        this.emailsSent = emailsSent;
    }
    
    public Integer getEmailsOpened() {
        return emailsOpened;
    }
    
    public void setEmailsOpened(Integer emailsOpened) {
        this.emailsOpened = emailsOpened;
    }
    
    public Integer getEmailsClicked() {
        return emailsClicked;
    }
    
    public void setEmailsClicked(Integer emailsClicked) {
        this.emailsClicked = emailsClicked;
    }
    
    public LocalDateTime getLastEmailSent() {
        return lastEmailSent;
    }
    
    public void setLastEmailSent(LocalDateTime lastEmailSent) {
        this.lastEmailSent = lastEmailSent;
    }
    
    public Boolean getOptOut() {
        return optOut;
    }
    
    public void setOptOut(Boolean optOut) {
        this.optOut = optOut;
    }
    
    public LocalDateTime getProfileCreatedAt() {
        return profileCreatedAt;
    }
    
    public void setProfileCreatedAt(LocalDateTime profileCreatedAt) {
        this.profileCreatedAt = profileCreatedAt;
    }
    
    public LocalDateTime getProfileUpdatedAt() {
        return profileUpdatedAt;
    }
    
    public void setProfileUpdatedAt(LocalDateTime profileUpdatedAt) {
        this.profileUpdatedAt = profileUpdatedAt;
    }
    
    // Negotiation-specific getters and setters
    public String getNegotiationTier() {
        return negotiationTier;
    }
    
    public void setNegotiationTier(String negotiationTier) {
        this.negotiationTier = negotiationTier;
    }
    
    public BigDecimal getMaxDiscountPercentage() {
        return maxDiscountPercentage;
    }
    
    public void setMaxDiscountPercentage(BigDecimal maxDiscountPercentage) {
        this.maxDiscountPercentage = maxDiscountPercentage;
    }
    
    public Integer getNegotiationAttempts() {
        return negotiationAttempts;
    }
    
    public void setNegotiationAttempts(Integer negotiationAttempts) {
        this.negotiationAttempts = negotiationAttempts;
    }
    
    public Integer getSuccessfulNegotiations() {
        return successfulNegotiations;
    }
    
    public void setSuccessfulNegotiations(Integer successfulNegotiations) {
        this.successfulNegotiations = successfulNegotiations;
    }
    
    public LocalDateTime getLastNegotiationDate() {
        return lastNegotiationDate;
    }
    
    public void setLastNegotiationDate(LocalDateTime lastNegotiationDate) {
        this.lastNegotiationDate = lastNegotiationDate;
    }
    
    public BigDecimal getTotalSavingsFromNegotiations() {
        return totalSavingsFromNegotiations;
    }
    
    public void setTotalSavingsFromNegotiations(BigDecimal totalSavingsFromNegotiations) {
        this.totalSavingsFromNegotiations = totalSavingsFromNegotiations;
    }
    
    public String getNegotiationBehavior() {
        return negotiationBehavior;
    }
    
    public void setNegotiationBehavior(String negotiationBehavior) {
        this.negotiationBehavior = negotiationBehavior;
    }
    
    public Boolean getEligibleForAutomaticDiscounts() {
        return eligibleForAutomaticDiscounts;
    }
    
    public void setEligibleForAutomaticDiscounts(Boolean eligibleForAutomaticDiscounts) {
        this.eligibleForAutomaticDiscounts = eligibleForAutomaticDiscounts;
    }
    
    public Integer getMonthlyNegotiationCount() {
        return monthlyNegotiationCount;
    }
    
    public void setMonthlyNegotiationCount(Integer monthlyNegotiationCount) {
        this.monthlyNegotiationCount = monthlyNegotiationCount;
    }
    
    public LocalDateTime getLastMonthlyReset() {
        return lastMonthlyReset;
    }
    
    public void setLastMonthlyReset(LocalDateTime lastMonthlyReset) {
        this.lastMonthlyReset = lastMonthlyReset;
    }
    
    // Utility methods
    public double getEmailOpenRate() {
        if (emailsSent == 0) return 0.0;
        return (double) emailsOpened / emailsSent * 100;
    }
    
    public double getEmailClickRate() {
        if (emailsSent == 0) return 0.0;
        return (double) emailsClicked / emailsSent * 100;
    }
    
    public double getNegotiationSuccessRate() {
        if (negotiationAttempts == 0) return 0.0;
        return (double) successfulNegotiations / negotiationAttempts * 100;
    }
    
    public void incrementNegotiationAttempt() {
        this.negotiationAttempts = (this.negotiationAttempts == null ? 0 : this.negotiationAttempts) + 1;
        this.monthlyNegotiationCount = (this.monthlyNegotiationCount == null ? 0 : this.monthlyNegotiationCount) + 1;
        this.lastNegotiationDate = LocalDateTime.now();
    }
    
    public void recordSuccessfulNegotiation(BigDecimal savingsAmount) {
        this.successfulNegotiations = (this.successfulNegotiations == null ? 0 : this.successfulNegotiations) + 1;
        this.totalSavingsFromNegotiations = (this.totalSavingsFromNegotiations == null ? BigDecimal.ZERO : this.totalSavingsFromNegotiations).add(savingsAmount);
    }
    
    public void resetMonthlyCountIfNeeded() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime currentMonthStart = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
        
        if (this.lastMonthlyReset == null || this.lastMonthlyReset.isBefore(currentMonthStart)) {
            this.monthlyNegotiationCount = 0;
            this.lastMonthlyReset = currentMonthStart;
        }
    }
    
    @PreUpdate
    public void preUpdate() {
        this.profileUpdatedAt = LocalDateTime.now();
        resetMonthlyCountIfNeeded();
    }
} 