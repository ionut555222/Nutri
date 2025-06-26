package com.example.project3;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);

    @Autowired
    private OrderService orderService;

    @GetMapping
    public ResponseEntity<?> getUserOrders(Authentication authentication) {
        try {
            String username = authentication.getName();
            List<OrderDTO> orders = orderService.getUserOrders(username);
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            logger.error("Error fetching orders for user: {}", authentication.getName(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch orders", "message", e.getMessage()));
        }
    }

    @GetMapping("/fulfilled")
    public ResponseEntity<?> getUserFulfilledOrders(Authentication authentication) {
        try {
            String username = authentication.getName();
            List<OrderDTO> orders = orderService.getUserFulfilledOrders(username);
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            logger.error("Error fetching fulfilled orders for user: {}", authentication.getName(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch fulfilled orders"));
        }
    }

    @GetMapping("/pending")
    public ResponseEntity<?> getUserPendingOrders(Authentication authentication) {
        try {
            String username = authentication.getName();
            List<OrderDTO> orders = orderService.getUserPendingOrders(username);
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            logger.error("Error fetching pending orders for user: {}", authentication.getName(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch pending orders"));
        }
    }

    @PostMapping("/checkout")
    @CacheEvict(value = "orders", key = "#authentication.name")
    public ResponseEntity<?> createOrder(@Valid @RequestBody CheckoutRequest checkoutRequest, 
                                       Authentication authentication) {
        try {
            String username = authentication.getName();
            logger.info("Starting checkout for user: {}", username);
            
            OrderDTO order = orderService.createOrder(checkoutRequest, username);
            
            logger.info("Order created successfully with ID: {} for user: {}", order.getId(), username);
            return ResponseEntity.status(HttpStatus.CREATED).body(order);
            
        } catch (OrderService.InvalidOrderException e) {
            logger.warn("Invalid order for user {}: {}", authentication.getName(), e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Invalid order", "message", e.getMessage()));
                    
        } catch (OrderService.ProductNotFoundException e) {
            logger.warn("Product not found during checkout for user {}: {}", authentication.getName(), e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Product not found", "message", e.getMessage()));
                    
        } catch (OrderService.InsufficientStockException e) {
            logger.warn("Insufficient stock during checkout for user {}: {}", authentication.getName(), e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", "Insufficient stock", "message", e.getMessage()));
                    
        } catch (OrderService.InvalidPriceException e) {
            logger.error("Invalid price during checkout for user {}: {}", authentication.getName(), e.getMessage());
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                    .body(Map.of("error", "Invalid price", "message", e.getMessage()));
                    
        } catch (Exception e) {
            logger.error("Unexpected error during checkout for user: {}", authentication.getName(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Order processing failed", "message", "Please try again later"));
        }
    }

    @PostMapping("/{id}/fulfill")
    @CacheEvict(value = "orders", allEntries = true)
    public ResponseEntity<?> fulfillOrder(@PathVariable Long id) {
        try {
            orderService.fulfillOrder(id);
            logger.info("Order {} fulfilled successfully", id);
            return ResponseEntity.ok(Map.of("message", "Order fulfilled successfully"));
            
        } catch (OrderService.OrderNotFoundException e) {
            logger.warn("Order not found for fulfillment: {}", id);
            return ResponseEntity.notFound().build();
            
        } catch (Exception e) {
            logger.error("Error fulfilling order {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fulfill order"));
        }
    }

    // Legacy endpoint for backward compatibility
    @GetMapping("/all")
    public ResponseEntity<?> getAllUserOrders(Authentication authentication) {
        return getUserOrders(authentication);
    }

    // Health check endpoint for this controller
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        return ResponseEntity.ok(Map.of(
            "status", "UP",
            "service", "OrderController",
            "timestamp", java.time.LocalDateTime.now().toString()
        ));
    }
} 