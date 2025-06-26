package com.example.project3;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDateTime;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import com.fasterxml.jackson.annotation.JsonBackReference;

@Entity
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String sender; // "user" or "bot"
    @jakarta.persistence.Column(columnDefinition = "TEXT")
    private String content;
    private LocalDateTime timestamp;

    @ManyToOne
    @JoinColumn(name = "customer_id")
    @JsonBackReference
    private Customer customer;

    public ChatMessage() {
        this.timestamp = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    // Backward compatibility methods
    public User getUser() {
        // Deprecated - use getCustomer() instead
        return null;
    }

    public void setUser(User user) {
        // Deprecated - use setCustomer() instead
    }
} 