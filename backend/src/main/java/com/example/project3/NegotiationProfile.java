package com.example.project3;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "negotiation_profiles")
public class NegotiationProfile {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "customer_id", nullable = false)
    private Long customerId;
    
    @Column(name = "negotiation_style")
    private String negotiationStyle; // "aggressive", "reasonable", "passive", "strategic"
    
    @Column(name = "average_discount_requested", precision = 5, scale = 2)
    private BigDecimal averageDiscountRequested = BigDecimal.ZERO;
    
    @Column(name = "negotiation_success_rate", precision = 5, scale = 2)
    private BigDecimal negotiationSuccessRate = BigDecimal.ZERO;
    
    @ElementCollection
    @CollectionTable(name = "preferred_offer_types", joinColumns = @JoinColumn(name = "profile_id"))
    @Column(name = "offer_type")
    private List<String> preferredOfferTypes; // "percentage", "fixed_amount", "free_shipping", "bundle"
    
    @Column(name = "max_negotiations_per_month")
    private Integer maxNegotiationsPerMonth = 5;
    
    @Column(name = "blocked_from_negotiation")
    private Boolean blockedFromNegotiation = false;
    
    @Column(name = "block_reason")
    private String blockReason;
    
    @Column(name = "block_until_date")
    private LocalDateTime blockUntilDate;
    
    @Column(name = "typical_negotiation_time")
    private String typicalNegotiationTime; // "morning", "afternoon", "evening", "weekend"
    
    @Column(name = "responds_to_urgency")
    private Boolean respondsToUrgency = true; // Whether customer responds well to time-limited offers
    
    @Column(name = "price_anchor_sensitivity", precision = 3, scale = 2)
    private BigDecimal priceAnchorSensitivity = BigDecimal.valueOf(1.0); // How much they're influenced by initial prices
    
    @Column(name = "bundle_preference_score", precision = 3, scale = 2)
    private BigDecimal bundlePreferenceScore = BigDecimal.valueOf(0.5); // How much they like bundle offers
    
    @Column(name = "loyalty_influence_factor", precision = 3, scale = 2)
    private BigDecimal loyaltyInfluenceFactor = BigDecimal.valueOf(1.0); // How much loyalty messaging affects them
    
    @Column(name = "last_negotiation_outcome")
    private String lastNegotiationOutcome; // "accepted", "rejected", "counter_offered", "abandoned"
    
    @Column(name = "consecutive_rejections")
    private Integer consecutiveRejections = 0;
    
    @Column(name = "consecutive_acceptances")
    private Integer consecutiveAcceptances = 0;
    
    @Column(name = "profile_created_at")
    private LocalDateTime profileCreatedAt;
    
    @Column(name = "profile_updated_at")
    private LocalDateTime profileUpdatedAt;
    
    public NegotiationProfile() {
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
    
    public Long getCustomerId() {
        return customerId;
    }
    
    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }
    
    public String getNegotiationStyle() {
        return negotiationStyle;
    }
    
    public void setNegotiationStyle(String negotiationStyle) {
        this.negotiationStyle = negotiationStyle;
    }
    
    public BigDecimal getAverageDiscountRequested() {
        return averageDiscountRequested;
    }
    
    public void setAverageDiscountRequested(BigDecimal averageDiscountRequested) {
        this.averageDiscountRequested = averageDiscountRequested;
    }
    
    public BigDecimal getNegotiationSuccessRate() {
        return negotiationSuccessRate;
    }
    
    public void setNegotiationSuccessRate(BigDecimal negotiationSuccessRate) {
        this.negotiationSuccessRate = negotiationSuccessRate;
    }
    
    public List<String> getPreferredOfferTypes() {
        return preferredOfferTypes;
    }
    
    public void setPreferredOfferTypes(List<String> preferredOfferTypes) {
        this.preferredOfferTypes = preferredOfferTypes;
    }
    
    public Integer getMaxNegotiationsPerMonth() {
        return maxNegotiationsPerMonth;
    }
    
    public void setMaxNegotiationsPerMonth(Integer maxNegotiationsPerMonth) {
        this.maxNegotiationsPerMonth = maxNegotiationsPerMonth;
    }
    
    public Boolean getBlockedFromNegotiation() {
        return blockedFromNegotiation;
    }
    
    public void setBlockedFromNegotiation(Boolean blockedFromNegotiation) {
        this.blockedFromNegotiation = blockedFromNegotiation;
    }
    
    public String getBlockReason() {
        return blockReason;
    }
    
    public void setBlockReason(String blockReason) {
        this.blockReason = blockReason;
    }
    
    public LocalDateTime getBlockUntilDate() {
        return blockUntilDate;
    }
    
    public void setBlockUntilDate(LocalDateTime blockUntilDate) {
        this.blockUntilDate = blockUntilDate;
    }
    
    public String getTypicalNegotiationTime() {
        return typicalNegotiationTime;
    }
    
    public void setTypicalNegotiationTime(String typicalNegotiationTime) {
        this.typicalNegotiationTime = typicalNegotiationTime;
    }
    
    public Boolean getRespondsToUrgency() {
        return respondsToUrgency;
    }
    
    public void setRespondsToUrgency(Boolean respondsToUrgency) {
        this.respondsToUrgency = respondsToUrgency;
    }
    
    public BigDecimal getPriceAnchorSensitivity() {
        return priceAnchorSensitivity;
    }
    
    public void setPriceAnchorSensitivity(BigDecimal priceAnchorSensitivity) {
        this.priceAnchorSensitivity = priceAnchorSensitivity;
    }
    
    public BigDecimal getBundlePreferenceScore() {
        return bundlePreferenceScore;
    }
    
    public void setBundlePreferenceScore(BigDecimal bundlePreferenceScore) {
        this.bundlePreferenceScore = bundlePreferenceScore;
    }
    
    public BigDecimal getLoyaltyInfluenceFactor() {
        return loyaltyInfluenceFactor;
    }
    
    public void setLoyaltyInfluenceFactor(BigDecimal loyaltyInfluenceFactor) {
        this.loyaltyInfluenceFactor = loyaltyInfluenceFactor;
    }
    
    public String getLastNegotiationOutcome() {
        return lastNegotiationOutcome;
    }
    
    public void setLastNegotiationOutcome(String lastNegotiationOutcome) {
        this.lastNegotiationOutcome = lastNegotiationOutcome;
    }
    
    public Integer getConsecutiveRejections() {
        return consecutiveRejections;
    }
    
    public void setConsecutiveRejections(Integer consecutiveRejections) {
        this.consecutiveRejections = consecutiveRejections;
    }
    
    public Integer getConsecutiveAcceptances() {
        return consecutiveAcceptances;
    }
    
    public void setConsecutiveAcceptances(Integer consecutiveAcceptances) {
        this.consecutiveAcceptances = consecutiveAcceptances;
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
    
    // Utility methods
    public boolean isCurrentlyBlocked() {
        if (!blockedFromNegotiation) return false;
        if (blockUntilDate == null) return true; // Permanent block
        return LocalDateTime.now().isBefore(blockUntilDate);
    }
    
    public void recordNegotiationOutcome(String outcome) {
        this.lastNegotiationOutcome = outcome;
        
        if ("accepted".equals(outcome)) {
            this.consecutiveAcceptances++;
            this.consecutiveRejections = 0;
        } else if ("rejected".equals(outcome)) {
            this.consecutiveRejections++;
            this.consecutiveAcceptances = 0;
        } else {
            // Reset both for other outcomes
            this.consecutiveAcceptances = 0;
            this.consecutiveRejections = 0;
        }
        
        // Auto-block if too many consecutive rejections
        if (this.consecutiveRejections >= 5) {
            this.blockedFromNegotiation = true;
            this.blockReason = "Excessive rejections - appears to be abusing system";
            this.blockUntilDate = LocalDateTime.now().plusDays(7); // 1 week cooldown
        }
    }
    
    public void updateSuccessRate(BigDecimal newRate) {
        this.negotiationSuccessRate = newRate;
        this.profileUpdatedAt = LocalDateTime.now();
        
        // Adjust max negotiations based on success rate
        if (newRate.compareTo(BigDecimal.valueOf(80.0)) > 0) {
            this.maxNegotiationsPerMonth = Math.min(this.maxNegotiationsPerMonth + 1, 10);
        } else if (newRate.compareTo(BigDecimal.valueOf(20.0)) < 0) {
            this.maxNegotiationsPerMonth = Math.max(this.maxNegotiationsPerMonth - 1, 2);
        }
    }
    
    @PreUpdate
    public void preUpdate() {
        this.profileUpdatedAt = LocalDateTime.now();
    }
} 