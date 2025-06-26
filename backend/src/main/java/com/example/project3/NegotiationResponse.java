package com.example.project3;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class NegotiationResponse {
    
    private String aiResponse;
    private boolean offerMade;
    private Coupon generatedCoupon;
    private BigDecimal discountPercentage;
    private BigDecimal discountAmount;
    private String offerType; // "percentage_discount", "fixed_discount", "free_shipping", "bundle", "alternative"
    private List<String> alternativeOffers;
    private LocalDateTime offerExpirationTime;
    private String negotiationOutcome; // "offer_made", "offer_declined", "alternative_suggested", "not_eligible"
    private String customerTier;
    private int negotiationAttemptsRemaining;
    private boolean eligibleForFutureNegotiations;
    
    public NegotiationResponse() {}
    
    public NegotiationResponse(String aiResponse, boolean offerMade) {
        this.aiResponse = aiResponse;
        this.offerMade = offerMade;
    }
    
    public NegotiationResponse(String aiResponse, Coupon generatedCoupon, String negotiationOutcome) {
        this.aiResponse = aiResponse;
        this.generatedCoupon = generatedCoupon;
        this.offerMade = generatedCoupon != null;
        this.negotiationOutcome = negotiationOutcome;
        
        if (generatedCoupon != null) {
            this.discountPercentage = generatedCoupon.getDiscountValue();
            this.offerType = generatedCoupon.getCouponType().name().toLowerCase();
            this.offerExpirationTime = generatedCoupon.getExpirationDate();
        }
    }
    
    // Getters and Setters
    public String getAiResponse() {
        return aiResponse;
    }
    
    public void setAiResponse(String aiResponse) {
        this.aiResponse = aiResponse;
    }
    
    public boolean isOfferMade() {
        return offerMade;
    }
    
    public void setOfferMade(boolean offerMade) {
        this.offerMade = offerMade;
    }
    
    public Coupon getGeneratedCoupon() {
        return generatedCoupon;
    }
    
    public void setGeneratedCoupon(Coupon generatedCoupon) {
        this.generatedCoupon = generatedCoupon;
        if (generatedCoupon != null) {
            this.offerMade = true;
            this.discountPercentage = generatedCoupon.getDiscountValue();
            this.offerExpirationTime = generatedCoupon.getExpirationDate();
        }
    }
    
    public BigDecimal getDiscountPercentage() {
        return discountPercentage;
    }
    
    public void setDiscountPercentage(BigDecimal discountPercentage) {
        this.discountPercentage = discountPercentage;
    }
    
    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }
    
    public void setDiscountAmount(BigDecimal discountAmount) {
        this.discountAmount = discountAmount;
    }
    
    public String getOfferType() {
        return offerType;
    }
    
    public void setOfferType(String offerType) {
        this.offerType = offerType;
    }
    
    public List<String> getAlternativeOffers() {
        return alternativeOffers;
    }
    
    public void setAlternativeOffers(List<String> alternativeOffers) {
        this.alternativeOffers = alternativeOffers;
    }
    
    public LocalDateTime getOfferExpirationTime() {
        return offerExpirationTime;
    }
    
    public void setOfferExpirationTime(LocalDateTime offerExpirationTime) {
        this.offerExpirationTime = offerExpirationTime;
    }
    
    public String getNegotiationOutcome() {
        return negotiationOutcome;
    }
    
    public void setNegotiationOutcome(String negotiationOutcome) {
        this.negotiationOutcome = negotiationOutcome;
    }
    
    public String getCustomerTier() {
        return customerTier;
    }
    
    public void setCustomerTier(String customerTier) {
        this.customerTier = customerTier;
    }
    
    public int getNegotiationAttemptsRemaining() {
        return negotiationAttemptsRemaining;
    }
    
    public void setNegotiationAttemptsRemaining(int negotiationAttemptsRemaining) {
        this.negotiationAttemptsRemaining = negotiationAttemptsRemaining;
    }
    
    public boolean isEligibleForFutureNegotiations() {
        return eligibleForFutureNegotiations;
    }
    
    public void setEligibleForFutureNegotiations(boolean eligibleForFutureNegotiations) {
        this.eligibleForFutureNegotiations = eligibleForFutureNegotiations;
    }
    
    // Utility methods
    public String getCouponCode() {
        return generatedCoupon != null ? generatedCoupon.getCouponCode() : null;
    }
    
    public boolean hasValidOffer() {
        return offerMade && generatedCoupon != null && generatedCoupon.isValid();
    }
    
    public String getOfferSummary() {
        if (!offerMade || generatedCoupon == null) {
            return "No offer made";
        }
        
        String summary = "";
        switch (generatedCoupon.getCouponType()) {
            case PERCENTAGE:
                summary = discountPercentage + "% discount";
                break;
            case FIXED_AMOUNT:
                summary = "$" + discountPercentage + " off";
                break;
            case FREE_SHIPPING:
                summary = "Free shipping";
                break;
            default:
                summary = "Special offer";
        }
        
        if (offerExpirationTime != null) {
            summary += " (expires " + offerExpirationTime.toString() + ")";
        }
        
        return summary;
    }
} 