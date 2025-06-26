package com.example.project3;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@Transactional
public class CustomerTierService {

    @Autowired
    private CustomerProfileService customerProfileService;
    
    @Autowired
    private OrderRepository orderRepository;

    public enum CustomerTier {
        VIP_PLATINUM(25, "VIP Platinum", BigDecimal.valueOf(2000), 50, 30),
        VIP_GOLD(20, "VIP Gold", BigDecimal.valueOf(1000), 25, 60),
        REGULAR_LOYAL(15, "Regular Loyal", BigDecimal.valueOf(500), 10, 90),
        REGULAR_ACTIVE(10, "Regular Active", BigDecimal.valueOf(200), 5, 120),
        BUDGET_CONSCIOUS(8, "Budget Conscious", BigDecimal.valueOf(0), 0, 365),
        NEW_CUSTOMER(12, "New Customer", BigDecimal.valueOf(0), 0, 30),
        AT_RISK(20, "At Risk", BigDecimal.valueOf(0), 0, 365);

        private final int maxDiscountPercentage;
        private final String displayName;
        private final BigDecimal minSpentThreshold;
        private final int minOrdersThreshold;
        private final int maxDaysSinceLastOrder;

        CustomerTier(int maxDiscountPercentage, String displayName, BigDecimal minSpentThreshold, 
                    int minOrdersThreshold, int maxDaysSinceLastOrder) {
            this.maxDiscountPercentage = maxDiscountPercentage;
            this.displayName = displayName;
            this.minSpentThreshold = minSpentThreshold;
            this.minOrdersThreshold = minOrdersThreshold;
            this.maxDaysSinceLastOrder = maxDaysSinceLastOrder;
        }

        public int getMaxDiscountPercentage() {
            return maxDiscountPercentage;
        }

        public String getDisplayName() {
            return displayName;
        }

        public BigDecimal getMinSpentThreshold() {
            return minSpentThreshold;
        }

        public int getMinOrdersThreshold() {
            return minOrdersThreshold;
        }

        public int getMaxDaysSinceLastOrder() {
            return maxDaysSinceLastOrder;
        }
    }

    public CustomerTier determineCustomerTier(Customer customer) {
        CustomerProfile profile = customerProfileService.getOrCreateProfile(customer.getId());
        
        BigDecimal totalSpent = profile.getTotalSpent();
        int totalOrders = profile.getTotalOrders();
        LocalDateTime lastOrderDate = profile.getLastOrderDate();
        LocalDateTime firstOrderDate = profile.getFirstOrderDate();
        
        // Calculate days since last order
        long daysSinceLastOrder = 0;
        if (lastOrderDate != null) {
            daysSinceLastOrder = ChronoUnit.DAYS.between(lastOrderDate, LocalDateTime.now());
        }
        
        // Calculate days since first order (customer age)
        long daysSinceFirstOrder = 0;
        if (firstOrderDate != null) {
            daysSinceFirstOrder = ChronoUnit.DAYS.between(firstOrderDate, LocalDateTime.now());
        }

        // Special case: At-risk customers (haven't ordered in 120+ days but have history)
        if (daysSinceLastOrder > 120 && totalOrders > 0) {
            return CustomerTier.AT_RISK;
        }

        // Special case: New customers (less than 30 days old or less than 3 orders)
        if (daysSinceFirstOrder < 30 || totalOrders < 3) {
            return CustomerTier.NEW_CUSTOMER;
        }

        // VIP Platinum: High value, frequent customers
        if (totalSpent.compareTo(CustomerTier.VIP_PLATINUM.getMinSpentThreshold()) >= 0 &&
            totalOrders >= CustomerTier.VIP_PLATINUM.getMinOrdersThreshold() &&
            daysSinceLastOrder <= CustomerTier.VIP_PLATINUM.getMaxDaysSinceLastOrder()) {
            return CustomerTier.VIP_PLATINUM;
        }

        // VIP Gold: Good value, regular customers
        if (totalSpent.compareTo(CustomerTier.VIP_GOLD.getMinSpentThreshold()) >= 0 &&
            totalOrders >= CustomerTier.VIP_GOLD.getMinOrdersThreshold() &&
            daysSinceLastOrder <= CustomerTier.VIP_GOLD.getMaxDaysSinceLastOrder()) {
            return CustomerTier.VIP_GOLD;
        }

        // Regular Loyal: Consistent customers
        if (totalSpent.compareTo(CustomerTier.REGULAR_LOYAL.getMinSpentThreshold()) >= 0 &&
            totalOrders >= CustomerTier.REGULAR_LOYAL.getMinOrdersThreshold() &&
            daysSinceLastOrder <= CustomerTier.REGULAR_LOYAL.getMaxDaysSinceLastOrder()) {
            return CustomerTier.REGULAR_LOYAL;
        }

        // Regular Active: Active customers
        if (totalSpent.compareTo(CustomerTier.REGULAR_ACTIVE.getMinSpentThreshold()) >= 0 &&
            totalOrders >= CustomerTier.REGULAR_ACTIVE.getMinOrdersThreshold() &&
            daysSinceLastOrder <= CustomerTier.REGULAR_ACTIVE.getMaxDaysSinceLastOrder()) {
            return CustomerTier.REGULAR_ACTIVE;
        }

        // Default: Budget conscious
        return CustomerTier.BUDGET_CONSCIOUS;
    }

    public NegotiationCapability calculateNegotiationCapability(Customer customer) {
        CustomerTier tier = determineCustomerTier(customer);
        CustomerProfile profile = customerProfileService.getOrCreateProfile(customer.getId());
        
        // Calculate additional factors
        BigDecimal averageOrderValue = profile.getAverageOrderValue();
        String priceSensitivity = profile.getPriceSensitivity();
        
        // Adjust max discount based on additional factors
        int adjustedMaxDiscount = tier.getMaxDiscountPercentage();
        
        // Boost discount for high-value customers
        if (averageOrderValue.compareTo(BigDecimal.valueOf(100)) > 0) {
            adjustedMaxDiscount += 2;
        }
        
        // Reduce discount for highly price-sensitive customers (they might abuse it)
        if ("High".equals(priceSensitivity)) {
            adjustedMaxDiscount = Math.max(adjustedMaxDiscount - 3, 5);
        }
        
        // Cap at reasonable maximum
        adjustedMaxDiscount = Math.min(adjustedMaxDiscount, 30);
        
        return new NegotiationCapability(
            tier,
            adjustedMaxDiscount,
            calculateNegotiationAttemptsRemaining(customer),
            determineNegotiationStrategy(tier, profile)
        );
    }

    private int calculateNegotiationAttemptsRemaining(Customer customer) {
        // Get current month's negotiation attempts
        LocalDateTime monthStart = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        
        // This would need to be implemented with a negotiation tracking system
        // For now, return a default based on tier
        CustomerTier tier = determineCustomerTier(customer);
        
        switch (tier) {
            case VIP_PLATINUM:
                return 10; // Premium customers get more attempts
            case VIP_GOLD:
                return 8;
            case REGULAR_LOYAL:
                return 6;
            case REGULAR_ACTIVE:
                return 5;
            case NEW_CUSTOMER:
                return 3; // Limited attempts for new customers
            case AT_RISK:
                return 8; // More attempts to retain them
            default:
                return 3;
        }
    }

    private String determineNegotiationStrategy(CustomerTier tier, CustomerProfile profile) {
        switch (tier) {
            case VIP_PLATINUM:
                return "HIGHLY_ACCOMMODATING"; // Almost always say yes, offer premium perks
            case VIP_GOLD:
                return "ACCOMMODATING"; // Usually say yes, offer good deals
            case REGULAR_LOYAL:
                return "STANDARD"; // Standard negotiation approach
            case REGULAR_ACTIVE:
                return "CAUTIOUS"; // More selective about offers
            case BUDGET_CONSCIOUS:
                return "VALUE_FOCUSED"; // Focus on value propositions
            case NEW_CUSTOMER:
                return "ACQUISITION_FOCUSED"; // Focus on converting to loyal customer
            case AT_RISK:
                return "RETENTION_FOCUSED"; // Focus on winning them back
            default:
                return "STANDARD";
        }
    }

    public static class NegotiationCapability {
        private final CustomerTier tier;
        private final int maxDiscountPercentage;
        private final int negotiationAttemptsRemaining;
        private final String negotiationStrategy;

        public NegotiationCapability(CustomerTier tier, int maxDiscountPercentage, 
                                   int negotiationAttemptsRemaining, String negotiationStrategy) {
            this.tier = tier;
            this.maxDiscountPercentage = maxDiscountPercentage;
            this.negotiationAttemptsRemaining = negotiationAttemptsRemaining;
            this.negotiationStrategy = negotiationStrategy;
        }

        public CustomerTier getTier() {
            return tier;
        }

        public int getMaxDiscountPercentage() {
            return maxDiscountPercentage;
        }

        public int getNegotiationAttemptsRemaining() {
            return negotiationAttemptsRemaining;
        }

        public String getNegotiationStrategy() {
            return negotiationStrategy;
        }

        public boolean canNegotiate() {
            return negotiationAttemptsRemaining > 0;
        }
    }
} 