package com.example.project3;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerProfileRepository extends JpaRepository<CustomerProfile, Long> {
    
    Optional<CustomerProfile> findByCustomerId(Long customerId);
    
    Optional<CustomerProfile> findByCustomerUsername(String username);
    
    // Backward compatibility methods (deprecated)
    default Optional<CustomerProfile> findByUserId(Long userId) {
        return findByCustomerId(userId);
    }
    
    default Optional<CustomerProfile> findByUserUsername(String username) {
        return findByCustomerUsername(username);
    }
    
    List<CustomerProfile> findByCustomerSegment(String segment);
    
    List<CustomerProfile> findByRiskLevel(String riskLevel);
    
    List<CustomerProfile> findByOptOutFalse();
    
    @Query("SELECT cp FROM CustomerProfile cp WHERE cp.totalSpent >= :minSpent")
    List<CustomerProfile> findByTotalSpentGreaterThanEqual(@Param("minSpent") BigDecimal minSpent);
    
    @Query("SELECT cp FROM CustomerProfile cp WHERE cp.orderFrequencyDays <= :maxDays")
    List<CustomerProfile> findFrequentCustomers(@Param("maxDays") Double maxDays);
    
    @Query("SELECT cp FROM CustomerProfile cp WHERE cp.lastOrderDate < :cutoffDate")
    List<CustomerProfile> findInactiveCustomers(@Param("cutoffDate") LocalDateTime cutoffDate);
    
    @Query("SELECT cp FROM CustomerProfile cp WHERE cp.lastEmailSent < :cutoffDate OR cp.lastEmailSent IS NULL")
    List<CustomerProfile> findCustomersNeedingEmail(@Param("cutoffDate") LocalDateTime cutoffDate);
    
    @Query("SELECT cp FROM CustomerProfile cp WHERE cp.profileUpdatedAt < :cutoffDate")
    List<CustomerProfile> findProfilesNeedingUpdate(@Param("cutoffDate") LocalDateTime cutoffDate);
    
    @Query("SELECT DISTINCT cp.customerSegment FROM CustomerProfile cp WHERE cp.customerSegment IS NOT NULL")
    List<String> findAllCustomerSegments();
    
    @Query("SELECT cp FROM CustomerProfile cp WHERE SIZE(cp.favoriteCategories) > 0 AND :category MEMBER OF cp.favoriteCategories")
    List<CustomerProfile> findByFavoriteCategory(@Param("category") String category);
} 