package com.example.project3;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/negotiate")
public class NegotiationController {

    @Autowired
    private AINegotiationService aiNegotiationService;
    
    @Autowired
    private CustomerRepository customerRepository;
    
    @Autowired
    private CustomerTierService customerTierService;
    
    @Autowired
    private CouponService couponService;

    @PostMapping
    public ResponseEntity<?> negotiatePrice(@RequestBody NegotiationRequest request, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body(createErrorResponse("User is not authenticated"));
        }
        
        try {
            String username = authentication.getName();
            Customer customer = customerRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("Customer not found: " + username));
            
            // Validate request
            if (request.getMessage() == null || request.getMessage().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("Negotiation message is required"));
            }
            
            if (request.getCartValue() == null || request.getCartValue().signum() <= 0) {
                return ResponseEntity.badRequest().body(createErrorResponse("Valid cart value is required"));
            }
            
            // Process negotiation
            NegotiationResponse response = aiNegotiationService.processNegotiationRequest(customer, request);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.status(500).body(createErrorResponse("Error processing negotiation: " + e.getMessage()));
        }
    }
    
    @GetMapping("/eligibility")
    public ResponseEntity<?> checkNegotiationEligibility(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body(createErrorResponse("User is not authenticated"));
        }
        
        try {
            String username = authentication.getName();
            Customer customer = customerRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("Customer not found: " + username));
            
            CustomerTierService.NegotiationCapability capability = 
                customerTierService.calculateNegotiationCapability(customer);
            
            Map<String, Object> eligibility = new HashMap<>();
            eligibility.put("eligible", capability.canNegotiate());
            eligibility.put("customerTier", capability.getTier().getDisplayName());
            eligibility.put("maxDiscountPercentage", capability.getMaxDiscountPercentage());
            eligibility.put("negotiationAttemptsRemaining", capability.getNegotiationAttemptsRemaining());
            eligibility.put("negotiationStrategy", capability.getNegotiationStrategy());
            
            return ResponseEntity.ok(eligibility);
            
        } catch (Exception e) {
            return ResponseEntity.status(500).body(createErrorResponse("Error checking eligibility: " + e.getMessage()));
        }
    }
    
    @GetMapping("/coupons")
    public ResponseEntity<?> getValidCoupons(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body(createErrorResponse("User is not authenticated"));
        }
        
        try {
            String username = authentication.getName();
            Customer customer = customerRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("Customer not found: " + username));
            
            var coupons = couponService.getValidCouponsForCustomer(customer);
            
            return ResponseEntity.ok(coupons);
            
        } catch (Exception e) {
            return ResponseEntity.status(500).body(createErrorResponse("Error fetching coupons: " + e.getMessage()));
        }
    }
    
    @PostMapping("/test")
    public ResponseEntity<?> testNegotiation(@RequestBody Map<String, Object> testRequest, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body(createErrorResponse("User is not authenticated"));
        }
        
        try {
            String username = authentication.getName();
            Customer customer = customerRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("Customer not found: " + username));
            
            // Get customer tier and capability info for testing
            CustomerTierService.CustomerTier tier = customerTierService.determineCustomerTier(customer);
            CustomerTierService.NegotiationCapability capability = 
                customerTierService.calculateNegotiationCapability(customer);
            
            Map<String, Object> testResponse = new HashMap<>();
            testResponse.put("customer", username);
            testResponse.put("tier", tier.getDisplayName());
            testResponse.put("tierCode", tier.name());
            testResponse.put("maxDiscount", capability.getMaxDiscountPercentage());
            testResponse.put("strategy", capability.getNegotiationStrategy());
            testResponse.put("canNegotiate", capability.canNegotiate());
            testResponse.put("attemptsRemaining", capability.getNegotiationAttemptsRemaining());
            testResponse.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.ok(testResponse);
            
        } catch (Exception e) {
            return ResponseEntity.status(500).body(createErrorResponse("Test error: " + e.getMessage()));
        }
    }
    
    private Map<String, String> createErrorResponse(String message) {
        Map<String, String> error = new HashMap<>();
        error.put("error", message);
        error.put("timestamp", LocalDateTime.now().toString());
        return error;
    }
} 