package com.example.project3;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class AIEmailMarketingService {

    @Autowired
    private CustomerProfileService customerProfileService;
    
    @Autowired
    private EmailCampaignRepository emailCampaignRepository;
    
    @Autowired
    private CustomerProfileRepository customerProfileRepository;
    
    @Autowired
    private EmailService emailService;

    public EmailCampaign createAIGeneratedCampaign(String campaignName, String campaignType, String targetSegment, String createdBy) {
        EmailCampaign campaign = new EmailCampaign();
        campaign.setCampaignName(campaignName);
        campaign.setCampaignType(campaignType);
        campaign.setTargetSegment(targetSegment);
        campaign.setCreatedBy(createdBy);
        campaign.setAiGenerated(true);
        campaign.setPersonalizationEnabled(true);
        
        // Generate AI content based on campaign type and target segment
        generateAICampaignContent(campaign);
        
        return emailCampaignRepository.save(campaign);
    }

    private void generateAICampaignContent(EmailCampaign campaign) {
        String subjectTemplate = generateSubjectTemplate(campaign);
        String contentTemplate = generateContentTemplate(campaign);
        
        campaign.setSubjectTemplate(subjectTemplate);
        campaign.setContentTemplate(contentTemplate);
    }

    private String generateSubjectTemplate(EmailCampaign campaign) {
        Map<String, String> subjectTemplates = new HashMap<>();
        
        // Personalized subject templates based on campaign type and segment
        subjectTemplates.put("PROMOTIONAL_Premium", "{{customerName}}, Exclusive Premium Deals Just for You! 🌟");
        subjectTemplates.put("PROMOTIONAL_Regular", "{{customerName}}, Fresh Deals on Your Favorite Products! 🛍️");
        subjectTemplates.put("PROMOTIONAL_Budget", "{{customerName}}, Amazing Savings on Quality Products! 💰");
        subjectTemplates.put("PROMOTIONAL_New", "Welcome {{customerName}}! Here's 15% Off Your Next Order");
        
        subjectTemplates.put("RE_ENGAGEMENT_Premium", "We Miss You, {{customerName}}! Come Back for VIP Deals");
        subjectTemplates.put("RE_ENGAGEMENT_Regular", "{{customerName}}, Your Favorite Products Are Waiting!");
        subjectTemplates.put("RE_ENGAGEMENT_Budget", "{{customerName}}, Special Comeback Offers Inside!");
        
        subjectTemplates.put("WELCOME_New", "Welcome to Our Shopping Family, {{customerName}}! 🎉");
        subjectTemplates.put("SEASONAL_Premium", "{{customerName}}, Premium Seasonal Selection Just Arrived!");
        subjectTemplates.put("SEASONAL_Regular", "{{customerName}}, Fresh Seasonal Products Are Here!");
        
        String key = campaign.getCampaignType() + "_" + campaign.getTargetSegment();
        return subjectTemplates.getOrDefault(key, "{{customerName}}, Special Offer Just for You!");
    }

    private String generateContentTemplate(EmailCampaign campaign) {
        StringBuilder content = new StringBuilder();
        
        content.append("Dear {{customerName}},\n\n");
        
        switch (campaign.getCampaignType()) {
            case "PROMOTIONAL":
                content.append(generatePromotionalContent(campaign.getTargetSegment()));
                break;
            case "WELCOME":
                content.append(generateWelcomeContent());
                break;
            case "RE_ENGAGEMENT":
                content.append(generateReEngagementContent(campaign.getTargetSegment()));
                break;
            case "SEASONAL":
                content.append(generateSeasonalContent(campaign.getTargetSegment()));
                break;
            case "PERSONALIZED":
                content.append(generatePersonalizedContent());
                break;
            default:
                content.append(generateGenericContent());
        }
        
        content.append("\n\nBest regards,\nThe Online Shop Team\n\n");
        content.append("{{unsubscribeLink}}");
        
        return content.toString();
    }

    private String generatePromotionalContent(String segment) {
        Map<String, String> promotionalContent = new HashMap<>();
        
        promotionalContent.put("Premium", 
            "As one of our valued premium customers, we're excited to offer you exclusive access to our finest selection!\n\n" +
            "🌟 Premium Product Collection - 20% OFF\n" +
            "🚚 Free Express Delivery on orders over $75\n" +
            "💎 Early access to limited seasonal varieties\n\n" +
            "Your sophisticated taste deserves the best, and we're here to deliver exceptional quality right to your door.\n\n" +
            "Based on your previous purchases of {{favoriteCategories}}, we think you'll love our new premium {{seasonalRecommendations}}!");
        
        promotionalContent.put("Regular",
            "Great news! Your favorite products are on special offer this week!\n\n" +
            "🛍️ 15% OFF on all your favorite categories: {{favoriteCategories}}\n" +
            "📦 Free delivery on orders over $50\n" +
            "⭐ Earn double points on this purchase\n\n" +
            "We noticed you love {{favoriteCategories}} - perfect timing for our fresh arrivals!");
        
        promotionalContent.put("Budget",
            "Amazing deals that won't break the bank!\n\n" +
            "💰 Budget-Friendly Product Packs - Starting at $15\n" +
            "🎯 10% OFF on bulk purchases\n" +
            "📅 Weekly specials tailored just for you\n\n" +
            "Quality products at unbeatable prices - because everyone deserves great value!");
        
        promotionalContent.put("New",
            "Welcome to our shopping family! Here's a special treat to get you started:\n\n" +
            "🎁 15% OFF your first order with code WELCOME15\n" +
            "📚 Free Shopping Guide with tips and recommendations\n" +
            "🌟 Join our loyalty program for ongoing savings\n\n" +
            "Discover the difference quality products can make in your daily life!");
        
        return promotionalContent.getOrDefault(segment, promotionalContent.get("Regular"));
    }

    private String generateWelcomeContent() {
        return "Welcome to our shopping family! 🎉\n\n" +
                "We're thrilled to have you join our community of smart shoppers. At our store, we're passionate about bringing you the highest-quality products right to your doorstep.\n\n" +
                "🌟 Premium quality products sourced from trusted suppliers\n" +
                "🚚 Fast, reliable delivery\n" +
                "💬 AI-powered customer support\n" +
                "🎯 Personalized recommendations just for you\n\n" +
                "To get you started, here's 15% off your first order with code WELCOME15!\n\n" +
                "We can't wait to learn about your preferences and help you discover new favorites.";
    }

    private String generateReEngagementContent(String segment) {
        Map<String, String> reEngagementContent = new HashMap<>();
        
        reEngagementContent.put("Premium",
            "We miss you! As one of our most valued customers, we want to welcome you back with something special.\n\n" +
            "🌟 VIP Welcome Back Offer: 25% OFF your next order\n" +
            "💎 Exclusive access to our premium collection\n" +
            "🎁 Complimentary upgrade to express delivery\n\n" +
            "Your refined taste has always impressed us, and we have some exciting new premium varieties we think you'll absolutely love.\n\n" +
            "What do you say? Ready to rediscover what made us your go-to shopping destination?");
        
        reEngagementContent.put("Regular",
            "It's been a while since your last order, and we genuinely miss having you as part of our shopping family!\n\n" +
            "🛍️ 20% OFF to welcome you back\n" +
            "📦 Free delivery on your comeback order\n" +
            "⭐ Triple loyalty points for this purchase\n\n" +
            "We've added some fantastic new varieties and improved our delivery service. Your favorite {{favoriteCategories}} are better than ever!\n\n" +
            "Come back and experience the difference!");
        
        reEngagementContent.put("Budget",
            "We haven't seen you in a while and wanted to reach out with a special offer!\n\n" +
            "💝 Comeback Special: 15% OFF + Free Delivery\n" +
            "🎯 New budget-friendly product packs\n" +
            "📅 Weekly deals customized for smart shoppers like you\n\n" +
            "We understand the importance of value, and we've been working hard to bring you even better prices without compromising on quality!");
        
        return reEngagementContent.getOrDefault(segment, reEngagementContent.get("Regular"));
    }

    private String generateSeasonalContent(String segment) {
        String season = getCurrentSeason();
        return String.format("🌿 Fresh %s arrivals are here!\n\n" +
                "The season has brought us some incredible new varieties that we're excited to share with you.\n\n" +
                "✨ Premium %s selection\n" +
                "🌱 Locally sourced when possible\n" +
                "💫 Limited time seasonal specials\n\n" +
                "Based on your preferences for {{favoriteCategories}}, we've curated some special %s recommendations just for you!\n\n" +
                "Don't miss out on these seasonal delights - they're only available for a limited time!", 
                season, season, season);
    }

    private String generatePersonalizedContent() {
        return "We've been analyzing your preferences, and we have some exciting personalized recommendations!\n\n" +
                "🎯 Based on your love for {{favoriteCategories}}\n" +
                "📊 Tailored to your shopping frequency of every {{orderFrequency}} days\n" +
                "💰 Special pricing for your segment: {{customerSegment}}\n\n" +
                "{{aiPersonalityInsights}}\n\n" +
                "Here are our top picks for you this week:\n" +
                "{{personalizedRecommendations}}\n\n" +
                "We believe these selections will perfectly match your taste and preferences!";
    }

    private String generateGenericContent() {
        return "We have some exciting updates and offers to share with you!\n\n" +
                "🍎 Fresh arrivals weekly\n" +
                "🚚 Reliable delivery service\n" +
                "⭐ Loyalty rewards program\n" +
                "💬 AI-powered customer support\n\n" +
                "Check out our latest offerings and discover something new today!";
    }

    public String personalizeEmailContent(String template, CustomerProfile profile) {
        String personalizedContent = template;
        
        // Replace placeholders with actual customer data
        personalizedContent = personalizedContent.replace("{{customerName}}", profile.getCustomer().getUsername());
        personalizedContent = personalizedContent.replace("{{customerSegment}}", profile.getCustomerSegment());
        personalizedContent = personalizedContent.replace("{{totalSpent}}", profile.getTotalSpent().toString());
        personalizedContent = personalizedContent.replace("{{orderFrequency}}", String.format("%.1f", profile.getOrderFrequencyDays()));
        
        if (profile.getFavoriteCategories() != null && !profile.getFavoriteCategories().isEmpty()) {
            personalizedContent = personalizedContent.replace("{{favoriteCategories}}", 
                String.join(", ", profile.getFavoriteCategories()));
        }
        
        if (profile.getAiPersonalityProfile() != null) {
            personalizedContent = personalizedContent.replace("{{aiPersonalityInsights}}", 
                profile.getAiPersonalityProfile());
        }
        
        // Add seasonal recommendations
        personalizedContent = personalizedContent.replace("{{seasonalRecommendations}}", 
            getSeasonalRecommendations(profile));
        
        // Add personalized recommendations
        personalizedContent = personalizedContent.replace("{{personalizedRecommendations}}", 
            getPersonalizedRecommendations(profile));
        
        // Add unsubscribe link
        personalizedContent = personalizedContent.replace("{{unsubscribeLink}}", 
            "If you no longer wish to receive these emails, click here to unsubscribe.");
        
        return personalizedContent;
    }

    public void sendCampaign(Long campaignId) {
        EmailCampaign campaign = emailCampaignRepository.findById(campaignId)
                .orElseThrow(() -> new RuntimeException("Campaign not found"));
        
        List<CustomerProfile> targetProfiles = getTargetProfiles(campaign);
        
        campaign.setStatus("RUNNING");
        campaign.setSentAt(LocalDateTime.now());
        campaign.setRecipientsCount(targetProfiles.size());
        
        int emailsSent = 0;
        
        for (CustomerProfile profile : targetProfiles) {
            try {
                if (shouldSendEmail(profile, campaign)) {
                    String personalizedSubject = personalizeEmailContent(campaign.getSubjectTemplate(), profile);
                    String personalizedContent = personalizeEmailContent(campaign.getContentTemplate(), profile);
                    
                    emailService.sendSimpleMessage(
                        profile.getCustomer().getEmail(),
                        personalizedSubject,
                        personalizedContent
                    );
                    
                    emailsSent++;
                    customerProfileService.recordEmailActivity(profile.getCustomer().getId(), "sent");
                }
            } catch (Exception e) {
                System.err.println("Failed to send email to " + profile.getCustomer().getEmail() + ": " + e.getMessage());
            }
        }
        
        campaign.setEmailsSent(emailsSent);
        campaign.setEmailsDelivered(emailsSent); // Assuming all sent emails are delivered for now
        campaign.setStatus("COMPLETED");
        
        emailCampaignRepository.save(campaign);
    }

    private List<CustomerProfile> getTargetProfiles(EmailCampaign campaign) {
        List<CustomerProfile> profiles = new ArrayList<>();
        
        if (campaign.getTargetSegment() != null && !campaign.getTargetSegment().equals("All")) {
            profiles = customerProfileRepository.findByCustomerSegment(campaign.getTargetSegment());
        } else {
            profiles = customerProfileRepository.findAll();
        }
        
        // Filter out opted-out customers if campaign respects opt-out
        if (campaign.getRespectOptOut()) {
            profiles = profiles.stream()
                    .filter(profile -> !profile.getOptOut())
                    .collect(Collectors.toList());
        }
        
        return profiles;
    }

    private boolean shouldSendEmail(CustomerProfile profile, EmailCampaign campaign) {
        // Check opt-out status
        if (campaign.getRespectOptOut() && profile.getOptOut()) {
            return false;
        }
        
        // Check minimum days since last email
        if (profile.getLastEmailSent() != null) {
            long daysSinceLastEmail = LocalDateTime.now().minusDays(campaign.getMinDaysSinceLastEmail())
                    .compareTo(profile.getLastEmailSent());
            if (daysSinceLastEmail < 0) {
                return false;
            }
        }
        
        return true;
    }

    private String getSeasonalRecommendations(CustomerProfile profile) {
        // This would typically integrate with inventory or seasonal product data
        String season = getCurrentSeason();
        return season + " seasonal products perfect for your taste preferences";
    }

    private String getPersonalizedRecommendations(CustomerProfile profile) {
        StringBuilder recommendations = new StringBuilder();
        
        if (profile.getFavoriteCategories() != null && !profile.getFavoriteCategories().isEmpty()) {
            recommendations.append("• Premium selections from your favorite categories: ")
                    .append(String.join(", ", profile.getFavoriteCategories())).append("\n");
        }
        
        switch (profile.getCustomerSegment()) {
            case "Premium":
                recommendations.append("• Exclusive premium varieties\n");
                recommendations.append("• Limited edition seasonal collections\n");
                break;
            case "Regular":
                recommendations.append("• Fresh arrivals in your preferred categories\n");
                recommendations.append("• Bundle deals for regular customers\n");
                break;
            case "Budget":
                recommendations.append("• Value packs with excellent savings\n");
                recommendations.append("• Weekly specials tailored for smart shoppers\n");
                break;
            default:
                recommendations.append("• Curated selection of popular products\n");
                recommendations.append("• Beginner-friendly variety packs\n");
        }
        
        return recommendations.toString();
    }

    private String getCurrentSeason() {
        int month = LocalDateTime.now().getMonthValue();
        if (month >= 3 && month <= 5) return "Spring";
        if (month >= 6 && month <= 8) return "Summer";
        if (month >= 9 && month <= 11) return "Fall";
        return "Winter";
    }

    public List<EmailCampaign> getActiveCampaigns() {
        return emailCampaignRepository.findActiveCampaigns();
    }

    public List<EmailCampaign> getCompletedCampaigns() {
        return emailCampaignRepository.findCompletedCampaigns();
    }

    public EmailCampaign scheduleCampaign(Long campaignId, LocalDateTime scheduledDate) {
        EmailCampaign campaign = emailCampaignRepository.findById(campaignId)
                .orElseThrow(() -> new RuntimeException("Campaign not found"));
        
        campaign.setScheduledDate(scheduledDate);
        campaign.setStatus("SCHEDULED");
        
        return emailCampaignRepository.save(campaign);
    }

    public void processPendingCampaigns() {
        List<EmailCampaign> pendingCampaigns = emailCampaignRepository
                .findCampaignsReadyToSend(LocalDateTime.now());
        
        for (EmailCampaign campaign : pendingCampaigns) {
            try {
                sendCampaign(campaign.getId());
            } catch (Exception e) {
                System.err.println("Failed to send campaign " + campaign.getId() + ": " + e.getMessage());
                campaign.setStatus("FAILED");
                emailCampaignRepository.save(campaign);
            }
        }
    }
} 