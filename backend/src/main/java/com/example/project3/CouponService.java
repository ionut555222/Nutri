package com.example.project3;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class CouponService {

    @Autowired
    private CouponRepository couponRepository;

    @Autowired
    private CustomerTierService customerTierService;

    private static final String COUPON_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final SecureRandom random = new SecureRandom();

    public Coupon generateNegotiationCoupon(Customer customer, BigDecimal discountPercentage, 
                                          BigDecimal minimumOrderValue, String context) {
        
        // Generate unique coupon code
        String couponCode = generateUniqueCouponCode("NEG");
        
        Coupon coupon = new Coupon();
        coupon.setCouponCode(couponCode);
        coupon.setCouponType(Coupon.CouponType.PERCENTAGE);
        coupon.setDiscountValue(discountPercentage);
        coupon.setMinimumOrderValue(minimumOrderValue);
        coupon.setCustomerId(customer.getId());
        coupon.setGeneratedBy(Coupon.CouponSource.AI_NEGOTIATION);
        
        // Set expiration to 48 hours from now
        coupon.setExpirationDate(LocalDateTime.now().plusHours(48));
        
        // Single use for negotiation coupons
        coupon.setMaxUses(1);
        
        // Add context as restrictions
        coupon.setRestrictions(String.format("{\"context\": \"%s\", \"negotiated\": true}", context));
        
        return couponRepository.save(coupon);
    }

    public Coupon generateWelcomeCoupon(Customer customer) {
        String couponCode = generateUniqueCouponCode("WELCOME");
        
        Coupon coupon = new Coupon();
        coupon.setCouponCode(couponCode);
        coupon.setCouponType(Coupon.CouponType.PERCENTAGE);
        coupon.setDiscountValue(BigDecimal.valueOf(15)); // 15% welcome discount
        coupon.setMinimumOrderValue(BigDecimal.valueOf(25)); // Minimum $25 order
        coupon.setCustomerId(customer.getId());
        coupon.setGeneratedBy(Coupon.CouponSource.WELCOME_BONUS);
        
        // Set expiration to 30 days
        coupon.setExpirationDate(LocalDateTime.now().plusDays(30));
        coupon.setMaxUses(1);
        
        return couponRepository.save(coupon);
    }

    public Coupon generateRetentionCoupon(Customer customer, BigDecimal discountPercentage) {
        String couponCode = generateUniqueCouponCode("BACK");
        
        Coupon coupon = new Coupon();
        coupon.setCouponCode(couponCode);
        coupon.setCouponType(Coupon.CouponType.PERCENTAGE);
        coupon.setDiscountValue(discountPercentage);
        coupon.setMinimumOrderValue(BigDecimal.valueOf(30));
        coupon.setCustomerId(customer.getId());
        coupon.setGeneratedBy(Coupon.CouponSource.LOYALTY_REWARD);
        
        // Set expiration to 14 days (urgency for at-risk customers)
        coupon.setExpirationDate(LocalDateTime.now().plusDays(14));
        coupon.setMaxUses(1);
        
        return couponRepository.save(coupon);
    }

    public Optional<Coupon> validateCoupon(String couponCode, Customer customer, BigDecimal orderTotal) {
        Optional<Coupon> couponOpt = couponRepository.findValidCouponByCode(couponCode, LocalDateTime.now());
        
        if (couponOpt.isEmpty()) {
            return Optional.empty();
        }
        
        Coupon coupon = couponOpt.get();
        
        // Check if coupon is customer-specific
        if (coupon.getCustomerId() != null && !coupon.getCustomerId().equals(customer.getId())) {
            return Optional.empty();
        }
        
        // Check minimum order value
        if (orderTotal.compareTo(coupon.getMinimumOrderValue()) < 0) {
            return Optional.empty();
        }
        
        // Additional validation logic can be added here
        // (category restrictions, etc.)
        
        return Optional.of(coupon);
    }

    public BigDecimal calculateDiscount(Coupon coupon, BigDecimal orderTotal) {
        switch (coupon.getCouponType()) {
            case PERCENTAGE:
                return orderTotal.multiply(coupon.getDiscountValue())
                                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            case FIXED_AMOUNT:
                return coupon.getDiscountValue().min(orderTotal);
            case FREE_SHIPPING:
                // This would need to be handled differently in the order calculation
                return BigDecimal.ZERO;
            default:
                return BigDecimal.ZERO;
        }
    }

    public void applyCoupon(Coupon coupon) {
        coupon.incrementUsage();
        couponRepository.save(coupon);
    }

    public List<Coupon> getValidCouponsForCustomer(Customer customer) {
        return couponRepository.findValidCouponsByCustomerId(customer.getId(), LocalDateTime.now());
    }

    public boolean hasNegotiationCouponsThisMonth(Customer customer) {
        LocalDateTime monthStart = LocalDateTime.now().withDayOfMonth(1)
                                                     .withHour(0).withMinute(0).withSecond(0);
        Long count = couponRepository.countNegotiationCouponsThisMonth(customer.getId(), monthStart);
        
        // Limit based on customer tier
        CustomerTierService.CustomerTier tier = customerTierService.determineCustomerTier(customer);
        int monthlyLimit = getMonthlyNegotiationCouponLimit(tier);
        
        return count >= monthlyLimit;
    }

    private int getMonthlyNegotiationCouponLimit(CustomerTierService.CustomerTier tier) {
        switch (tier) {
            case VIP_PLATINUM:
                return 8;
            case VIP_GOLD:
                return 6;
            case REGULAR_LOYAL:
                return 4;
            case REGULAR_ACTIVE:
                return 3;
            case NEW_CUSTOMER:
                return 2;
            case AT_RISK:
                return 5; // More opportunities for at-risk customers
            case BUDGET_CONSCIOUS:
            default:
                return 2;
        }
    }

    private String generateUniqueCouponCode(String prefix) {
        String code;
        int attempts = 0;
        
        do {
            code = prefix + generateRandomString(6);
            attempts++;
            
            if (attempts > 10) {
                // Fallback to timestamp-based code if too many collisions
                code = prefix + System.currentTimeMillis() % 1000000;
                break;
            }
        } while (couponRepository.findByCouponCode(code).isPresent());
        
        return code;
    }

    private String generateRandomString(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(COUPON_CHARS.charAt(random.nextInt(COUPON_CHARS.length())));
        }
        return sb.toString();
    }

    // Cleanup expired coupons (could be scheduled)
    public void deactivateExpiredCoupons() {
        List<Coupon> expiredCoupons = couponRepository.findExpiredActiveCoupons(LocalDateTime.now());
        for (Coupon coupon : expiredCoupons) {
            coupon.setIsActive(false);
        }
        couponRepository.saveAll(expiredCoupons);
    }

    // Analytics methods
    public CouponUsageStats getCouponUsageStats(LocalDateTime startDate, LocalDateTime endDate) {
        List<Coupon> coupons = couponRepository.findBySourceAndDateRange(
            Coupon.CouponSource.AI_NEGOTIATION, startDate, endDate);
        
        long totalGenerated = coupons.size();
        long totalUsed = coupons.stream().mapToLong(c -> c.getCurrentUses()).sum();
        BigDecimal totalDiscountGiven = coupons.stream()
            .filter(c -> c.getCurrentUses() > 0)
            .map(c -> c.getDiscountValue())
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        return new CouponUsageStats(totalGenerated, totalUsed, totalDiscountGiven);
    }

    public CouponValidationResult validateCouponDetailed(String couponCode, Customer customer, BigDecimal orderAmount) {
        try {
            Optional<Coupon> couponOpt = couponRepository.findByCouponCode(couponCode);
            if (couponOpt.isEmpty()) {
                return new CouponValidationResult(false, "Coupon code not found", null, BigDecimal.ZERO);
            }
            
            Coupon coupon = couponOpt.get();
            
            // Check if coupon is valid
            if (!coupon.isValid()) {
                return new CouponValidationResult(false, "Coupon is expired or inactive", coupon, BigDecimal.ZERO);
            }
            
            // Check customer eligibility
            if (coupon.getCustomerId() != null && !coupon.getCustomerId().equals(customer.getId())) {
                return new CouponValidationResult(false, "Coupon is not valid for this customer", coupon, BigDecimal.ZERO);
            }
            
            // Check minimum order amount
            if (orderAmount.compareTo(coupon.getMinimumOrderValue()) < 0) {
                return new CouponValidationResult(false, 
                    String.format("Minimum order amount of $%.2f required", coupon.getMinimumOrderValue()), 
                    coupon, BigDecimal.ZERO);
            }
            
            // Check usage limits
            if (coupon.getCurrentUses() >= coupon.getMaxUses()) {
                return new CouponValidationResult(false, "Coupon usage limit exceeded", coupon, BigDecimal.ZERO);
            }
            
            // Calculate discount
            BigDecimal discountAmount = calculateDiscount(coupon, orderAmount);
            
            return new CouponValidationResult(true, "Coupon is valid", coupon, discountAmount);
            
        } catch (Exception e) {
            return new CouponValidationResult(false, "Error validating coupon: " + e.getMessage(), null, BigDecimal.ZERO);
        }
    }
    
    public void useCoupon(Coupon coupon, Customer customer) {
        coupon.setCurrentUses(coupon.getCurrentUses() + 1);
        couponRepository.save(coupon);
    }

    public static class CouponUsageStats {
        private final long totalGenerated;
        private final long totalUsed;
        private final BigDecimal totalDiscountGiven;

        public CouponUsageStats(long totalGenerated, long totalUsed, BigDecimal totalDiscountGiven) {
            this.totalGenerated = totalGenerated;
            this.totalUsed = totalUsed;
            this.totalDiscountGiven = totalDiscountGiven;
        }

        public long getTotalGenerated() {
            return totalGenerated;
        }

        public long getTotalUsed() {
            return totalUsed;
        }

        public BigDecimal getTotalDiscountGiven() {
            return totalDiscountGiven;
        }

        public double getUsageRate() {
            return totalGenerated > 0 ? (double) totalUsed / totalGenerated : 0.0;
        }
    }
    
    public static class CouponValidationResult {
        private final boolean valid;
        private final String errorMessage;
        private final Coupon coupon;
        private final BigDecimal discountAmount;
        
        public CouponValidationResult(boolean valid, String errorMessage, Coupon coupon, BigDecimal discountAmount) {
            this.valid = valid;
            this.errorMessage = errorMessage;
            this.coupon = coupon;
            this.discountAmount = discountAmount;
        }
        
        public boolean isValid() { return valid; }
        public String getErrorMessage() { return errorMessage; }
        public Coupon getCoupon() { return coupon; }
        public BigDecimal getDiscountAmount() { return discountAmount; }
    }
} 