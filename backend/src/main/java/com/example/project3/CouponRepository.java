package com.example.project3;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CouponRepository extends JpaRepository<Coupon, Long> {
    
    Optional<Coupon> findByCouponCode(String couponCode);
    
    List<Coupon> findByCustomerId(Long customerId);
    
    List<Coupon> findByCustomerIdAndIsActiveTrue(Long customerId);
    
    @Query("SELECT c FROM Coupon c WHERE c.customerId = :customerId AND c.isActive = true AND c.expirationDate > :now AND c.currentUses < c.maxUses")
    List<Coupon> findValidCouponsByCustomerId(@Param("customerId") Long customerId, @Param("now") LocalDateTime now);
    
    @Query("SELECT c FROM Coupon c WHERE c.couponCode = :couponCode AND c.isActive = true AND c.expirationDate > :now AND c.currentUses < c.maxUses")
    Optional<Coupon> findValidCouponByCode(@Param("couponCode") String couponCode, @Param("now") LocalDateTime now);
    
    List<Coupon> findByGeneratedBy(Coupon.CouponSource source);
    
    @Query("SELECT c FROM Coupon c WHERE c.generatedBy = :source AND c.createdAt BETWEEN :startDate AND :endDate")
    List<Coupon> findBySourceAndDateRange(
        @Param("source") Coupon.CouponSource source, 
        @Param("startDate") LocalDateTime startDate, 
        @Param("endDate") LocalDateTime endDate
    );
    
    @Query("SELECT COUNT(c) FROM Coupon c WHERE c.customerId = :customerId AND c.generatedBy = 'AI_NEGOTIATION' AND c.createdAt >= :monthStart")
    Long countNegotiationCouponsThisMonth(@Param("customerId") Long customerId, @Param("monthStart") LocalDateTime monthStart);
    
    @Query("SELECT c FROM Coupon c WHERE c.expirationDate < :now AND c.isActive = true")
    List<Coupon> findExpiredActiveCoupons(@Param("now") LocalDateTime now);
} 