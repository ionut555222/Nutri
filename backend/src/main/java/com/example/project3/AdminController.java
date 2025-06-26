package com.example.project3;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private EmailService emailService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FruitRepository fruitRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

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
    private CustomerRepository customerRepository;

    @Autowired
    private AnalyticsService analyticsService;

    @Autowired
    private CouponService couponService;

    @Autowired
    private CouponRepository couponRepository;

    @Value("${app.upload.dir:uploads/}")
    private String uploadDir;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('EMPLOYEE')")
    public String adminHome() {
        return "admin/index";
    }

    @GetMapping("/groceries")
    @PreAuthorize("hasRole('ADMIN') or hasRole('EMPLOYEE')")
    public String groceriesAdmin(Model model) {
        model.addAttribute("fruits", fruitRepository.findAll());
        List<OrderDTO> pendingOrderDTOs = convertToOrderDTOs(orderRepository.findAllByFulfilledFalse());
        model.addAttribute("orders", pendingOrderDTOs);
        return "admin/groceries";
    }

    @GetMapping("/orders/fulfilled")
    @PreAuthorize("hasRole('ADMIN') or hasRole('EMPLOYEE')")
    public String fulfilledOrders(Model model) {
        List<OrderDTO> fulfilledOrderDTOs = convertToOrderDTOs(orderRepository.findAllByFulfilledTrue());
        model.addAttribute("orders", fulfilledOrderDTOs);
        return "admin/fulfilled_orders";
    }

    @GetMapping("/orders/all")
    @PreAuthorize("hasRole('ADMIN') or hasRole('EMPLOYEE')")
    public String allOrders(Model model) {
        List<OrderDTO> allOrderDTOs = convertToOrderDTOs(orderRepository.findAll());
        List<OrderDTO> fulfilledOrderDTOs = convertToOrderDTOs(orderRepository.findAllByFulfilledTrue());
        List<OrderDTO> pendingOrderDTOs = convertToOrderDTOs(orderRepository.findAllByFulfilledFalse());
        
        model.addAttribute("allOrders", allOrderDTOs);
        model.addAttribute("fulfilledOrders", fulfilledOrderDTOs);
        model.addAttribute("pendingOrders", pendingOrderDTOs);
        return "admin/all_orders";
    }

    @GetMapping("/orders/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('EMPLOYEE')")
    public String orderDetail(@PathVariable Long id, Model model) {
        try {
            Order order = orderRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid order Id:" + id));
            
            OrderDTO orderDTO = convertToOrderDTO(order);
            model.addAttribute("order", orderDTO);
            model.addAttribute("canFulfill", !order.isFulfilled());
            return "admin/order_detail";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Error loading order details: " + e.getMessage());
            return "error";
        }
    }

    @PostMapping("/orders/{id}/fulfill")
    @PreAuthorize("hasRole('ADMIN') or hasRole('EMPLOYEE')")
    public String fulfillOrder(@PathVariable Long id, 
                               @RequestParam(value = "notes", required = false) String notes,
                               RedirectAttributes redirectAttributes) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid order Id:" + id));
        
        order.setFulfilled(true);
        order.setFulfilledDate(LocalDateTime.now());
        if (notes != null && !notes.trim().isEmpty()) {
            order.setOrderNotes(notes);
        }
        orderRepository.save(order);
        
        redirectAttributes.addFlashAttribute("message", "Order #" + id + " has been fulfilled successfully!");
        return "redirect:/admin/orders/" + id;
    }

    @PostMapping("/orders/{id}/unfulfill")
    @PreAuthorize("hasRole('ADMIN')")
    public String unfulfillOrder(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid order Id:" + id));
        
        order.setFulfilled(false);
        order.setFulfilledDate(null);
        orderRepository.save(order);
        
        redirectAttributes.addFlashAttribute("message", "Order #" + id + " has been marked as unfulfilled!");
        return "redirect:/admin/orders/" + id;
    }

    @GetMapping("/api/orders/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('EMPLOYEE')")
    @ResponseBody
    public ResponseEntity<OrderDTO> getOrderDetails(@PathVariable Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid order Id:" + id));
        
        OrderDTO orderDTO = convertToOrderDTO(order);
        return ResponseEntity.ok(orderDTO);
    }

    @GetMapping("/groceries/new")
    @PreAuthorize("hasRole('ADMIN') or hasRole('EMPLOYEE')")
    public String showAddFruitForm(Model model) {
        model.addAttribute("fruit", new Fruit());
        model.addAttribute("categories", categoryRepository.findAll());
        return "admin/fruit_form";
    }

    @PostMapping("/groceries/save")
    @PreAuthorize("hasRole('ADMIN') or hasRole('EMPLOYEE')")
    public String saveFruit(@ModelAttribute("fruit") Fruit fruit, 
                           @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                           RedirectAttributes redirectAttributes) {
        try {
            // Handle image upload
            if (imageFile != null && !imageFile.isEmpty()) {
                String imageUrl = uploadProductImage(imageFile);
                fruit.setImageUrl(imageUrl);
                fruit.setImageFilename(extractFilenameFromUrl(imageUrl));
            }
            
            fruitRepository.save(fruit);
            redirectAttributes.addFlashAttribute("message", "Product saved successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error saving product: " + e.getMessage());
        }
        return "redirect:/admin/groceries";
    }

    @GetMapping("/groceries/edit/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('EMPLOYEE')")
    public String showEditFruitForm(@PathVariable Long id, Model model) {
        Fruit fruit = fruitRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid fruit Id:" + id));
        model.addAttribute("fruit", fruit);
        model.addAttribute("categories", categoryRepository.findAll());
        return "admin/fruit_form";
    }

    @PostMapping("/groceries/update/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('EMPLOYEE')")
    public String updateFruit(@PathVariable Long id, 
                             @ModelAttribute("fruit") Fruit fruitDetails,
                             @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                             RedirectAttributes redirectAttributes) {
        try {
            Fruit fruit = fruitRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid fruit Id:" + id));
            
            fruit.setName(fruitDetails.getName());
            fruit.setPrice(fruitDetails.getPrice());
            fruit.setStock(fruitDetails.getStock());
            fruit.setCategory(fruitDetails.getCategory());
            fruit.setDescription(fruitDetails.getDescription());
            fruit.setUnit(fruitDetails.getUnit());
            
            // Handle image upload
            if (imageFile != null && !imageFile.isEmpty()) {
                String imageUrl = uploadProductImage(imageFile);
                fruit.setImageUrl(imageUrl);
                fruit.setImageFilename(extractFilenameFromUrl(imageUrl));
            }
            
            fruitRepository.save(fruit);
            redirectAttributes.addFlashAttribute("message", "Product updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error updating product: " + e.getMessage());
        }
        return "redirect:/admin/groceries";
    }

    @GetMapping("/groceries/delete/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('EMPLOYEE')")
    public String deleteFruit(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        fruitRepository.deleteById(id);
        redirectAttributes.addFlashAttribute("message", "Fruit deleted successfully!");
        return "redirect:/admin/groceries";
    }

    @GetMapping("/categories")
    @PreAuthorize("hasRole('ADMIN') or hasRole('EMPLOYEE')")
    public String categoriesAdmin(Model model) {
        model.addAttribute("categories", categoryRepository.findAll());
        return "admin/categories";
    }

    @GetMapping("/categories/new")
    @PreAuthorize("hasRole('ADMIN') or hasRole('EMPLOYEE')")
    public String showAddCategoryForm(Model model) {
        model.addAttribute("category", new Category());
        return "admin/category_form";
    }

    @PostMapping("/categories/save")
    @PreAuthorize("hasRole('ADMIN') or hasRole('EMPLOYEE')")
    public String saveCategory(@ModelAttribute("category") Category category, RedirectAttributes redirectAttributes) {
        categoryRepository.save(category);
        redirectAttributes.addFlashAttribute("message", "Category saved successfully!");
        return "redirect:/admin/categories";
    }

    @GetMapping("/categories/edit/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('EMPLOYEE')")
    public String showEditCategoryForm(@PathVariable Long id, Model model) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid category Id:" + id));
        model.addAttribute("category", category);
        return "admin/category_form";
    }

    @PostMapping("/categories/update/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('EMPLOYEE')")
    public String updateCategory(@PathVariable Long id, @ModelAttribute("category") Category categoryDetails, RedirectAttributes redirectAttributes) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid category Id:" + id));
        category.setName(categoryDetails.getName());
        categoryRepository.save(category);
        redirectAttributes.addFlashAttribute("message", "Category updated successfully!");
        return "redirect:/admin/categories";
    }

    @GetMapping("/categories/delete/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('EMPLOYEE')")
    public String deleteCategory(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        categoryRepository.deleteById(id);
        redirectAttributes.addFlashAttribute("message", "Category deleted successfully!");
        return "redirect:/admin/categories";
    }

    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public String usersAdmin(Model model) {
        model.addAttribute("users", userRepository.findAll());
        return "admin/users";
    }

    @GetMapping("/users/edit/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String showEditUserForm(@PathVariable Long id, Model model) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid user Id:" + id));
        model.addAttribute("user", user);
        model.addAttribute("allRoles", roleRepository.findAll());
        return "admin/user_form";
    }

    @PostMapping("/users/update/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String updateUser(@PathVariable Long id, @ModelAttribute("user") User userDetails, @RequestParam(required = false) Set<Integer> roles, RedirectAttributes redirectAttributes) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid user Id:" + id));

        user.setUsername(userDetails.getUsername());
        user.setEmail(userDetails.getEmail());

        if (userDetails.getPassword() != null && !userDetails.getPassword().isEmpty() && !userDetails.getPassword().equals(user.getPassword())) {
            user.setPassword(passwordEncoder.encode(userDetails.getPassword()));
        }

        if (roles != null) {
            user.setRoles(new HashSet<>(roleRepository.findAllById(roles)));
        }

        userRepository.save(user);
        redirectAttributes.addFlashAttribute("message", "User updated successfully!");
        return "redirect:/admin/users";
    }

    @GetMapping("/users/delete/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String deleteUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        userRepository.deleteById(id);
        redirectAttributes.addFlashAttribute("message", "User deleted successfully!");
        return "redirect:/admin/users";
    }

    @GetMapping("/mail")
    @PreAuthorize("hasRole('ADMIN') or hasRole('EMPLOYEE')")
    public String mailAdmin(Model model) {
        model.addAttribute("users", userRepository.findAll());
        return "admin/mail";
    }

    @PostMapping("/mail/send")
    @PreAuthorize("hasRole('ADMIN') or hasRole('EMPLOYEE')")
    public String sendEmail(@RequestParam("userId") Long userId,
                            @RequestParam("emailType") String emailType,
                            RedirectAttributes redirectAttributes) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid user Id:" + userId));

        String subject = "";
        String text = "";

        switch (emailType) {
            case "welcome":
                subject = "Welcome to Our Store!";
                text = "Hi " + user.getUsername() + ",\n\nThank you for joining us!";
                break;
            case "promotion":
                subject = "Special Promotion For You!";
                text = "Hi " + user.getUsername() + ",\n\nCheck out our latest promotions!";
                break;
            case "password_reset":
                subject = "Password Reset Request";
                text = "Hi " + user.getUsername() + ",\n\nPlease follow this link to reset your password.";
                break;
            default:
                redirectAttributes.addFlashAttribute("message", "Unknown email type.");
                return "redirect:/admin/mail";
        }

        try {
            emailService.sendSimpleMessage(user.getEmail(), subject, text);
            redirectAttributes.addFlashAttribute("message", "Email sent successfully to " + user.getUsername());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", "Error sending email: " + e.getMessage());
        }

        return "redirect:/admin/mail";
    }

    // Test endpoint for debugging
    @GetMapping("/test-profiles")
    @PreAuthorize("hasRole('ADMIN') or hasRole('EMPLOYEE')")
    @ResponseBody
    public String testProfiles() {
        try {
            List<CustomerProfile> profiles = customerProfileRepository.findAll();
            return "Profiles found: " + profiles.size() + "\n" + 
                   "Customers count: " + customerRepository.count() + "\n" +
                   "First profile: " + (profiles.isEmpty() ? "none" : profiles.get(0).getCustomerSegment());
        } catch (Exception e) {
            return "Error: " + e.getMessage() + "\nCause: " + (e.getCause() != null ? e.getCause().getMessage() : "none");
        }
    }

    // Customer Profile Management
    @GetMapping("/customer-profiles")
    @PreAuthorize("hasRole('ADMIN') or hasRole('EMPLOYEE')")
    public String customerProfiles(Model model) {
        try {
            List<CustomerProfile> profiles = customerProfileRepository.findAll();
            List<String> segments = customerProfileRepository.findAllCustomerSegments();
            model.addAttribute("profiles", profiles != null ? profiles : new ArrayList<>());
            model.addAttribute("segments", segments != null ? segments : new ArrayList<>());
            return "admin/customer_profiles";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("profiles", new ArrayList<>());
            model.addAttribute("segments", new ArrayList<>());
            model.addAttribute("error", "Error loading customer profiles: " + e.getMessage());
            return "admin/customer_profiles";
        }
    }

    @GetMapping("/customer-profiles/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('EMPLOYEE')")
    public String customerProfileDetail(@PathVariable Long id, Model model) {
        CustomerProfile profile = customerProfileRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid profile Id:" + id));
        model.addAttribute("profile", profile);
        model.addAttribute("customer", profile.getCustomer());
        model.addAttribute("orders", orderRepository.findByCustomerId(profile.getCustomer().getId()));
        return "admin/customer_profile_detail";
    }

    @PostMapping("/customer-profiles/update/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String updateCustomerProfile(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            CustomerProfile profile = customerProfileRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid profile Id:" + id));
            customerProfileService.updateProfileFromOrders(profile.getCustomer().getId());
            redirectAttributes.addFlashAttribute("message", "Customer profile updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", "Error updating profile: " + e.getMessage());
        }
        return "redirect:/admin/customer-profiles";
    }

    @PostMapping("/customer-profiles/update-all")
    @PreAuthorize("hasRole('ADMIN')")
    public String updateAllCustomerProfiles(RedirectAttributes redirectAttributes) {
        try {
            customerProfileService.updateAllProfiles();
            redirectAttributes.addFlashAttribute("message", "All customer profiles updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", "Error updating profiles: " + e.getMessage());
        }
        return "redirect:/admin/customer-profiles";
    }

    // AI Email Marketing Campaign Management
    @GetMapping("/email-campaigns")
    @PreAuthorize("hasRole('ADMIN') or hasRole('EMPLOYEE')")
    public String emailCampaigns(Model model) {
        try {
            List<EmailCampaign> campaigns = emailCampaignRepository.findAll();
            List<EmailCampaign> activeCampaigns = aiEmailMarketingService.getActiveCampaigns();
            List<EmailCampaign> completedCampaigns = aiEmailMarketingService.getCompletedCampaigns();
            
            // Filter out any null campaigns
            campaigns = campaigns.stream().filter(campaign -> campaign != null).collect(Collectors.toList());
            activeCampaigns = activeCampaigns.stream().filter(campaign -> campaign != null).collect(Collectors.toList());
            completedCampaigns = completedCampaigns.stream().filter(campaign -> campaign != null).collect(Collectors.toList());
            
            model.addAttribute("campaigns", campaigns);
            model.addAttribute("activeCampaigns", activeCampaigns);
            model.addAttribute("completedCampaigns", completedCampaigns);
            
            return "admin/email_campaigns";
        } catch (Exception e) {
            System.err.println("Error in emailCampaigns controller: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("campaigns", new ArrayList<>());
            model.addAttribute("activeCampaigns", new ArrayList<>());
            model.addAttribute("completedCampaigns", new ArrayList<>());
            return "admin/email_campaigns";
        }
    }

    @GetMapping("/email-campaigns/new")
    @PreAuthorize("hasRole('ADMIN') or hasRole('EMPLOYEE')")
    public String showCreateCampaignForm(Model model) {
        model.addAttribute("campaign", new EmailCampaign());
        model.addAttribute("segments", customerProfileRepository.findAllCustomerSegments());
        return "admin/email_campaign_form";
    }

    @PostMapping("/email-campaigns/create")
    @PreAuthorize("hasRole('ADMIN') or hasRole('EMPLOYEE')")
    public String createEmailCampaign(@RequestParam("campaignName") String campaignName,
                                      @RequestParam("campaignType") String campaignType,
                                      @RequestParam("targetSegment") String targetSegment,
                                      @RequestParam("aiGenerated") boolean aiGenerated,
                                      @RequestParam(value = "subjectTemplate", required = false) String subjectTemplate,
                                      @RequestParam(value = "contentTemplate", required = false) String contentTemplate,
                                      RedirectAttributes redirectAttributes) {
        try {
            EmailCampaign campaign;
            
            if (aiGenerated) {
                campaign = aiEmailMarketingService.createAIGeneratedCampaign(
                        campaignName, campaignType, targetSegment, "admin");
            } else {
                campaign = new EmailCampaign();
                campaign.setCampaignName(campaignName);
                campaign.setCampaignType(campaignType);
                campaign.setTargetSegment(targetSegment);
                campaign.setSubjectTemplate(subjectTemplate);
                campaign.setContentTemplate(contentTemplate);
                campaign.setCreatedBy("admin");
                campaign = emailCampaignRepository.save(campaign);
            }
            
            redirectAttributes.addFlashAttribute("message", 
                    "Email campaign '" + campaignName + "' created successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", "Error creating campaign: " + e.getMessage());
        }
        
        return "redirect:/admin/email-campaigns";
    }

    @GetMapping("/email-campaigns/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('EMPLOYEE')")
    public String emailCampaignDetail(@PathVariable Long id, Model model) {
        EmailCampaign campaign = emailCampaignRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid campaign Id:" + id));
        model.addAttribute("campaign", campaign);
        
        // Get target customers count
        List<CustomerProfile> targetProfiles;
        if ("All".equals(campaign.getTargetSegment())) {
            targetProfiles = customerProfileRepository.findByOptOutFalse();
        } else {
            targetProfiles = customerProfileRepository.findByCustomerSegment(campaign.getTargetSegment());
        }
        model.addAttribute("targetCustomersCount", targetProfiles.size());
        
        return "admin/email_campaign_detail";
    }

    @PostMapping("/email-campaigns/send/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String sendEmailCampaign(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            aiEmailMarketingService.sendCampaign(id);
            redirectAttributes.addFlashAttribute("message", "Email campaign sent successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", "Error sending campaign: " + e.getMessage());
        }
        return "redirect:/admin/email-campaigns/" + id;
    }

    @PostMapping("/email-campaigns/schedule/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String scheduleCampaign(@PathVariable Long id, 
                                   @RequestParam("scheduledDate") String scheduledDateStr,
                                   RedirectAttributes redirectAttributes) {
        try {
            LocalDateTime scheduledDate = LocalDateTime.parse(scheduledDateStr);
            aiEmailMarketingService.scheduleCampaign(id, scheduledDate);
            redirectAttributes.addFlashAttribute("message", "Campaign scheduled successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", "Error scheduling campaign: " + e.getMessage());
        }
        return "redirect:/admin/email-campaigns/" + id;
    }

    // Enhanced Mail System with AI personalization
    @GetMapping("/mail/ai")
    @PreAuthorize("hasRole('ADMIN') or hasRole('EMPLOYEE')")
    public String aiMailAdmin(Model model) {
        model.addAttribute("customers", customerRepository.findAll());
        model.addAttribute("profiles", customerProfileRepository.findAll());
        model.addAttribute("segments", customerProfileRepository.findAllCustomerSegments());
        return "admin/ai_mail";
    }

    @PostMapping("/mail/send-personalized")
    @PreAuthorize("hasRole('ADMIN') or hasRole('EMPLOYEE')")
    public String sendPersonalizedEmail(@RequestParam("customerId") Long customerId,
                                        @RequestParam("campaignType") String campaignType,
                                        RedirectAttributes redirectAttributes) {
        try {
            CustomerProfile profile = customerProfileService.getOrCreateProfile(customerId);
            Customer customer = profile.getCustomer();

            // Create a temporary campaign for this single email
            EmailCampaign tempCampaign = aiEmailMarketingService.createAIGeneratedCampaign(
                    "Personal Email to " + customer.getUsername(),
                    campaignType,
                    profile.getCustomerSegment(),
                    "admin"
            );

            String personalizedSubject = aiEmailMarketingService.personalizeEmailContent(
                    tempCampaign.getSubjectTemplate(), profile);
            String personalizedContent = aiEmailMarketingService.personalizeEmailContent(
                    tempCampaign.getContentTemplate(), profile);

            emailService.sendSimpleMessage(customer.getEmail(), personalizedSubject, personalizedContent);
            customerProfileService.recordEmailActivity(customerId, "sent");

            redirectAttributes.addFlashAttribute("message", 
                    "Personalized email sent successfully to " + customer.getUsername());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", "Error sending personalized email: " + e.getMessage());
        }

        return "redirect:/admin/mail/ai";
    }

    @PostMapping("/mail/send-to-segment")
    @PreAuthorize("hasRole('ADMIN')")
    public String sendEmailToSegment(@RequestParam("targetSegment") String targetSegment,
                                     @RequestParam("campaignType") String campaignType,
                                     RedirectAttributes redirectAttributes) {
        try {
            EmailCampaign campaign = aiEmailMarketingService.createAIGeneratedCampaign(
                    "Segment Email: " + targetSegment,
                    campaignType,
                    targetSegment,
                    "admin"
            );
            
            aiEmailMarketingService.sendCampaign(campaign.getId());
            
            redirectAttributes.addFlashAttribute("message", 
                    "Email campaign sent to " + targetSegment + " segment successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", "Error sending segment email: " + e.getMessage());
        }

        return "redirect:/admin/mail/ai";
    }
    
    @GetMapping("/email-test")
    @PreAuthorize("hasRole('ADMIN') or hasRole('EMPLOYEE')")
    public String emailTestPage(Model model) {
        return "admin/email_test";
    }
    
    @PostMapping("/api/send-test-email")
    @PreAuthorize("hasRole('ADMIN') or hasRole('EMPLOYEE')")
    @ResponseBody
    public ResponseEntity<String> sendTestEmail(@RequestBody Map<String, String> emailData) {
        try {
            String to = emailData.get("to");
            String subject = emailData.get("subject");
            String text = emailData.get("text");
            
            emailService.sendSimpleMessage(to, subject, text);
            return ResponseEntity.ok("Email sent successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to send email: " + e.getMessage());
        }
    }

    @GetMapping("/email-campaigns-test")
    @PreAuthorize("hasRole('ADMIN') or hasRole('EMPLOYEE')")
    @ResponseBody
    public String emailCampaignsTest() {
        try {
            List<EmailCampaign> campaigns = emailCampaignRepository.findAll();
            List<EmailCampaign> activeCampaigns = aiEmailMarketingService.getActiveCampaigns();
            List<EmailCampaign> completedCampaigns = aiEmailMarketingService.getCompletedCampaigns();
            
            return String.format("Campaigns: %d, Active: %d, Completed: %d", 
                campaigns.size(), activeCampaigns.size(), completedCampaigns.size());
        } catch (Exception e) {
            return "Error: " + e.getMessage() + " - " + e.getClass().getSimpleName();
        }
    }

    @GetMapping("/email-campaigns-simple")
    @PreAuthorize("hasRole('ADMIN') or hasRole('EMPLOYEE')")
    public String emailCampaignsSimple(Model model) {
        try {
            List<EmailCampaign> campaigns = emailCampaignRepository.findAll();
            model.addAttribute("campaigns", campaigns);
            model.addAttribute("campaignCount", campaigns.size());
            return "admin/email_campaigns_simple";
        } catch (Exception e) {
            System.err.println("Error in emailCampaignsSimple: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", e.getMessage());
            return "admin/email_campaigns_simple";
        }
    }

    // Analytics Dashboard
    @GetMapping("/analytics")
    @PreAuthorize("hasRole('ADMIN') or hasRole('EMPLOYEE')")
    public String analyticsPage(Model model) {
        try {
            // Get dashboard data
            Map<String, Object> dashboardData = analyticsService.getDashboardData();
            model.addAllAttributes(dashboardData);
            
            return "admin/analytics";
        } catch (Exception e) {
            model.addAttribute("error", "Failed to load analytics data: " + e.getMessage());
            return "admin/analytics";
        }
    }

    @GetMapping("/gdpr-compliance")
    @PreAuthorize("hasRole('ADMIN') or hasRole('EMPLOYEE')")
    public String gdprCompliancePage(Model model) {
        model.addAttribute("gdprCompliant", true);
        model.addAttribute("privacyPolicyVersion", "1.0");
        model.addAttribute("dataRetentionPeriod", 24);
        return "admin/gdpr_compliance";
    }

    // Analytics API Endpoints
    @GetMapping("/api/analytics/dashboard")
    @PreAuthorize("hasRole('ADMIN') or hasRole('EMPLOYEE')")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getDashboardData() {
        try {
            return ResponseEntity.ok(analyticsService.getDashboardData());
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/api/analytics/sales/monthly")
    @PreAuthorize("hasRole('ADMIN') or hasRole('EMPLOYEE')")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> getMonthlySales() {
        try {
            return ResponseEntity.ok(analyticsService.getMonthlySalesReport());
        } catch (Exception e) {
            return ResponseEntity.status(500).body(List.of());
        }
    }

    @GetMapping("/api/analytics/products/top")
    @PreAuthorize("hasRole('ADMIN') or hasRole('EMPLOYEE')")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> getTopProducts() {
        try {
            return ResponseEntity.ok(analyticsService.getTopSellingProducts());
        } catch (Exception e) {
            return ResponseEntity.status(500).body(List.of());
        }
    }

    @GetMapping("/api/analytics/categories")
    @PreAuthorize("hasRole('ADMIN') or hasRole('EMPLOYEE')")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> getCategoryPerformance() {
        try {
            return ResponseEntity.ok(analyticsService.getCategoryPerformance());
        } catch (Exception e) {
            return ResponseEntity.status(500).body(List.of());
        }
    }

    @GetMapping("/api/analytics/customers/segments")
    @PreAuthorize("hasRole('ADMIN') or hasRole('EMPLOYEE')")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getCustomerSegments() {
        try {
            return ResponseEntity.ok(analyticsService.getCustomerSegments());
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    // Customer Management Section
    @GetMapping("/customers")
    @PreAuthorize("hasRole('ADMIN') or hasRole('EMPLOYEE')")
    public String customersAdmin(Model model) {
        try {
            List<Customer> customers = customerRepository.findAll();
            model.addAttribute("customers", customers != null ? customers : new ArrayList<>());
            return "admin/customers";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("customers", new ArrayList<>());
            model.addAttribute("error", "Error loading customers: " + e.getMessage());
            return "admin/customers";
        }
    }

    @GetMapping("/customers/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('EMPLOYEE')")
    public String customerDetail(@PathVariable Long id, Model model) {
        try {
            System.out.println("DEBUG: Accessing customer detail for ID: " + id);
            
            Customer customer = customerRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid customer Id:" + id));
            System.out.println("DEBUG: Found customer: " + customer.getEmail());
            
            // Get customer profile if exists
            CustomerProfile profile = null;
            try {
                profile = customerProfileRepository.findByCustomerId(id).orElse(null);
                System.out.println("DEBUG: Profile found: " + (profile != null));
            } catch (Exception e) {
                System.out.println("DEBUG: Error loading profile: " + e.getMessage());
            }
            
            // Get customer orders
            List<Order> orders = new ArrayList<>();
            try {
                orders = orderRepository.findByCustomerId(id);
                System.out.println("DEBUG: Found " + orders.size() + " orders");
            } catch (Exception e) {
                System.out.println("DEBUG: Error loading orders: " + e.getMessage());
            }
            
            model.addAttribute("customer", customer);
            model.addAttribute("profile", profile);
            model.addAttribute("orders", orders);
            model.addAttribute("orderCount", orders.size());
            
            double totalSpent = 0.0;
            try {
                totalSpent = orders.stream()
                        .filter(Order::isFulfilled)
                        .mapToDouble(order -> order.getTotalAmount().doubleValue())
                        .sum();
            } catch (Exception e) {
                System.out.println("DEBUG: Error calculating total spent: " + e.getMessage());
            }
            model.addAttribute("totalSpent", totalSpent);
            
            System.out.println("DEBUG: Returning customer_detail template");
            return "admin/customer_detail_simple";
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("DEBUG: Exception in customerDetail: " + e.getMessage());
            model.addAttribute("error", "Error loading customer details: " + e.getMessage());
            return "error";
        }
    }

    @PostMapping("/customers/{id}/create-profile")
    @PreAuthorize("hasRole('ADMIN')")
    public String createCustomerProfile(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            customerProfileService.getOrCreateProfile(id);
            redirectAttributes.addFlashAttribute("message", "Customer profile created successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", "Error creating profile: " + e.getMessage());
        }
        return "redirect:/admin/customers/" + id;
    }

    @PostMapping("/customers/{id}/delete")
    @PreAuthorize("hasRole('ADMIN')")
    public String deleteCustomer(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            // First delete related data
            CustomerProfile profile = customerProfileRepository.findByCustomerId(id).orElse(null);
            if (profile != null) {
                customerProfileRepository.delete(profile);
            }
            
            // Delete orders (if any - though this should be handled carefully in production)
            List<Order> orders = orderRepository.findByCustomerId(id);
            if (!orders.isEmpty()) {
                redirectAttributes.addFlashAttribute("message", 
                    "Cannot delete customer with existing orders. Please handle orders first.");
                return "redirect:/admin/customers/" + id;
            }
            
            customerRepository.deleteById(id);
            redirectAttributes.addFlashAttribute("message", "Customer deleted successfully!");
            return "redirect:/admin/customers";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", "Error deleting customer: " + e.getMessage());
            return "redirect:/admin/customers/" + id;
        }
    }

    // Test endpoint to debug customer details issue
    @GetMapping("/customers/{id}/test")
    @PreAuthorize("hasRole('ADMIN') or hasRole('EMPLOYEE')")
    @ResponseBody
    public ResponseEntity<String> testCustomerDetail(@PathVariable Long id) {
        try {
            System.out.println("DEBUG TEST: Accessing customer test for ID: " + id);
            
            Customer customer = customerRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid customer Id:" + id));
            System.out.println("DEBUG TEST: Found customer: " + customer.getEmail());
            
            return ResponseEntity.ok("Customer found: " + customer.getEmail() + " (ID: " + customer.getId() + ")");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("DEBUG TEST: Exception in testCustomerDetail: " + e.getMessage());
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    // ==============================================
    // COUPON MANAGEMENT SECTION
    // ==============================================

    // Coupon listing page
    @GetMapping("/coupons")
    @PreAuthorize("hasRole('ADMIN') or hasRole('EMPLOYEE')")
    public String couponsAdmin(Model model) {
        try {
            List<Coupon> allCoupons = couponRepository.findAll();
            List<Coupon> activeCoupons = allCoupons.stream()
                .filter(c -> c.getIsActive() && c.getExpirationDate().isAfter(LocalDateTime.now()))
                .collect(Collectors.toList());
            List<Coupon> expiredCoupons = allCoupons.stream()
                .filter(c -> c.getExpirationDate().isBefore(LocalDateTime.now()))
                .collect(Collectors.toList());
            
            model.addAttribute("allCoupons", allCoupons);
            model.addAttribute("activeCoupons", activeCoupons);
            model.addAttribute("expiredCoupons", expiredCoupons);
            model.addAttribute("aiGeneratedCount", 
                couponRepository.findByGeneratedBy(Coupon.CouponSource.AI_NEGOTIATION).size());
            
            return "admin/coupons";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("allCoupons", new ArrayList<>());
            model.addAttribute("activeCoupons", new ArrayList<>());
            model.addAttribute("expiredCoupons", new ArrayList<>());
            model.addAttribute("aiGeneratedCount", 0);
            model.addAttribute("error", "Error loading coupons: " + e.getMessage());
            return "admin/coupons";
        }
    }

    // Coupon creation form
    @GetMapping("/coupons/new")
    @PreAuthorize("hasRole('ADMIN')")
    public String showCreateCouponForm(Model model) {
        model.addAttribute("coupon", new Coupon());
        model.addAttribute("couponTypes", Coupon.CouponType.values());
        model.addAttribute("couponSources", Coupon.CouponSource.values());
        model.addAttribute("customers", customerRepository.findAll());
        return "admin/coupon_form";
    }

    // Coupon creation
    @PostMapping("/coupons/create")
    @PreAuthorize("hasRole('ADMIN')")
    public String createCoupon(@ModelAttribute("coupon") Coupon coupon, 
                              RedirectAttributes redirectAttributes) {
        try {
            coupon.setGeneratedBy(Coupon.CouponSource.MANUAL_ADMIN);
            coupon.setCreatedAt(LocalDateTime.now());
            
            // Generate unique coupon code if not provided
            if (coupon.getCouponCode() == null || coupon.getCouponCode().trim().isEmpty()) {
                coupon.setCouponCode(generateAdminCouponCode());
            }
            
            couponRepository.save(coupon);
            redirectAttributes.addFlashAttribute("message", 
                "Coupon created successfully: " + coupon.getCouponCode());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", 
                "Error creating coupon: " + e.getMessage());
        }
        return "redirect:/admin/coupons";
    }

    // Coupon detail view
    @GetMapping("/coupons/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('EMPLOYEE')")
    public String couponDetail(@PathVariable Long id, Model model) {
        try {
            Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid coupon Id:" + id));
            
            // Get usage statistics
            List<Order> ordersWithCoupon = orderRepository.findByCouponCode(coupon.getCouponCode());
            BigDecimal totalSavings = ordersWithCoupon.stream()
                .map(Order::getDiscountAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            model.addAttribute("coupon", coupon);
            model.addAttribute("ordersWithCoupon", ordersWithCoupon);
            model.addAttribute("totalSavings", totalSavings);
            model.addAttribute("usageCount", ordersWithCoupon.size());
            
            return "admin/coupon_detail";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Error loading coupon details: " + e.getMessage());
            return "error";
        }
    }

    // Coupon edit form
    @GetMapping("/coupons/edit/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String showEditCouponForm(@PathVariable Long id, Model model) {
        try {
            Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid coupon Id:" + id));
            
            model.addAttribute("coupon", coupon);
            model.addAttribute("couponTypes", Coupon.CouponType.values());
            model.addAttribute("customers", customerRepository.findAll());
            
            return "admin/coupon_form";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Error loading coupon: " + e.getMessage());
            return "error";
        }
    }

    // Coupon update
    @PostMapping("/coupons/update/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String updateCoupon(@PathVariable Long id, 
                              @ModelAttribute("coupon") Coupon couponDetails,
                              RedirectAttributes redirectAttributes) {
        try {
            Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid coupon Id:" + id));
            
            // Update editable fields
            coupon.setCouponType(couponDetails.getCouponType());
            coupon.setDiscountValue(couponDetails.getDiscountValue());
            coupon.setMinimumOrderValue(couponDetails.getMinimumOrderValue());
            coupon.setExpirationDate(couponDetails.getExpirationDate());
            coupon.setMaxUses(couponDetails.getMaxUses());
            coupon.setIsActive(couponDetails.getIsActive());
            coupon.setRestrictions(couponDetails.getRestrictions());
            
            couponRepository.save(coupon);
            redirectAttributes.addFlashAttribute("message", "Coupon updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error updating coupon: " + e.getMessage());
        }
        return "redirect:/admin/coupons/" + id;
    }

    // Deactivate coupon
    @PostMapping("/coupons/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public String deactivateCoupon(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid coupon Id:" + id));
            
            coupon.setIsActive(false);
            couponRepository.save(coupon);
            
            redirectAttributes.addFlashAttribute("message", 
                "Coupon " + coupon.getCouponCode() + " has been deactivated!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error deactivating coupon: " + e.getMessage());
        }
        return "redirect:/admin/coupons/" + id;
    }

    // Coupon analytics API
    @GetMapping("/api/coupons/analytics")
    @PreAuthorize("hasRole('ADMIN') or hasRole('EMPLOYEE')")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getCouponAnalytics() {
        try {
            Map<String, Object> analytics = new HashMap<>();
            
            // Basic counts
            analytics.put("totalCoupons", couponRepository.count());
            analytics.put("activeCoupons", 
                couponRepository.findAll().stream()
                    .filter(c -> c.getIsActive() && c.getExpirationDate().isAfter(LocalDateTime.now()))
                    .count());
            analytics.put("aiGeneratedCoupons", 
                couponRepository.findByGeneratedBy(Coupon.CouponSource.AI_NEGOTIATION).size());
            
            // Usage statistics
            List<Coupon> usedCoupons = couponRepository.findAll().stream()
                .filter(c -> c.getCurrentUses() > 0)
                .collect(Collectors.toList());
            
            analytics.put("usedCoupons", usedCoupons.size());
            analytics.put("totalSavings", calculateTotalSavings());
            
            return ResponseEntity.ok(analytics);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    private String generateAdminCouponCode() {
        return "ADMIN" + System.currentTimeMillis() % 100000;
    }

    private BigDecimal calculateTotalSavings() {
        return orderRepository.findAll().stream()
            .filter(Order::hasDiscount)
            .map(Order::getDiscountAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // Helper methods for Order conversion
    private List<OrderDTO> convertToOrderDTOs(List<Order> orders) {
        return orders.stream()
                .map(this::convertToOrderDTO)
                .collect(java.util.stream.Collectors.toList());
    }

    private OrderDTO convertToOrderDTO(Order order) {
        OrderDTO dto = new OrderDTO();
        dto.setId(order.getId());
        dto.setOrderDate(order.getOrderDate());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setOriginalAmount(order.getOriginalAmount());
        dto.setDiscountAmount(order.getDiscountAmount());
        dto.setCouponCode(order.getCouponCode());
        
        // Handle both customer and user relationships for backward compatibility
        if (order.getCustomer() != null) {
            dto.setUsername(order.getCustomer().getFirstName() + " " + order.getCustomer().getLastName());
            dto.setCustomerEmail(order.getCustomer().getEmail());
        } else if (order.getUser() != null) {
            dto.setUsername(order.getUser().getUsername());
            dto.setCustomerEmail(order.getUser().getEmail());
        } else {
            dto.setUsername("Unknown Customer");
            dto.setCustomerEmail("unknown@example.com");
        }
        
        dto.setFulfilled(order.isFulfilled());
        dto.setFulfilledDate(order.getFulfilledDate());
        dto.setOrderNotes(order.getOrderNotes());
        
        // Convert order items
        List<OrderItemDTO> orderItemDTOs = order.getOrderItems().stream()
                .map(this::convertToOrderItemDTO)
                .collect(java.util.stream.Collectors.toList());
        dto.setOrderItems(orderItemDTOs);
        dto.setTotalItems(dto.calculateTotalItems());
        
        return dto;
    }

    private OrderItemDTO convertToOrderItemDTO(OrderItem orderItem) {
        OrderItemDTO dto = new OrderItemDTO();
        dto.setId(orderItem.getId());
        dto.setQuantity(orderItem.getQuantity());
        dto.setPrice(orderItem.getPrice());
        dto.setFruitId(orderItem.getFruit().getId());
        dto.setFruitName(orderItem.getFruit().getName());
        dto.setFruitDescription(orderItem.getFruit().getDescription());
        // Note: Image functionality can be added later when Fruit entity includes imageUrl field
        dto.setFruitImage(null);
        
        // Get category information
        if (orderItem.getFruit().getCategory() != null) {
            dto.setCategoryName(orderItem.getFruit().getCategory().getName());
        }
        
        // Calculate subtotal
        dto.setSubtotal(dto.calculateSubtotal());
        
        return dto;
    }
    
    /**
     * Uploads a product image and returns the URL
     */
    private String uploadProductImage(MultipartFile file) throws IOException {
        // Validate file
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }
        
        // Check file size (5MB limit)
        long maxSize = 5 * 1024 * 1024; // 5MB
        if (file.getSize() > maxSize) {
            throw new IllegalArgumentException("File size exceeds 5MB limit");
        }
        
        // Validate file type
        String contentType = file.getContentType();
        if (contentType == null || (!contentType.startsWith("image/"))) {
            throw new IllegalArgumentException("Invalid file type. Only images are allowed.");
        }
        
        // Generate unique filename
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String filename = UUID.randomUUID().toString() + extension;
        
        // Ensure upload directory exists
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        
        // Save file
        Path filePath = uploadPath.resolve(filename);
        Files.copy(file.getInputStream(), filePath);
        
        // Return URL path for accessing the image
        return "/api/images/view/" + filename;
    }
    
    /**
     * Extracts filename from image URL
     */
    private String extractFilenameFromUrl(String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            return null;
        }
        
        // Extract filename from URL like "/api/images/view/filename.jpg"
        int lastSlashIndex = imageUrl.lastIndexOf("/");
        if (lastSlashIndex != -1 && lastSlashIndex < imageUrl.length() - 1) {
            return imageUrl.substring(lastSlashIndex + 1);
        }
        
        return null;
    }
}