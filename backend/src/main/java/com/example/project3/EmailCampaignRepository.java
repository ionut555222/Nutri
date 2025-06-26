package com.example.project3;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EmailCampaignRepository extends JpaRepository<EmailCampaign, Long> {
    
    List<EmailCampaign> findByStatus(String status);
    
    List<EmailCampaign> findByCampaignType(String campaignType);
    
    List<EmailCampaign> findByTargetSegment(String targetSegment);
    
    List<EmailCampaign> findByCreatedBy(String createdBy);
    
    @Query("SELECT ec FROM EmailCampaign ec WHERE ec.scheduledDate <= :now AND ec.status = 'SCHEDULED'")
    List<EmailCampaign> findCampaignsReadyToSend(@Param("now") LocalDateTime now);
    
    @Query("SELECT ec FROM EmailCampaign ec WHERE ec.status IN ('RUNNING', 'SCHEDULED') ORDER BY ec.scheduledDate ASC")
    List<EmailCampaign> findActiveCampaigns();
    
    @Query("SELECT ec FROM EmailCampaign ec WHERE ec.status = 'COMPLETED' ORDER BY ec.sentAt DESC")
    List<EmailCampaign> findCompletedCampaigns();
    
    @Query("SELECT ec FROM EmailCampaign ec WHERE ec.createdAt >= :startDate AND ec.createdAt <= :endDate")
    List<EmailCampaign> findCampaignsByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT DISTINCT ec.targetSegment FROM EmailCampaign ec WHERE ec.targetSegment IS NOT NULL")
    List<String> findAllTargetSegments();
    
    @Query("SELECT ec FROM EmailCampaign ec WHERE SIZE(ec.targetCategories) > 0 AND :category MEMBER OF ec.targetCategories")
    List<EmailCampaign> findByTargetCategory(@Param("category") String category);
} 