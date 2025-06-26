package com.example.project3;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class AINegotiationService {

    @Autowired
    private CustomerTierService customerTierService;
    
    @Autowired
    private CustomerProfileService customerProfileService;
    
    @Autowired
    private CouponService couponService;
    
    @Autowired
    private GeminiService geminiService;
    
    @Autowired
    private NegotiationProfileRepository negotiationProfileRepository;
    
    @Autowired
    private FruitRepository fruitRepository;

    public NegotiationResponse processNegotiationRequest(Customer customer, NegotiationRequest request) {
        try {
            // 1. Build negotiation context
            NegotiationContext context = buildNegotiationContext(customer, request);
            
            // 2. Check eligibility
            if (!context.canNegotiate()) {
                return createIneligibleResponse(context);
            }
            
            // 3. Determine negotiation strategy
            NegotiationStrategy strategy = determineNegotiationStrategy(context);
            
            // 4. Generate AI response
            String aiResponse = generateAINegotiationResponse(context, strategy);
            
            // 5. Create coupon if offer is being made
            Coupon generatedCoupon = null;
            if (strategy.shouldMakeOffer()) {
                generatedCoupon = createNegotiationCoupon(customer, strategy, context.getCartValue());
            }
            
            // 6. Update customer profiles
            updateCustomerProfiles(customer, context, strategy, generatedCoupon != null);
            
            // 7. Build response
            return buildNegotiationResponse(aiResponse, generatedCoupon, strategy, context);
            
        } catch (Exception e) {
            // Log error and return graceful failure response
            return createErrorResponse(e.getMessage());
        }
    }

    private NegotiationContext buildNegotiationContext(Customer customer, NegotiationRequest request) {
        NegotiationContext context = new NegotiationContext();
        
        // Customer information
        context.setCustomer(customer);
        context.setCustomerProfile(customerProfileService.getOrCreateProfile(customer.getId()));
        context.setNegotiationProfile(getOrCreateNegotiationProfile(customer.getId()));
        
        // Tier and capability calculation
        CustomerTierService.CustomerTier tier = customerTierService.determineCustomerTier(customer);
        context.setCustomerTier(tier);
        context.setNegotiationCapability(customerTierService.calculateNegotiationCapability(customer));
        
        // Request information
        context.setCartValue(request.getCartValue());
        context.setNegotiationMessage(request.getMessage());
        context.setNegotiationChannel("chat");
        
        // Convert cart items
        if (request.getCartItems() != null) {
            List<NegotiationContext.CartItemInfo> cartItemInfos = new ArrayList<>();
            for (NegotiationRequest.CartItem item : request.getCartItems()) {
                // Get product details for margin calculation
                Optional<Fruit> fruit = fruitRepository.findById(item.getProductId());
                BigDecimal margin = fruit.map(f -> calculateMargin(f, item.getUnitPrice())).orElse(BigDecimal.valueOf(30)); // Default 30% margin
                
                cartItemInfos.add(new NegotiationContext.CartItemInfo(
                    item.getProductName(),
                    fruit.map(f -> f.getCategory() != null ? f.getCategory().getName() : "Unknown").orElse("Unknown"),
                    item.getQuantity(),
                    item.getUnitPrice(),
                    item.getTotalPrice(),
                    margin
                ));
            }
            context.setCartItems(cartItemInfos);
        }
        
        // Behavioral context
        CustomerProfile profile = context.getCustomerProfile();
        context.setRepeatNegotiator(profile.getNegotiationAttempts() != null && profile.getNegotiationAttempts() > 0);
        context.setHasRecentPurchase(profile.getLastOrderDate() != null && 
                                    ChronoUnit.DAYS.between(profile.getLastOrderDate(), LocalDateTime.now()) <= 30);
        
        profile.resetMonthlyCountIfNeeded();
        context.setNegotiationAttemptsThisMonth(profile.getMonthlyNegotiationCount());
        
        // Market context (simplified for now)
        context.setSeasonalPeak(isSeasonalPeak());
        context.setHasInventoryPressure(false); // Could be enhanced with real inventory data
        
        return context;
    }

    private NegotiationStrategy determineNegotiationStrategy(NegotiationContext context) {
        NegotiationStrategy strategy = new NegotiationStrategy();
        
        CustomerTierService.NegotiationCapability capability = context.getNegotiationCapability();
        String strategyType = capability.getNegotiationStrategy();
        
        // Base discount calculation
        int baseMaxDiscount = capability.getMaxDiscountPercentage();
        
        // Adjust based on context
        int adjustedDiscount = baseMaxDiscount;
        
        // Cart value adjustments
        if (context.getCartValue().compareTo(BigDecimal.valueOf(100)) > 0) {
            adjustedDiscount += 2; // Bonus for large orders
        }
        
        // Loyalty adjustments
        if (context.getCustomerProfile().getTotalSpent().compareTo(BigDecimal.valueOf(1000)) > 0) {
            adjustedDiscount += 1; // Bonus for high lifetime value
        }
        
        // Recent purchase penalty (to prevent abuse)
        if (context.isHasRecentPurchase() && context.getNegotiationAttemptsThisMonth() > 2) {
            adjustedDiscount -= 2;
        }
        
        // Seasonal adjustments
        if (context.isSeasonalPeak()) {
            adjustedDiscount -= 1; // Less generous during peak times
        }
        
        // Cap the discount
        adjustedDiscount = Math.max(5, Math.min(adjustedDiscount, 30));
        
        strategy.setMaxDiscountPercentage(adjustedDiscount);
        strategy.setStrategyType(strategyType);
        strategy.setShouldMakeOffer(adjustedDiscount >= 5);
        strategy.setOfferType("percentage_discount");
        
        // Determine minimum cart value for discount
        BigDecimal minimumCartValue = BigDecimal.valueOf(25); // Default minimum
        if (context.getCustomerTier() == CustomerTierService.CustomerTier.VIP_PLATINUM) {
            minimumCartValue = BigDecimal.valueOf(15); // Lower minimum for VIP
        }
        strategy.setMinimumCartValue(minimumCartValue);
        
        return strategy;
    }

    private String generateAINegotiationResponse(NegotiationContext context, NegotiationStrategy strategy) {
        try {
            // Build enhanced system prompt for negotiation
            String systemPrompt = buildNegotiationSystemPrompt(context, strategy);
            
            // Create a chat request with the negotiation context
            ChatRequest chatRequest = new ChatRequest();
            chatRequest.setMessage(context.getNegotiationMessage());
            chatRequest.setHistory(new ArrayList<>()); // Empty history for negotiation
            
            // Get AI response
            return geminiService.getResponse(context.getCustomer().getUsername(), chatRequest);
            
        } catch (Exception e) {
            // Fallback response if AI fails
            return generateFallbackResponse(context, strategy);
        }
    }

    private String buildNegotiationSystemPrompt(NegotiationContext context, NegotiationStrategy strategy) {
        return String.format("""
            SYSTEM INSTRUCTION: You are Nutri, an AI sales negotiator for a premium fruit store. You're friendly, professional, and customer-focused.
            
            CUSTOMER CONTEXT:
            - Name: %s
            - Tier: %s (%s)
            - Total Spent: $%s | Orders: %d
            - Cart Value: $%s
            - Cart Items: %s
            - Negotiation Strategy: %s
            - Max Discount Available: %d%%
            - Attempts This Month: %d
            
            NEGOTIATION RULES:
            1. Always acknowledge customer loyalty/status appropriately
            2. If discount is within limits: Generate specific coupon code format NEG######
            3. If request exceeds limits: Offer maximum possible + explain value
            4. If no discount possible: Suggest alternatives (bundles, free shipping, loyalty points)
            5. Create urgency with 48-hour expiration
            6. Always end with clear call to action
            
            PERSONALITY BY STRATEGY:
            - HIGHLY_ACCOMMODATING: Almost always say yes, very generous, use premium language
            - ACCOMMODATING: Usually say yes, good offers, friendly and helpful
            - STANDARD: Balanced approach, fair offers, professional
            - CAUTIOUS: More selective, smaller offers, focus on value
            - VALUE_FOCUSED: Emphasize value over price, suggest alternatives
            - ACQUISITION_FOCUSED: Focus on building relationship, educational
            - RETENTION_FOCUSED: Generous to win back customer, acknowledge absence
            
            OFFER DETAILS:
            - %s
            - Coupon Code Format: NEG + 6 random characters
            - Expiration: 48 hours from now
            - Minimum Order: $%s
            
            Be conversational, use emojis sparingly, and make customers feel valued!
            """,
            context.getCustomer().getUsername(),
            context.getCustomerTierDisplayName(),
            context.getCustomerTier().name(),
            context.getCustomerProfile().getTotalSpent(),
            context.getCustomerProfile().getTotalOrders(),
            context.getCartValue(),
            context.getCartSummary(),
            strategy.getStrategyType(),
            strategy.getMaxDiscountPercentage(),
            context.getNegotiationAttemptsThisMonth(),
            strategy.shouldMakeOffer() ? 
                String.format("MAKE OFFER: %d%% discount", strategy.getMaxDiscountPercentage()) : 
                "DO NOT MAKE DISCOUNT OFFER - suggest alternatives",
            strategy.getMinimumCartValue()
        );
    }

    private String generateFallbackResponse(NegotiationContext context, NegotiationStrategy strategy) {
        if (strategy.shouldMakeOffer()) {
            return String.format("""
                Hi %s! Thanks for reaching out about pricing. 
                
                As a valued %s customer, I can offer you a %d%% discount on your current order. 
                
                Use code NEG123456 at checkout - this offer expires in 48 hours!
                
                Would you like to proceed with this exclusive offer?
                """,
                context.getCustomer().getUsername(),
                context.getCustomerTierDisplayName(),
                strategy.getMaxDiscountPercentage()
            );
        } else {
            return String.format("""
                Hi %s! I appreciate you reaching out.
                
                While I can't offer a discount on your current order, I have some great alternatives:
                - Free shipping on orders over $50
                - Loyalty points that add up to future savings
                - Bundle deals that give you more value
                
                Would any of these options work better for you?
                """,
                context.getCustomer().getUsername()
            );
        }
    }

    private Coupon createNegotiationCoupon(Customer customer, NegotiationStrategy strategy, BigDecimal cartValue) {
        return couponService.generateNegotiationCoupon(
            customer,
            BigDecimal.valueOf(strategy.getMaxDiscountPercentage()),
            strategy.getMinimumCartValue(),
            "AI Negotiation - " + strategy.getStrategyType()
        );
    }

    private void updateCustomerProfiles(Customer customer, NegotiationContext context, 
                                      NegotiationStrategy strategy, boolean offerMade) {
        // Update CustomerProfile
        CustomerProfile profile = context.getCustomerProfile();
        profile.incrementNegotiationAttempt();
        
        if (offerMade) {
            profile.recordSuccessfulNegotiation(BigDecimal.ZERO); // Will be updated when coupon is used
        }
        
        // Update tier information
        profile.setNegotiationTier(context.getCustomerTier().name());
        profile.setMaxDiscountPercentage(BigDecimal.valueOf(strategy.getMaxDiscountPercentage()));
        
        customerProfileService.updateProfileFromOrders(customer.getId());
        
        // Update NegotiationProfile
        NegotiationProfile negProfile = context.getNegotiationProfile();
        negProfile.recordNegotiationOutcome(offerMade ? "offer_made" : "no_offer");
        negotiationProfileRepository.save(negProfile);
    }

    private NegotiationResponse buildNegotiationResponse(String aiResponse, Coupon generatedCoupon, 
                                                       NegotiationStrategy strategy, NegotiationContext context) {
        NegotiationResponse response = new NegotiationResponse(aiResponse, generatedCoupon, 
                                                              generatedCoupon != null ? "offer_made" : "no_offer");
        
        response.setCustomerTier(context.getCustomerTierDisplayName());
        response.setNegotiationAttemptsRemaining(
            context.getNegotiationCapability().getNegotiationAttemptsRemaining() - 1);
        response.setEligibleForFutureNegotiations(
            context.getNegotiationCapability().getNegotiationAttemptsRemaining() > 1);
        
        if (generatedCoupon == null && strategy.shouldMakeOffer()) {
            // Suggest alternatives
            response.setAlternativeOffers(Arrays.asList(
                "Free shipping on your next order",
                "10% off when you spend $75 or more",
                "Double loyalty points on this purchase"
            ));
        }
        
        return response;
    }

    private NegotiationResponse createIneligibleResponse(NegotiationContext context) {
        String reason = "";
        if (context.getNegotiationProfile() != null && context.getNegotiationProfile().isCurrentlyBlocked()) {
            reason = "You've reached your negotiation limit. Please try again later.";
        } else if (!context.getNegotiationCapability().canNegotiate()) {
            reason = "You've reached your monthly negotiation limit.";
        } else {
            reason = "Negotiation is not available at this time.";
        }
        
        return new NegotiationResponse(
            "I understand you'd like to discuss pricing. " + reason + 
            " However, I'm here to help with any other questions you might have!",
            false
        );
    }

    private NegotiationResponse createErrorResponse(String errorMessage) {
        return new NegotiationResponse(
            "I apologize, but I'm having trouble processing your request right now. " +
            "Please try again in a moment, or contact our customer service team for assistance.",
            false
        );
    }

    private NegotiationProfile getOrCreateNegotiationProfile(Long customerId) {
        return negotiationProfileRepository.findByCustomerId(customerId)
                .orElseGet(() -> {
                    NegotiationProfile profile = new NegotiationProfile();
                    profile.setCustomerId(customerId);
                    profile.setNegotiationStyle("reasonable");
                    return negotiationProfileRepository.save(profile);
                });
    }

    private BigDecimal calculateMargin(Fruit fruit, BigDecimal sellingPrice) {
        // Simplified margin calculation - in real implementation, this would use cost data
        BigDecimal assumedCost = sellingPrice.multiply(BigDecimal.valueOf(0.7)); // Assume 30% margin
        return sellingPrice.subtract(assumedCost).divide(sellingPrice, 2, RoundingMode.HALF_UP)
                          .multiply(BigDecimal.valueOf(100));
    }

    private boolean isSeasonalPeak() {
        // Simplified seasonal detection - could be enhanced with real data
        int month = LocalDateTime.now().getMonthValue();
        return month == 12 || month == 1 || month == 6 || month == 7; // Winter holidays and summer
    }

    // Helper class for negotiation strategy
    private static class NegotiationStrategy {
        private int maxDiscountPercentage;
        private String strategyType;
        private boolean shouldMakeOffer;
        private String offerType;
        private BigDecimal minimumCartValue;

        public int getMaxDiscountPercentage() {
            return maxDiscountPercentage;
        }

        public void setMaxDiscountPercentage(int maxDiscountPercentage) {
            this.maxDiscountPercentage = maxDiscountPercentage;
        }

        public String getStrategyType() {
            return strategyType;
        }

        public void setStrategyType(String strategyType) {
            this.strategyType = strategyType;
        }

        public boolean shouldMakeOffer() {
            return shouldMakeOffer;
        }

        public void setShouldMakeOffer(boolean shouldMakeOffer) {
            this.shouldMakeOffer = shouldMakeOffer;
        }

        public String getOfferType() {
            return offerType;
        }

        public void setOfferType(String offerType) {
            this.offerType = offerType;
        }

        public BigDecimal getMinimumCartValue() {
            return minimumCartValue;
        }

        public void setMinimumCartValue(BigDecimal minimumCartValue) {
            this.minimumCartValue = minimumCartValue;
        }
    }
} 