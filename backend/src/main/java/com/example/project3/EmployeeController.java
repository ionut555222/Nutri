package com.example.project3;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/employee")
public class EmployeeController {

    @Autowired
    private FruitRepository fruitRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private CustomerProfileService customerProfileService;

    @Autowired
    private AIEmailMarketingService aiEmailMarketingService;

    @Autowired
    private CustomerProfileRepository customerProfileRepository;

    @Autowired
    private EmailCampaignRepository emailCampaignRepository;

    @Autowired
    private EmailService emailService;

    @GetMapping
    @PreAuthorize("hasRole('EMPLOYEE')")
    public String employeeDashboard(Model model) {
        // Employee dashboard with key metrics
        model.addAttribute("totalProducts", fruitRepository.count());
        model.addAttribute("pendingOrders", orderRepository.findAllByFulfilledFalse().size());
        model.addAttribute("totalCustomers", customerProfileRepository.count());
        model.addAttribute("activeCampaigns", emailCampaignRepository.findActiveCampaigns().size());
        
        return "employee/dashboard";
    }

    // Product Management (Employee can view and edit, but not delete)
    @GetMapping("/products")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public String manageProducts(Model model) {
        model.addAttribute("fruits", fruitRepository.findAll());
        model.addAttribute("categories", categoryRepository.findAll());
        return "employee/products";
    }

    @GetMapping("/products/edit/{id}")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public String editProduct(@PathVariable Long id, Model model) {
        Fruit fruit = fruitRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid product Id:" + id));
        model.addAttribute("fruit", fruit);
        model.addAttribute("categories", categoryRepository.findAll());
        return "employee/product_form";
    }

    @PostMapping("/products/update/{id}")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public String updateProduct(@PathVariable Long id, @ModelAttribute("fruit") Fruit fruitDetails, 
                               RedirectAttributes redirectAttributes) {
        try {
            Fruit fruit = fruitRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid product Id:" + id));
            fruit.setName(fruitDetails.getName());
            fruit.setDescription(fruitDetails.getDescription());
            fruit.setPrice(fruitDetails.getPrice());
            fruit.setStock(fruitDetails.getStock());
            fruit.setUnit(fruitDetails.getUnit());
            // Note: Category changes might require admin approval
            
            fruitRepository.save(fruit);
            redirectAttributes.addFlashAttribute("message", "Product updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", "Error updating product: " + e.getMessage());
        }
        return "redirect:/employee/products";
    }

    // Order Management
    @GetMapping("/orders")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public String manageOrders(Model model) {
        model.addAttribute("pendingOrders", orderRepository.findAllByFulfilledFalse());
        model.addAttribute("recentOrders", orderRepository.findAll().stream()
                .sorted((o1, o2) -> o2.getOrderDate().compareTo(o1.getOrderDate()))
                .limit(10)
                .toList());
        return "employee/orders";
    }

    @PostMapping("/orders/fulfill/{id}")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public String fulfillOrder(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Order order = orderRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid order Id:" + id));
            order.setFulfilled(true);
            orderRepository.save(order);
            
            // Update customer profile after order fulfillment
            customerProfileService.updateProfileFromOrders(order.getUser().getId());
            
            redirectAttributes.addFlashAttribute("message", "Order fulfilled successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", "Error fulfilling order: " + e.getMessage());
        }
        return "redirect:/employee/orders";
    }

    // Customer Support
    @GetMapping("/customers")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public String viewCustomers(Model model) {
        model.addAttribute("customers", customerProfileRepository.findAll());
        model.addAttribute("segments", customerProfileRepository.findAllCustomerSegments());
        return "employee/customers";
    }

    @GetMapping("/customers/{id}")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public String viewCustomerProfile(@PathVariable Long id, Model model) {
        CustomerProfile profile = customerProfileRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid profile Id:" + id));
        model.addAttribute("profile", profile);
        model.addAttribute("user", profile.getUser());
        model.addAttribute("orders", orderRepository.findByUserId(profile.getUser().getId()));
        return "employee/customer_profile";
    }

    // Email Campaigns (Employee can create and send, but with limitations)
    @GetMapping("/campaigns")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public String manageCampaigns(Model model) {
        model.addAttribute("campaigns", emailCampaignRepository.findAll());
        model.addAttribute("activeCampaigns", aiEmailMarketingService.getActiveCampaigns());
        return "employee/campaigns";
    }

    @GetMapping("/campaigns/create")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public String createCampaignForm(Model model) {
        model.addAttribute("segments", customerProfileRepository.findAllCustomerSegments());
        return "employee/campaign_form";
    }

    @PostMapping("/campaigns/create")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public String createCampaign(@RequestParam("campaignName") String campaignName,
                                @RequestParam("campaignType") String campaignType,
                                @RequestParam("targetSegment") String targetSegment,
                                RedirectAttributes redirectAttributes) {
        try {
            // Employees can only create certain types of campaigns
            if (!isAllowedCampaignType(campaignType)) {
                redirectAttributes.addFlashAttribute("message", "Campaign type not allowed for employees. Please contact admin.");
                return "redirect:/employee/campaigns";
            }
            
            EmailCampaign campaign = aiEmailMarketingService.createAIGeneratedCampaign(
                    campaignName, campaignType, targetSegment, "employee");
            
            redirectAttributes.addFlashAttribute("message", 
                    "Campaign '" + campaignName + "' created successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", "Error creating campaign: " + e.getMessage());
        }
        
        return "redirect:/employee/campaigns";
    }

    @PostMapping("/campaigns/send/{id}")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public String sendCampaign(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            EmailCampaign campaign = emailCampaignRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid campaign Id:" + id));
            
            // Employees can only send campaigns they created or certain types
            if (!campaign.getCreatedBy().equals("employee") && !isAllowedCampaignType(campaign.getCampaignType())) {
                redirectAttributes.addFlashAttribute("message", "You don't have permission to send this campaign.");
                return "redirect:/employee/campaigns";
            }
            
            aiEmailMarketingService.sendCampaign(id);
            redirectAttributes.addFlashAttribute("message", "Campaign sent successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", "Error sending campaign: " + e.getMessage());
        }
        return "redirect:/employee/campaigns";
    }

    // Quick Actions
    @GetMapping("/quick-email")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public String quickEmailForm(Model model) {
        model.addAttribute("customers", customerProfileRepository.findAll());
        model.addAttribute("segments", customerProfileRepository.findAllCustomerSegments());
        return "employee/quick_email";
    }

    @PostMapping("/send-quick-email")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public String sendQuickEmail(@RequestParam("customerId") Long customerId,
                                @RequestParam("campaignType") String campaignType,
                                RedirectAttributes redirectAttributes) {
        try {
            CustomerProfile profile = customerProfileRepository.findById(customerId)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid customer Id:" + customerId));
            
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

            redirectAttributes.addFlashAttribute("message", 
                    "Email sent successfully to " + profile.getUser().getUsername());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", "Error sending email: " + e.getMessage());
        }

        return "redirect:/employee/quick-email";
    }

    // Reports (Read-only for employees)
    @GetMapping("/reports")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public String viewReports(Model model) {
        model.addAttribute("totalOrders", orderRepository.count());
        model.addAttribute("pendingOrders", orderRepository.findAllByFulfilledFalse().size());
        model.addAttribute("totalCustomers", customerProfileRepository.count());
        model.addAttribute("campaignsSent", emailCampaignRepository.findCompletedCampaigns().size());
        
        // Segment breakdown
        model.addAttribute("premiumCustomers", customerProfileRepository.findByCustomerSegment("Premium").size());
        model.addAttribute("regularCustomers", customerProfileRepository.findByCustomerSegment("Regular").size());
        model.addAttribute("budgetCustomers", customerProfileRepository.findByCustomerSegment("Budget").size());
        model.addAttribute("newCustomers", customerProfileRepository.findByCustomerSegment("New").size());
        
        return "employee/reports";
    }

    // Helper method to check allowed campaign types for employees
    private boolean isAllowedCampaignType(String campaignType) {
        return campaignType.equals("WELCOME") || 
               campaignType.equals("PROMOTIONAL") || 
               campaignType.equals("SEASONAL");
        // Employees cannot create RE_ENGAGEMENT or PERSONALIZED campaigns
    }
} 