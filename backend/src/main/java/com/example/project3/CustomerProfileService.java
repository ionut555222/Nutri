package com.example.project3;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class CustomerProfileService {

    @Autowired
    private CustomerProfileRepository customerProfileRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private CustomerRepository customerRepository;
    


    public CustomerProfile getOrCreateProfile(Long customerId) {
        return customerProfileRepository.findByCustomerId(customerId)
                .orElse(createNewProfile(customerId));
    }

    public CustomerProfile getOrCreateProfile(String username) {
        return customerProfileRepository.findByCustomerUsername(username)
                .orElseGet(() -> {
                    Customer customer = customerRepository.findByUsername(username)
                            .orElseThrow(() -> new RuntimeException("Customer not found: " + username));
                    return createNewProfile(customer.getId());
                });
    }

    private CustomerProfile createNewProfile(Long customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found with id: " + customerId));
        
        CustomerProfile profile = new CustomerProfile();
        profile.setCustomer(customer);
        profile.setCustomerSegment("New");
        profile.setRiskLevel("Low");
        profile.setPriceSensitivity("Medium");
        
        return customerProfileRepository.save(profile);
    }

    public CustomerProfile updateProfileFromOrders(Long customerId) {
        CustomerProfile profile = getOrCreateProfile(customerId);
        
        List<Order> orders = orderRepository.findByCustomerId(customerId);
        
        if (orders.isEmpty()) {
            return profile;
        }

        // Calculate basic metrics
        calculateBasicMetrics(profile, orders);
        
        // Analyze category preferences
        analyzeCategoryPreferences(profile, orders);
        
        // Generate AI insights
        generateAIInsights(profile, orders);
        
        // Update customer segment
        updateCustomerSegment(profile);
        
        // Calculate risk level
        calculateRiskLevel(profile);
        
        profile.setProfileUpdatedAt(LocalDateTime.now());
        
        return customerProfileRepository.save(profile);
    }

    private void calculateBasicMetrics(CustomerProfile profile, List<Order> orders) {
        profile.setTotalOrders(orders.size());
        
        BigDecimal totalSpent = orders.stream()
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        profile.setTotalSpent(totalSpent);
        
        if (orders.size() > 0) {
            profile.setAverageOrderValue(totalSpent.divide(
                    BigDecimal.valueOf(orders.size()), 2, RoundingMode.HALF_UP));
        }
        
        // Sort orders by date
        orders.sort(Comparator.comparing(Order::getOrderDate));
        
        profile.setFirstOrderDate(orders.get(0).getOrderDate());
        profile.setLastOrderDate(orders.get(orders.size() - 1).getOrderDate());
        
        // Calculate order frequency
        if (orders.size() > 1) {
            long daysBetweenFirstAndLast = ChronoUnit.DAYS.between(
                    profile.getFirstOrderDate(), profile.getLastOrderDate());
            if (daysBetweenFirstAndLast > 0) {
                profile.setOrderFrequencyDays((double) daysBetweenFirstAndLast / (orders.size() - 1));
            }
        }
    }

    private void analyzeCategoryPreferences(CustomerProfile profile, List<Order> orders) {
        Map<String, BigDecimal> categorySpending = new HashMap<>();
        Map<String, Integer> categoryCount = new HashMap<>();
        
        for (Order order : orders) {
            for (OrderItem item : order.getOrderItems()) {
                String categoryName = item.getFruit().getCategory().getName();
                BigDecimal itemTotal = item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
                
                categorySpending.merge(categoryName, itemTotal, BigDecimal::add);
                categoryCount.merge(categoryName, 1, Integer::sum);
            }
        }
        
        profile.setCategorySpending(categorySpending);
        
        // Determine favorite categories (top 3)
        List<String> favoriteCategories = categorySpending.entrySet().stream()
                .sorted(Map.Entry.<String, BigDecimal>comparingByValue().reversed())
                .limit(3)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
        
        profile.setFavoriteCategories(favoriteCategories);
    }

    private void generateAIInsights(CustomerProfile profile, List<Order> orders) {
        try {
            // Generate AI insights without external API call for now
            String aiResponse = "Based on customer behavior analysis:\n" +
                    "Customer shows " + determinePriceSensitivity(profile) + " price sensitivity.\n" +
                    "Preferred shopping pattern: " + determineShoppingPattern(profile) + ".\n" +
                    "Seasonal preferences: " + analyzeSeasonalPreferences(orders) + ".";
            
            profile.setAiPersonalityProfile(aiResponse);
            profile.setPriceSensitivity(determinePriceSensitivity(profile));
            profile.setSeasonalPreferences(analyzeSeasonalPreferences(orders));
            
        } catch (Exception e) {
            // Fallback analysis without AI
            profile.setPriceSensitivity(determinePriceSensitivity(profile));
            profile.setSeasonalPreferences("Analysis pending");
        }
    }



    private String determinePriceSensitivity(CustomerProfile profile) {
        if (profile.getAverageOrderValue().compareTo(BigDecimal.valueOf(100)) > 0) {
            return "Low";
        } else if (profile.getAverageOrderValue().compareTo(BigDecimal.valueOf(50)) > 0) {
            return "Medium";
        } else {
            return "High";
        }
    }

    private String determineShoppingPattern(CustomerProfile profile) {
        if (profile.getOrderFrequencyDays() <= 7) {
            return "Frequent buyer";
        } else if (profile.getOrderFrequencyDays() <= 30) {
            return "Regular buyer";
        } else {
            return "Occasional buyer";
        }
    }

    private String analyzeSeasonalPreferences(List<Order> orders) {
        Map<String, Integer> monthlyOrders = new HashMap<>();
        
        for (Order order : orders) {
            String month = order.getOrderDate().getMonth().toString();
            monthlyOrders.merge(month, 1, Integer::sum);
        }
        
        return monthlyOrders.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(entry -> "Most active in " + entry.getKey())
                .orElse("No clear seasonal pattern");
    }

    private void updateCustomerSegment(CustomerProfile profile) {
        BigDecimal totalSpent = profile.getTotalSpent();
        int totalOrders = profile.getTotalOrders();
        
        if (totalSpent.compareTo(BigDecimal.valueOf(500)) >= 0 && totalOrders >= 10) {
            profile.setCustomerSegment("Premium");
        } else if (totalSpent.compareTo(BigDecimal.valueOf(200)) >= 0 && totalOrders >= 5) {
            profile.setCustomerSegment("Regular");  
        } else if (totalOrders >= 1) {
            profile.setCustomerSegment("Budget");
        } else {
            profile.setCustomerSegment("New");
        }
    }

    private void calculateRiskLevel(CustomerProfile profile) {
        LocalDateTime now = LocalDateTime.now();
        
        if (profile.getLastOrderDate() == null) {
            profile.setRiskLevel("Low");
            return;
        }
        
        long daysSinceLastOrder = ChronoUnit.DAYS.between(profile.getLastOrderDate(), now);
        
        if (daysSinceLastOrder > 90) {
            profile.setRiskLevel("High");
        } else if (daysSinceLastOrder > 30) {
            profile.setRiskLevel("Medium");
        } else {
            profile.setRiskLevel("Low");
        }
    }

    public void updateAllProfiles() {
        List<Customer> customers = customerRepository.findAll();
        
        for (Customer customer : customers) {
            try {
                updateProfileFromOrders(customer.getId());
            } catch (Exception e) {
                // Log error and continue with next customer
                System.err.println("Error updating profile for customer " + customer.getId() + ": " + e.getMessage());
            }
        }
    }

    public List<CustomerProfile> getProfilesBySegment(String segment) {
        return customerProfileRepository.findByCustomerSegment(segment);
    }

    public List<CustomerProfile> getAtRiskCustomers() {
        return customerProfileRepository.findByRiskLevel("High");
    }

    public List<CustomerProfile> getCustomersNeedingReEngagement(int daysSinceLastEmail) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysSinceLastEmail);
        return customerProfileRepository.findCustomersNeedingEmail(cutoffDate);
    }

    public void recordEmailActivity(Long customerId, String activityType) {
        CustomerProfile profile = getOrCreateProfile(customerId);
        
        switch (activityType.toLowerCase()) {
            case "sent":
                profile.setEmailsSent(profile.getEmailsSent() + 1);
                profile.setLastEmailSent(LocalDateTime.now());
                break;
            case "opened":
                profile.setEmailsOpened(profile.getEmailsOpened() + 1);
                break;
            case "clicked":
                profile.setEmailsClicked(profile.getEmailsClicked() + 1);
                break;
            case "optout":
                profile.setOptOut(true);
                break;
        }
        
        customerProfileRepository.save(profile);
    }

    public void createInitialCustomerProfile(Customer customer) {
        // Check if profile already exists
        if (customerProfileRepository.findByCustomerId(customer.getId()).isPresent()) {
            return; // Profile already exists, skip creation
        }
        
        CustomerProfile profile = new CustomerProfile();
        profile.setCustomer(customer);
        profile.setCustomerSegment("New");
        profile.setRiskLevel("Low");
        profile.setPriceSensitivity("Medium");
        profile.setPreferredCommunicationTime("Evening");
        
        customerProfileRepository.save(profile);
        customer.setProfile(profile);
        customerRepository.save(customer);
    }
} 