package com.example.project3;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/chat")
public class ChatController {

    @Autowired
    private GeminiService geminiService;

    @Autowired
    private ChatMessageRepository chatMessageRepository;
    
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @PostMapping
    public ResponseEntity<?> chat(@RequestBody ChatRequest chatRequest, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body("User is not authenticated");
        }
        String username = authentication.getName();

        try {
            String responseText = geminiService.getResponse(username, chatRequest);
            return ResponseEntity.ok(Collections.singletonMap("response", responseText));
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error processing your request: " + e.getMessage());
        }
    }

    @GetMapping("/history")
    public ResponseEntity<?> getHistory(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body("User is not authenticated");
        }
        String username = authentication.getName();
        Customer customer = customerRepository.findByUsername(username).orElse(null);
        
        if (customer == null) {
            // If no Customer found, try to find/create one from User
            User user = userRepository.findByUsername(username).orElse(null);
            if (user == null) {
                return ResponseEntity.status(404).body("User not found");
            }
            
            // Create a Customer entity from the User for chat functionality
            customer = new Customer();
            customer.setUsername(user.getUsername());
            customer.setEmail(user.getEmail());
            customer = customerRepository.save(customer);
        }
        
        List<ChatMessage> history = chatMessageRepository.findByCustomerOrderByTimestampAsc(customer);
        return ResponseEntity.ok(history);
    }

    @PostMapping("/messages")
    public ResponseEntity<?> saveMessage(@RequestBody ChatMessage message, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body("User is not authenticated");
        }
        String username = authentication.getName();
        Customer customer = customerRepository.findByUsername(username).orElse(null);
        
        if (customer == null) {
            // If no Customer found, try to find/create one from User
            User user = userRepository.findByUsername(username).orElse(null);
            if (user == null) {
                return ResponseEntity.status(404).body("User not found");
            }
            
            // Create a Customer entity from the User for chat functionality
            customer = new Customer();
            customer.setUsername(user.getUsername());
            customer.setEmail(user.getEmail());
            customer = customerRepository.save(customer);
        }
        
        message.setCustomer(customer);
        message.setTimestamp(java.time.LocalDateTime.now());
        ChatMessage savedMessage = chatMessageRepository.save(message);
        return ResponseEntity.ok(Collections.singletonMap("message", "Chat message saved successfully"));
    }
} 