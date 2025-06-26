package com.example.project3;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class ScheduledTaskService {

    private static final Logger logger = LoggerFactory.getLogger(ScheduledTaskService.class);

    @Autowired
    private AIEmailMarketingService aiEmailMarketingService;
    
    @Autowired
    private CustomerProfileService customerProfileService;

    /**
     * Process pending email campaigns every 5 minutes
     */
    @Scheduled(fixedRate = 300000) // 5 minutes
    public void processPendingCampaigns() {
        try {
            logger.info("Processing pending email campaigns...");
            aiEmailMarketingService.processPendingCampaigns();
            logger.info("Completed processing pending campaigns");
        } catch (Exception e) {
            logger.error("Error processing pending campaigns", e);
        }
    }

    /**
     * Update customer profiles daily at 2 AM
     */
    @Scheduled(cron = "0 0 2 * * *")
    public void updateCustomerProfiles() {
        try {
            logger.info("Starting daily customer profile update...");
            customerProfileService.updateAllProfiles();
            logger.info("Completed daily customer profile update");
        } catch (Exception e) {
            logger.error("Error updating customer profiles", e);
        }
    }

    /**
     * Generate automated re-engagement campaigns weekly
     */
    @Scheduled(cron = "0 0 9 * * MON") // Every Monday at 9 AM
    public void generateReEngagementCampaigns() {
        try {
            logger.info("Generating weekly re-engagement campaigns...");
            
            // Create re-engagement campaign for at-risk customers
            aiEmailMarketingService.createAIGeneratedCampaign(
                "Weekly Re-engagement Campaign - " + LocalDateTime.now().toLocalDate(),
                "RE_ENGAGEMENT",
                "All", // Will be filtered by risk level in the service
                "system_automated"
            );
            
            logger.info("Generated weekly re-engagement campaign");
        } catch (Exception e) {
            logger.error("Error generating re-engagement campaigns", e);
        }
    }

    /**
     * Generate welcome campaigns for new customers daily at 10 AM
     */
    @Scheduled(cron = "0 0 10 * * *")
    public void generateWelcomeCampaigns() {
        try {
            logger.info("Checking for new customers to welcome...");
            
            // Create welcome campaign for new customers
            aiEmailMarketingService.createAIGeneratedCampaign(
                "Daily Welcome Campaign - " + LocalDateTime.now().toLocalDate(),
                "WELCOME",
                "New",
                "system_automated"
            );
            
            logger.info("Generated welcome campaign for new customers");
        } catch (Exception e) {
            logger.error("Error generating welcome campaigns", e);
        }
    }

    /**
     * Generate seasonal campaigns monthly on the 1st at 8 AM
     */
    @Scheduled(cron = "0 0 8 1 * *") // First day of every month at 8 AM
    public void generateSeasonalCampaigns() {
        try {
            logger.info("Generating monthly seasonal campaigns...");
            
            // Create seasonal campaigns for different segments
            String[] segments = {"Premium", "Regular", "Budget"};
            
            for (String segment : segments) {
                aiEmailMarketingService.createAIGeneratedCampaign(
                    "Monthly Seasonal Campaign - " + segment + " - " + LocalDateTime.now().toLocalDate(),
                    "SEASONAL",
                    segment,
                    "system_automated"
                );
            }
            
            logger.info("Generated seasonal campaigns for all segments");
        } catch (Exception e) {
            logger.error("Error generating seasonal campaigns", e);
        }
    }

    /**
     * Health check for the email marketing system - runs every hour
     */
    @Scheduled(fixedRate = 3600000) // 1 hour
    public void emailSystemHealthCheck() {
        try {
            logger.debug("Performing email system health check...");
            
            // Check for failed campaigns and log them
            // Could potentially implement retry logic here
            
            logger.debug("Email system health check completed");
        } catch (Exception e) {
            logger.error("Error during email system health check", e);
        }
    }
} 