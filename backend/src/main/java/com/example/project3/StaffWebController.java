package com.example.project3;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class StaffWebController {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private OrderRepository orderRepository;

    @GetMapping("/")
    public String home() {
        return "redirect:/admin/login";
    }

    @GetMapping("/admin/login")
    public String adminLogin() {
        return "login";
    }

    @GetMapping("/owner/login")
    public String ownerLogin() {
        return "staff_login";
    }

    @GetMapping("/employee/login")
    public String employeeLogin() {
        return "staff_login";
    }

    // Customer information page (no access)
    @GetMapping("/customer-info")
    public String customerInfo(Model model) {
        model.addAttribute("title", "Customer Access Information");
        model.addAttribute("message", "This is a staff management interface. Customers should use the iOS mobile app to:");
        model.addAttribute("appFeatures", new String[]{
            "Browse and purchase fruits",
            "View order history",
            "Chat with support",
            "Manage account settings",
            "Track deliveries"
        });
        model.addAttribute("appStoreLink", "#"); // Replace with actual App Store link
        model.addAttribute("contactEmail", "support@fruitstore.com");
        return "customer_info";
    }

    // Access denied page
    @GetMapping("/access-denied")
    public String accessDenied(Model model) {
        model.addAttribute("title", "Access Denied");
        model.addAttribute("message", "You don't have permission to access this resource.");
        model.addAttribute("suggestion", "If you're a customer, please use our iOS app. If you're staff, please contact your administrator.");
        return "access_denied";
    }

    // 403 error page
    @GetMapping("/403")
    public String forbidden(Model model) {
        model.addAttribute("title", "Forbidden");
        model.addAttribute("message", "Access to this resource is forbidden.");
        return "403";
    }

    // Staff help page
    @GetMapping("/help")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'EMPLOYEE')")
    public String staffHelp(Model model) {
        model.addAttribute("title", "Staff Help & Documentation");
        model.addAttribute("sections", new String[]{
            "Getting Started",
            "Managing Orders",
            "Customer Support",
            "Inventory Management",
            "Reporting Tools"
        });
        return "staff_help";
    }

    // System status page
    @GetMapping("/status")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public String systemStatus(Model model) {
        model.addAttribute("title", "System Status");
        model.addAttribute("systemHealth", "Operational");
        model.addAttribute("apiStatus", "Online");
        model.addAttribute("databaseStatus", "Connected");
        model.addAttribute("lastUpdate", java.time.LocalDateTime.now());
        return "system_status";
    }
} 