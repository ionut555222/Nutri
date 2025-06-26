package com.example.project3;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "customers")
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "last_login")
    private LocalDateTime lastLogin;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "email_verified")
    private Boolean emailVerified = false;

    // GDPR Compliance Fields
    @Column(name = "date_of_birth")
    private LocalDateTime dateOfBirth;
    
    @Column(name = "marketing_consent")
    private Boolean marketingConsent = false;
    
    @Column(name = "analytics_consent")
    private Boolean analyticsConsent = false;
    
    @Column(name = "profiling_consent")
    private Boolean profilingConsent = false;
    
    @Column(name = "consent_date")
    private LocalDateTime consentDate;
    
    @Column(name = "consent_withdrawn_date")
    private LocalDateTime consentWithdrawnDate;
    
    @Column(name = "gdpr_deleted")
    private Boolean gdprDeleted = false;
    
    @Column(name = "deletion_date")
    private LocalDateTime deletionDate;
    
    @Column(name = "data_retention_period_months")
    private Integer dataRetentionPeriodMonths = 24; // Default 2 years
    
    @Column(name = "last_data_export_date")
    private LocalDateTime lastDataExportDate;
    
    @Column(name = "privacy_policy_version")
    private String privacyPolicyVersion;
    
    @Column(name = "privacy_policy_accepted_date")
    private LocalDateTime privacyPolicyAcceptedDate;

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference
    private Set<Order> orders = new HashSet<>();

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<ChatMessage> chatMessages = new ArrayList<>();

    @OneToOne(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true)
    private CustomerProfile profile;

    public Customer() {
        this.createdAt = LocalDateTime.now();
    }

    public Customer(String username, String email, String password) {
        this();
        this.username = username;
        this.email = email;
        this.password = password;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(LocalDateTime lastLogin) {
        this.lastLogin = lastLogin;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public Boolean getEmailVerified() {
        return emailVerified;
    }

    public void setEmailVerified(Boolean emailVerified) {
        this.emailVerified = emailVerified;
    }

    public Set<Order> getOrders() {
        return orders;
    }

    public void setOrders(Set<Order> orders) {
        this.orders = orders;
    }

    public List<ChatMessage> getChatMessages() {
        return chatMessages;
    }

    public void setChatMessages(List<ChatMessage> chatMessages) {
        this.chatMessages = chatMessages;
    }

    public CustomerProfile getProfile() {
        return profile;
    }

    public void setProfile(CustomerProfile profile) {
        this.profile = profile;
    }

    public String getFullName() {
        if (firstName != null && lastName != null) {
            return firstName + " " + lastName;
        }
        return username;
    }
} 