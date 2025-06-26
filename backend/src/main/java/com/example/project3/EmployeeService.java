package com.example.project3;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class EmployeeService {

    @Autowired
    private FruitRepository fruitRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private CustomerProfileRepository customerProfileRepository;

    @Autowired
    private EmailCampaignRepository emailCampaignRepository;

    @Autowired
    private CustomerProfileService customerProfileService;

    @Autowired
    private AIEmailMarketingService aiEmailMarketingService;

    @Autowired
    private EmailService emailService;

    // Dashboard Analytics for Employees
    public EmployeeDashboardData getDashboardData() {
        EmployeeDashboardData data = new EmployeeDashboardData();
        data.setTotalProducts((int) fruitRepository.count());
        data.setPendingOrders(orderRepository.findAllByFulfilledFalse().size());
        data.setTotalCustomers((int) customerProfileRepository.count());
        data.setActiveCampaigns(emailCampaignRepository.findActiveCampaigns().size());
        
        // Customer segment breakdown
        data.setPremiumCustomers(customerProfileRepository.findByCustomerSegment("Premium").size());
        data.setRegularCustomers(customerProfileRepository.findByCustomerSegment("Regular").size());
        data.setBudgetCustomers(customerProfileRepository.findByCustomerSegment("Budget").size());
        data.setNewCustomers(customerProfileRepository.findByCustomerSegment("New").size());
        
        return data;
    }

    // Product Management (Limited permissions for employees)
    public List<Fruit> getAllProducts() {
        return fruitRepository.findAll();
    }

    public Fruit getProductById(Long id) {
        return fruitRepository.findById(id).orElse(null);
    }

    public Fruit updateProduct(Fruit product) {
        // Employees can update product details but not create/delete
        return fruitRepository.save(product);
    }

    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    // Order Management
    public List<Order> getPendingOrders() {
        return orderRepository.findAllByFulfilledFalse();
    }

    public List<Order> getRecentOrders(int limit) {
        return orderRepository.findAll().stream()
                .sorted((o1, o2) -> o2.getOrderDate().compareTo(o1.getOrderDate()))
                .limit(limit)
                .collect(Collectors.toList());
    }

    public Order fulfillOrder(Long orderId) {
        Order order = orderRepository.findById(orderId).orElse(null);
        if (order != null) {
            order.setFulfilled(true);
            order = orderRepository.save(order);
            
            // Update customer profile after fulfillment
            customerProfileService.updateProfileFromOrders(order.getUser().getId());
        }
        return order;
    }

    public Order getOrderById(Long orderId) {
        return orderRepository.findById(orderId).orElse(null);
    }

    // Customer Management (Read access with limited write)
    public List<CustomerProfile> getAllCustomers() {
        return customerProfileRepository.findAll();
    }

    public CustomerProfile getCustomerProfile(Long profileId) {
        return customerProfileRepository.findById(profileId).orElse(null);
    }

    public List<CustomerProfile> getCustomersBySegment(String segment) {
        return customerProfileRepository.findByCustomerSegment(segment);
    }

    public List<String> getAllCustomerSegments() {
        return customerProfileRepository.findAllCustomerSegments();
    }

    public List<Order> getCustomerOrders(Long customerId) {
        return orderRepository.findByUserId(customerId);
    }

    // Email Campaign Management (Limited to certain types)
    public List<EmailCampaign> getAllCampaigns() {
        return emailCampaignRepository.findAll();
    }

    public List<EmailCampaign> getActiveCampaigns() {
        return aiEmailMarketingService.getActiveCampaigns();
    }

    public List<EmailCampaign> getEmployeeCampaigns() {
        return emailCampaignRepository.findAll().stream()
                .filter(campaign -> "employee".equals(campaign.getCreatedBy()) || 
                        isAllowedCampaignType(campaign.getCampaignType()))
                .collect(Collectors.toList());
    }

    public EmailCampaign createCampaign(String campaignName, String campaignType, String targetSegment) {
        if (!isAllowedCampaignType(campaignType)) {
            throw new IllegalArgumentException("Campaign type not allowed for employees: " + campaignType);
        }
        
        return aiEmailMarketingService.createAIGeneratedCampaign(
                campaignName, campaignType, targetSegment, "employee");
    }

    public void sendCampaign(Long campaignId) {
        EmailCampaign campaign = emailCampaignRepository.findById(campaignId).orElse(null);
        if (campaign == null) {
            throw new IllegalArgumentException("Campaign not found");
        }
        
        // Check if employee can send this campaign
        if (!canEmployeeSendCampaign(campaign)) {
            throw new IllegalArgumentException("Employee cannot send this campaign");
        }
        
        aiEmailMarketingService.sendCampaign(campaignId);
    }

    public void sendQuickEmail(Long customerId, String campaignType) {
        CustomerProfile profile = customerProfileRepository.findById(customerId).orElse(null);
        if (profile == null) {
            throw new IllegalArgumentException("Customer not found");
        }

        if (!isAllowedCampaignType(campaignType)) {
            throw new IllegalArgumentException("Campaign type not allowed for employees");
        }

        // Create temporary campaign
        EmailCampaign tempCampaign = aiEmailMarketingService.createAIGeneratedCampaign(
                "Quick Email to " + profile.getUser().getUsername(),
                campaignType,
                profile.getCustomerSegment(),
                "employee"
        );

        String personalizedSubject = aiEmailMarketingService.personalizeEmailContent(
                tempCampaign.getSubjectTemplate(), profile);
        String personalizedContent = aiEmailMarketingService.personalizeEmailContent(
                tempCampaign.getContentTemplate(), profile);

        emailService.sendSimpleMessage(profile.getUser().getEmail(), personalizedSubject, personalizedContent);
        customerProfileService.recordEmailActivity(customerId, "sent");
    }

    // Reports and Analytics (Read-only)
    public EmployeeReportData getReportData() {
        EmployeeReportData data = new EmployeeReportData();
        data.setTotalOrders((int) orderRepository.count());
        data.setPendingOrders(orderRepository.findAllByFulfilledFalse().size());
        data.setTotalCustomers((int) customerProfileRepository.count());
        data.setCampaignsSent(emailCampaignRepository.findCompletedCampaigns().size());
        
        // Segment breakdown
        data.setPremiumCustomers(customerProfileRepository.findByCustomerSegment("Premium").size());
        data.setRegularCustomers(customerProfileRepository.findByCustomerSegment("Regular").size());
        data.setBudgetCustomers(customerProfileRepository.findByCustomerSegment("Budget").size());
        data.setNewCustomers(customerProfileRepository.findByCustomerSegment("New").size());
        
        return data;
    }

    // Helper methods for permission checking
    private boolean isAllowedCampaignType(String campaignType) {
        return campaignType.equals("WELCOME") || 
               campaignType.equals("PROMOTIONAL") || 
               campaignType.equals("SEASONAL");
        // Employees cannot create RE_ENGAGEMENT or PERSONALIZED campaigns
    }

    private boolean canEmployeeSendCampaign(EmailCampaign campaign) {
        return "employee".equals(campaign.getCreatedBy()) || 
               isAllowedCampaignType(campaign.getCampaignType());
    }

    // Data classes for structured responses
    public static class EmployeeDashboardData {
        private int totalProducts;
        private int pendingOrders;
        private int totalCustomers;
        private int activeCampaigns;
        private int premiumCustomers;
        private int regularCustomers;
        private int budgetCustomers;
        private int newCustomers;

        // Getters and setters
        public int getTotalProducts() { return totalProducts; }
        public void setTotalProducts(int totalProducts) { this.totalProducts = totalProducts; }

        public int getPendingOrders() { return pendingOrders; }
        public void setPendingOrders(int pendingOrders) { this.pendingOrders = pendingOrders; }

        public int getTotalCustomers() { return totalCustomers; }
        public void setTotalCustomers(int totalCustomers) { this.totalCustomers = totalCustomers; }

        public int getActiveCampaigns() { return activeCampaigns; }
        public void setActiveCampaigns(int activeCampaigns) { this.activeCampaigns = activeCampaigns; }

        public int getPremiumCustomers() { return premiumCustomers; }
        public void setPremiumCustomers(int premiumCustomers) { this.premiumCustomers = premiumCustomers; }

        public int getRegularCustomers() { return regularCustomers; }
        public void setRegularCustomers(int regularCustomers) { this.regularCustomers = regularCustomers; }

        public int getBudgetCustomers() { return budgetCustomers; }
        public void setBudgetCustomers(int budgetCustomers) { this.budgetCustomers = budgetCustomers; }

        public int getNewCustomers() { return newCustomers; }
        public void setNewCustomers(int newCustomers) { this.newCustomers = newCustomers; }
    }

    public static class EmployeeReportData {
        private int totalOrders;
        private int pendingOrders;
        private int totalCustomers;
        private int campaignsSent;
        private int premiumCustomers;
        private int regularCustomers;
        private int budgetCustomers;
        private int newCustomers;

        // Getters and setters
        public int getTotalOrders() { return totalOrders; }
        public void setTotalOrders(int totalOrders) { this.totalOrders = totalOrders; }

        public int getPendingOrders() { return pendingOrders; }
        public void setPendingOrders(int pendingOrders) { this.pendingOrders = pendingOrders; }

        public int getTotalCustomers() { return totalCustomers; }
        public void setTotalCustomers(int totalCustomers) { this.totalCustomers = totalCustomers; }

        public int getCampaignsSent() { return campaignsSent; }
        public void setCampaignsSent(int campaignsSent) { this.campaignsSent = campaignsSent; }

        public int getPremiumCustomers() { return premiumCustomers; }
        public void setPremiumCustomers(int premiumCustomers) { this.premiumCustomers = premiumCustomers; }

        public int getRegularCustomers() { return regularCustomers; }
        public void setRegularCustomers(int regularCustomers) { this.regularCustomers = regularCustomers; }

        public int getBudgetCustomers() { return budgetCustomers; }
        public void setBudgetCustomers(int budgetCustomers) { this.budgetCustomers = budgetCustomers; }

        public int getNewCustomers() { return newCustomers; }
        public void setNewCustomers(int newCustomers) { this.newCustomers = newCustomers; }
    }
} 