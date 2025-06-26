package com.example.project3;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@Transactional
public class OrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FruitRepository fruitRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private CustomerProfileService customerProfileService;

    @Autowired
    private CouponService couponService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private ModelMapper modelMapper;

    @Cacheable(value = "orders", key = "#username")
    public List<OrderDTO> getUserOrders(String username) {
        // Handle both customers and staff users
        Customer customer = customerRepository.findByUsername(username)
                .orElse(customerRepository.findByEmail(username).orElse(null));
        
        List<Order> orders;
        if (customer != null) {
            orders = orderRepository.findByCustomerOrderByOrderDateDesc(customer);
        } else {
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
            orders = orderRepository.findByUserOrderByOrderDateDesc(user);
        }
        
        return orders.stream()
                .map(order -> modelMapper.map(order, OrderDTO.class))
                .collect(Collectors.toList());
    }

    @Cacheable(value = "orders", key = "#username + '-fulfilled'")
    public List<OrderDTO> getUserFulfilledOrders(String username) {
        // Handle both customers and staff users
        Customer customer = customerRepository.findByUsername(username)
                .orElse(customerRepository.findByEmail(username).orElse(null));
        
        List<Order> orders;
        if (customer != null) {
            orders = orderRepository.findByCustomerAndFulfilledTrueOrderByOrderDateDesc(customer);
        } else {
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
            orders = orderRepository.findByUserAndFulfilledTrueOrderByOrderDateDesc(user);
        }
        
        return orders.stream()
                .map(order -> modelMapper.map(order, OrderDTO.class))
                .collect(Collectors.toList());
    }

    @Cacheable(value = "orders", key = "#username + '-pending'")
    public List<OrderDTO> getUserPendingOrders(String username) {
        // Handle both customers and staff users
        Customer customer = customerRepository.findByUsername(username)
                .orElse(customerRepository.findByEmail(username).orElse(null));
        
        List<Order> orders;
        if (customer != null) {
            orders = orderRepository.findByCustomerAndFulfilledFalseOrderByOrderDateDesc(customer);
        } else {
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
            orders = orderRepository.findByUserAndFulfilledFalseOrderByOrderDateDesc(user);
        }
        
        return orders.stream()
                .map(order -> modelMapper.map(order, OrderDTO.class))
                .collect(Collectors.toList());
    }

    @Transactional
    public OrderDTO createOrder(CheckoutRequest checkoutRequest, String username) {
        logger.info("Starting order creation for user: {}", username);
        
        validateCheckoutRequest(checkoutRequest);
        
        // Find user (handle both customers and staff)
        Customer customer = customerRepository.findByUsername(username)
                .orElse(customerRepository.findByEmail(username).orElse(null));
        User user = null;
        
        // If not found as customer, try to find as staff user
        if (customer == null) {
            user = userRepository.findByUsername(username).orElse(null);
        }

        if (customer == null && user == null) {
            throw new UsernameNotFoundException("User not found with username: " + username);
        }

        Order order = new Order();
        if (customer != null) {
            order.setCustomer(customer);
        } else {
            order.setUser(user);
        }
        order.setOrderDate(LocalDateTime.now());

        // Process order items
        OrderProcessingResult result = processOrderItems(checkoutRequest.getCartItems(), order);
        
        // Apply coupon if provided
        BigDecimal finalAmount = applyCoupon(checkoutRequest.getCouponCode(), customer, user, result.originalAmount, order);
        
        order.setTotalAmount(finalAmount);

        // Save order
        Order savedOrder = orderRepository.save(order);
        logger.info("Order created successfully with ID: {} for user: {}", savedOrder.getId(), username);

        // Post-processing (async operations) - run after transaction commits
        final Customer finalCustomer = customer;
        final User finalUser = user;
        CompletableFuture.runAsync(() -> handlePostOrderProcessing(savedOrder, finalCustomer, finalUser));

        return modelMapper.map(savedOrder, OrderDTO.class);
    }

    @Transactional
    public void fulfillOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with id: " + orderId));
        
        order.setFulfilled(true);
        order.setFulfilledDate(LocalDateTime.now());
        orderRepository.save(order);
        
        // Update customer profile
        try {
            if (order.getUser() != null) {
                customerProfileService.updateProfileFromOrders(order.getUser().getId());
            }
            logger.info("Customer profile updated after order fulfillment for order: {}", orderId);
        } catch (Exception e) {
            logger.error("Failed to update customer profile after fulfillment for order: {}", orderId, e);
        }
    }

    // Private helper methods

    private User findUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
    }

    private void validateCheckoutRequest(CheckoutRequest request) {
        if (request.getCartItems() == null || request.getCartItems().isEmpty()) {
            throw new InvalidOrderException("Cart cannot be empty");
        }
        
        for (CartItemDTO item : request.getCartItems()) {
            if (item.getQuantity() <= 0) {
                throw new InvalidOrderException("Item quantity must be positive");
            }
        }
    }

    private OrderProcessingResult processOrderItems(List<CartItemDTO> cartItems, Order order) {
        Set<OrderItem> orderItems = new HashSet<>();
        BigDecimal originalAmount = BigDecimal.ZERO;

        logger.info("Processing {} items in cart", cartItems.size());
        
        for (CartItemDTO itemDTO : cartItems) {
            logger.info("Processing item: fruitId={}, quantity={}", itemDTO.getFruitId(), itemDTO.getQuantity());
            
            Fruit fruit = fruitRepository.findById(itemDTO.getFruitId())
                    .orElseThrow(() -> new ProductNotFoundException("Fruit not found with id: " + itemDTO.getFruitId()));

            // Validate stock
            if (fruit.getStock() < itemDTO.getQuantity()) {
                throw new InsufficientStockException(
                    String.format("Insufficient stock for %s. Requested: %d, Available: %d", 
                        fruit.getName(), itemDTO.getQuantity(), fruit.getStock()));
            }

            // Validate price
            if (fruit.getPrice() == null || fruit.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
                throw new InvalidPriceException("Product " + fruit.getName() + " has invalid price");
            }

            // Update stock
            fruit.setStock(fruit.getStock() - itemDTO.getQuantity());
            fruitRepository.save(fruit);
            logger.info("Stock updated for {}. New stock: {}", fruit.getName(), fruit.getStock());

            // Create order item
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setFruit(fruit);
            orderItem.setQuantity(itemDTO.getQuantity());
            orderItem.setPrice(fruit.getPrice());
            orderItems.add(orderItem);

            BigDecimal itemTotal = fruit.getPrice().multiply(BigDecimal.valueOf(itemDTO.getQuantity()));
            originalAmount = originalAmount.add(itemTotal);
            logger.info("Item {} processed. Item total: {}", fruit.getName(), itemTotal);
        }

        order.setOrderItems(orderItems);
        order.setOriginalAmount(originalAmount);
        
        return new OrderProcessingResult(orderItems, originalAmount);
    }

    private BigDecimal applyCoupon(String couponCode, Customer customer, User user, BigDecimal originalAmount, Order order) {
        if (couponCode == null || couponCode.trim().isEmpty()) {
            return originalAmount;
        }

        try {
            Customer orderCustomer = customer != null ? customer : 
                customerRepository.findByUsername(user.getUsername()).orElse(null);
                
            if (orderCustomer == null) {
                logger.warn("No customer found for coupon application for user: {}", user.getUsername());
                return originalAmount;
            }

            CouponService.CouponValidationResult validation = 
                couponService.validateCouponDetailed(couponCode, orderCustomer, originalAmount);
            
            if (validation.isValid()) {
                BigDecimal discountAmount = validation.getDiscountAmount();
                BigDecimal finalAmount = originalAmount.subtract(discountAmount);
                
                order.setAppliedCoupon(validation.getCoupon());
                order.setDiscountAmount(discountAmount);
                
                couponService.useCoupon(validation.getCoupon(), orderCustomer);
                logger.info("Coupon {} applied successfully. Discount: {}", couponCode, discountAmount);
                
                return finalAmount;
            } else {
                logger.warn("Invalid coupon code: {} - {}", couponCode, validation.getErrorMessage());
                return originalAmount;
            }
        } catch (Exception e) {
            logger.error("Error processing coupon {}: {}", couponCode, e.getMessage());
            return originalAmount;
        }
    }

    private void handlePostOrderProcessing(Order order, Customer customer, User user) {
        // Update customer profile (non-blocking)
        try {
            if (customer != null) {
                customerProfileService.updateProfileFromOrders(customer.getId());
                logger.info("Customer profile updated for customer: {}", customer.getUsername());
            } else if (user != null) {
                customerProfileService.updateProfileFromOrders(user.getId());
                logger.info("Customer profile updated for user: {}", user.getUsername());
            }
        } catch (Exception e) {
            String username = customer != null ? customer.getUsername() : (user != null ? user.getUsername() : "unknown");
            logger.error("Failed to update customer profile for user: {}", username, e);
        }

        // Send confirmation email (non-blocking)
        try {
            sendOrderConfirmationEmail(order, customer, user);
        } catch (Exception e) {
            logger.error("Failed to send confirmation email for order: {}", order.getId(), e);
        }
    }

    private void sendOrderConfirmationEmail(Order order, Customer customer, User user) {
        String username = customer != null ? customer.getUsername() : (user != null ? user.getUsername() : "Customer");
        String email = customer != null ? customer.getEmail() : (user != null ? user.getEmail() : null);
        
        if (email == null) {
            logger.warn("No email address found for order confirmation: {}", order.getId());
            return;
        }
        
        String subject = "Order Confirmation #" + order.getId();
        String message = String.format(
            "Dear %s,\n\nThank you for your order!\n\n" +
            "Order Details:\n" +
            "Order ID: %d\n" +
            "Total Amount: $%.2f\n" +
            "Order Date: %s\n\n" +
            "We'll send you updates as your order is processed.\n\n" +
            "Best regards,\nYour Grocery Store Team",
            username,
            order.getId(),
            order.getTotalAmount(),
            order.getOrderDate()
        );
        
        try {
            emailService.sendSimpleMessage(email, subject, message);
            logger.info("Confirmation email sent for order: {}", order.getId());
        } catch (Exception emailError) {
            logger.warn("Failed to send confirmation email for order {}: {}", order.getId(), emailError.getMessage());
            // Don't fail the entire order process if email fails
        }
    }

    // Inner classes for better organization
    private static class OrderProcessingResult {
        final Set<OrderItem> orderItems;
        final BigDecimal originalAmount;

        OrderProcessingResult(Set<OrderItem> orderItems, BigDecimal originalAmount) {
            this.orderItems = orderItems;
            this.originalAmount = originalAmount;
        }
    }

    // Custom exceptions for better error handling
    public static class OrderNotFoundException extends RuntimeException {
        public OrderNotFoundException(String message) {
            super(message);
        }
    }

    public static class InvalidOrderException extends RuntimeException {
        public InvalidOrderException(String message) {
            super(message);
        }
    }

    public static class ProductNotFoundException extends RuntimeException {
        public ProductNotFoundException(String message) {
            super(message);
        }
    }

    public static class InsufficientStockException extends RuntimeException {
        public InsufficientStockException(String message) {
            super(message);
        }
    }

    public static class InvalidPriceException extends RuntimeException {
        public InvalidPriceException(String message) {
            super(message);
        }
    }
} 