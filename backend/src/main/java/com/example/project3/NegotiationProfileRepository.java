package com.example.project3;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface NegotiationProfileRepository extends JpaRepository<NegotiationProfile, Long> {
    
    Optional<NegotiationProfile> findByCustomerId(Long customerId);
    
    List<NegotiationProfile> findByBlockedFromNegotiationTrue();
    
    List<NegotiationProfile> findByNegotiationStyle(String negotiationStyle);
    
    @Query("SELECT np FROM NegotiationProfile np WHERE np.blockedFromNegotiation = true AND np.blockUntilDate < :now")
    List<NegotiationProfile> findExpiredBlocks(@Param("now") LocalDateTime now);
    
    @Query("SELECT np FROM NegotiationProfile np WHERE np.consecutiveRejections >= :threshold")
    List<NegotiationProfile> findHighRejectionProfiles(@Param("threshold") Integer threshold);
    
    @Query("SELECT np FROM NegotiationProfile np WHERE np.negotiationSuccessRate > :minRate ORDER BY np.negotiationSuccessRate DESC")
    List<NegotiationProfile> findHighPerformingNegotiators(@Param("minRate") Double minRate);
    
    @Query("SELECT AVG(np.negotiationSuccessRate) FROM NegotiationProfile np WHERE np.negotiationStyle = :style")
    Double getAverageSuccessRateByStyle(@Param("style") String style);
    
    @Query("SELECT COUNT(np) FROM NegotiationProfile np WHERE np.profileCreatedAt >= :startDate")
    Long countNewProfilesSince(@Param("startDate") LocalDateTime startDate);
    
    @Query("SELECT np FROM NegotiationProfile np WHERE np.profileUpdatedAt < :threshold")
    List<NegotiationProfile> findStaleProfiles(@Param("threshold") LocalDateTime threshold);
} 