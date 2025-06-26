package com.example.project3;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "email_campaigns")
public class EmailCampaign {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "campaign_name", nullable = false)
    private String campaignName;
    
    @Column(name = "campaign_type")
    private String campaignType; // "PROMOTIONAL", "WELCOME", "RE_ENGAGEMENT", "SEASONAL", "PERSONALIZED"
    
    @Column(name = "target_segment")
    private String targetSegment; // "Premium", "Regular", "Budget", "New", "At_Risk", etc.
    
    @Column(name = "subject_template", columnDefinition = "TEXT")
    private String subjectTemplate;
    
    @Column(name = "content_template", columnDefinition = "TEXT")
    private String contentTemplate;
    
    @Column(name = "ai_generated")
    private Boolean aiGenerated = false;
    
    @Column(name = "personalization_enabled")
    private Boolean personalizationEnabled = true;
    
    @Column(name = "status")
    private String status; // "DRAFT", "SCHEDULED", "RUNNING", "COMPLETED", "PAUSED"
    
    @Column(name = "scheduled_date")
    private LocalDateTime scheduledDate;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "sent_at")
    private LocalDateTime sentAt;
    
    // Campaign metrics
    @Column(name = "recipients_count")
    private Integer recipientsCount = 0;
    
    @Column(name = "emails_sent")
    private Integer emailsSent = 0;
    
    @Column(name = "emails_delivered")
    private Integer emailsDelivered = 0;
    
    @Column(name = "emails_opened")
    private Integer emailsOpened = 0;
    
    @Column(name = "emails_clicked")
    private Integer emailsClicked = 0;
    
    @Column(name = "unsubscribes")
    private Integer unsubscribes = 0;
    
    // Campaign settings
    @Column(name = "min_days_since_last_email")
    private Integer minDaysSinceLastEmail = 1;
    
    @Column(name = "respect_opt_out")
    private Boolean respectOptOut = true;
    
    @ElementCollection
    @CollectionTable(name = "campaign_target_categories", joinColumns = @JoinColumn(name = "campaign_id"))
    @Column(name = "category_name")
    private List<String> targetCategories;
    
    @Column(name = "created_by")
    private String createdBy;
    
    public EmailCampaign() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.status = "DRAFT";
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getCampaignName() {
        return campaignName;
    }
    
    public void setCampaignName(String campaignName) {
        this.campaignName = campaignName;
    }
    
    public String getCampaignType() {
        return campaignType;
    }
    
    public void setCampaignType(String campaignType) {
        this.campaignType = campaignType;
    }
    
    public String getTargetSegment() {
        return targetSegment;
    }
    
    public void setTargetSegment(String targetSegment) {
        this.targetSegment = targetSegment;
    }
    
    public String getSubjectTemplate() {
        return subjectTemplate;
    }
    
    public void setSubjectTemplate(String subjectTemplate) {
        this.subjectTemplate = subjectTemplate;
    }
    
    public String getContentTemplate() {
        return contentTemplate;
    }
    
    public void setContentTemplate(String contentTemplate) {
        this.contentTemplate = contentTemplate;
    }
    
    public Boolean getAiGenerated() {
        return aiGenerated;
    }
    
    public void setAiGenerated(Boolean aiGenerated) {
        this.aiGenerated = aiGenerated;
    }
    
    public Boolean getPersonalizationEnabled() {
        return personalizationEnabled;
    }
    
    public void setPersonalizationEnabled(Boolean personalizationEnabled) {
        this.personalizationEnabled = personalizationEnabled;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
        this.updatedAt = LocalDateTime.now();
    }
    
    public LocalDateTime getScheduledDate() {
        return scheduledDate;
    }
    
    public void setScheduledDate(LocalDateTime scheduledDate) {
        this.scheduledDate = scheduledDate;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public LocalDateTime getSentAt() {
        return sentAt;
    }
    
    public void setSentAt(LocalDateTime sentAt) {
        this.sentAt = sentAt;
    }
    
    public Integer getRecipientsCount() {
        return recipientsCount;
    }
    
    public void setRecipientsCount(Integer recipientsCount) {
        this.recipientsCount = recipientsCount;
    }
    
    public Integer getEmailsSent() {
        return emailsSent;
    }
    
    public void setEmailsSent(Integer emailsSent) {
        this.emailsSent = emailsSent;
    }
    
    public Integer getEmailsDelivered() {
        return emailsDelivered;
    }
    
    public void setEmailsDelivered(Integer emailsDelivered) {
        this.emailsDelivered = emailsDelivered;
    }
    
    public Integer getEmailsOpened() {
        return emailsOpened;
    }
    
    public void setEmailsOpened(Integer emailsOpened) {
        this.emailsOpened = emailsOpened;
    }
    
    public Integer getEmailsClicked() {
        return emailsClicked;
    }
    
    public void setEmailsClicked(Integer emailsClicked) {
        this.emailsClicked = emailsClicked;
    }
    
    public Integer getUnsubscribes() {
        return unsubscribes;
    }
    
    public void setUnsubscribes(Integer unsubscribes) {
        this.unsubscribes = unsubscribes;
    }
    
    public Integer getMinDaysSinceLastEmail() {
        return minDaysSinceLastEmail;
    }
    
    public void setMinDaysSinceLastEmail(Integer minDaysSinceLastEmail) {
        this.minDaysSinceLastEmail = minDaysSinceLastEmail;
    }
    
    public Boolean getRespectOptOut() {
        return respectOptOut;
    }
    
    public void setRespectOptOut(Boolean respectOptOut) {
        this.respectOptOut = respectOptOut;
    }
    
    public List<String> getTargetCategories() {
        return targetCategories;
    }
    
    public void setTargetCategories(List<String> targetCategories) {
        this.targetCategories = targetCategories;
    }
    
    public String getCreatedBy() {
        return createdBy;
    }
    
    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }
    
    // Utility methods
    public double getOpenRate() {
        if (emailsSent == 0) return 0.0;
        return (double) emailsOpened / emailsSent * 100;
    }
    
    public double getClickRate() {
        if (emailsSent == 0) return 0.0;
        return (double) emailsClicked / emailsSent * 100;
    }
    
    public double getDeliveryRate() {
        if (emailsSent == 0) return 0.0;
        return (double) emailsDelivered / emailsSent * 100;
    }
    
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
} 