package com.example.project3;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Controller
@RequestMapping("/owner")
public class OwnerController {

    @Autowired
    private StaffRepository staffRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private FruitRepository fruitRepository;

    @Autowired
    private CustomerProfileRepository customerProfileRepository;

    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('OWNER')")
    public String ownerDashboard(Model model) {
        // Owner dashboard with system-wide metrics
        model.addAttribute("totalStaff", staffRepository.count());
        model.addAttribute("totalCustomers", customerRepository.count());
        model.addAttribute("totalOrders", orderRepository.count());
        model.addAttribute("totalProducts", fruitRepository.count());
        
        model.addAttribute("activeStaff", staffRepository.findByIsActiveTrue().size());
        model.addAttribute("activeCustomers", customerRepository.findByIsActiveTrue().size());
        model.addAttribute("recentOrders", orderRepository.findAll().stream()
                .sorted((o1, o2) -> o2.getOrderDate().compareTo(o1.getOrderDate()))
                .limit(5)
                .toList());
        
        return "owner/dashboard";
    }

    // Staff Management (Owner-only functionality)
    @GetMapping("/staff")
    @PreAuthorize("hasRole('OWNER')")
    public String manageStaff(Model model) {
        model.addAttribute("allStaff", staffRepository.findAll());
        model.addAttribute("owners", staffRepository.findOwners());
        model.addAttribute("admins", staffRepository.findAdmins());
        model.addAttribute("employees", staffRepository.findEmployees());
        return "owner/staff";
    }

    @GetMapping("/staff/new")
    @PreAuthorize("hasRole('OWNER')")
    public String showAddStaffForm(Model model) {
        model.addAttribute("staff", new StaffMember());
        model.addAttribute("allRoles", roleRepository.findAll());
        return "owner/staff_form";
    }

    @PostMapping("/staff/save")
    @PreAuthorize("hasRole('OWNER')")
    public String saveStaff(@ModelAttribute("staff") StaffMember staff,
                           @RequestParam(required = false) Set<Integer> roles,
                           RedirectAttributes redirectAttributes) {
        try {
            // Encrypt password if provided
            if (staff.getPassword() != null && !staff.getPassword().isEmpty()) {
                staff.setPassword(passwordEncoder.encode(staff.getPassword()));
            }

            // Set roles
            if (roles != null && !roles.isEmpty()) {
                Set<Role> staffRoles = new HashSet<>();
                for (Integer roleId : roles) {
                    Role role = roleRepository.findById(roleId).orElse(null);
                    if (role != null) {
                        staffRoles.add(role);
                    }
                }
                staff.setRoles(staffRoles);
            } else {
                // Default to employee role
                Role employeeRole = roleRepository.findByName(RoleName.ROLE_EMPLOYEE)
                        .orElseThrow(() -> new RuntimeException("Employee role not found"));
                Set<Role> defaultRoles = new HashSet<>();
                defaultRoles.add(employeeRole);
                staff.setRoles(defaultRoles);
            }

            // Set hire date if new staff
            if (staff.getId() == null) {
                staff.setHireDate(LocalDateTime.now());
            }

            staffRepository.save(staff);
            redirectAttributes.addFlashAttribute("message", "Staff member saved successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", "Error saving staff member: " + e.getMessage());
        }
        return "redirect:/owner/staff";
    }

    @GetMapping("/staff/edit/{id}")
    @PreAuthorize("hasRole('OWNER')")
    public String showEditStaffForm(@PathVariable Long id, Model model) {
        StaffMember staff = staffRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid staff Id:" + id));
        model.addAttribute("staff", staff);
        model.addAttribute("allRoles", roleRepository.findAll());
        return "owner/staff_form";
    }

    @PostMapping("/staff/update/{id}")
    @PreAuthorize("hasRole('OWNER')")
    public String updateStaff(@PathVariable Long id, @ModelAttribute("staff") StaffMember staffDetails,
                             @RequestParam(required = false) Set<Integer> roles,
                             RedirectAttributes redirectAttributes) {
        try {
            StaffMember staff = staffRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid staff Id:" + id));
            
            staff.setUsername(staffDetails.getUsername());
            staff.setEmail(staffDetails.getEmail());
            staff.setFirstName(staffDetails.getFirstName());
            staff.setLastName(staffDetails.getLastName());
            staff.setEmployeeId(staffDetails.getEmployeeId());
            staff.setDepartment(staffDetails.getDepartment());
            staff.setIsActive(staffDetails.getIsActive());

            // Update password only if provided
            if (staffDetails.getPassword() != null && !staffDetails.getPassword().isEmpty()) {
                staff.setPassword(passwordEncoder.encode(staffDetails.getPassword()));
            }

            // Update roles
            if (roles != null && !roles.isEmpty()) {
                Set<Role> staffRoles = new HashSet<>();
                for (Integer roleId : roles) {
                    Role role = roleRepository.findById(roleId).orElse(null);
                    if (role != null) {
                        staffRoles.add(role);
                    }
                }
                staff.setRoles(staffRoles);
            }

            staffRepository.save(staff);
            redirectAttributes.addFlashAttribute("message", "Staff member updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", "Error updating staff member: " + e.getMessage());
        }
        return "redirect:/owner/staff";
    }

    @PostMapping("/staff/deactivate/{id}")
    @PreAuthorize("hasRole('OWNER')")
    public String deactivateStaff(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            StaffMember staff = staffRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid staff Id:" + id));
            staff.setIsActive(false);
            staffRepository.save(staff);
            redirectAttributes.addFlashAttribute("message", "Staff member deactivated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", "Error deactivating staff member: " + e.getMessage());
        }
        return "redirect:/owner/staff";
    }

    // System Analytics (Owner-only)
    @GetMapping("/analytics")
    @PreAuthorize("hasRole('OWNER')")
    public String systemAnalytics(Model model) {
        model.addAttribute("totalRevenue", orderRepository.findAll().stream()
                .map(Order::getTotalAmount)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add));
        model.addAttribute("customerGrowth", customerRepository.count());
        model.addAttribute("orderTrends", orderRepository.count());
        model.addAttribute("systemHealth", "Operational"); // Add actual health checks
        
        return "owner/analytics";
    }

    // Customer Management Overview (Owner-only)
    @GetMapping("/customers")
    @PreAuthorize("hasRole('OWNER')")
    public String customerOverview(Model model) {
        model.addAttribute("customers", customerRepository.findAll());
        model.addAttribute("newCustomers", customerRepository.findNewCustomers(LocalDateTime.now().minusDays(30)));
        model.addAttribute("inactiveCustomers", customerRepository.findInactiveCustomers(LocalDateTime.now().minusDays(90)));
        
        return "owner/customers";
    }

    // System Settings (Owner-only)
    @GetMapping("/settings")
    @PreAuthorize("hasRole('OWNER')")
    public String systemSettings(Model model) {
        // Add system configuration settings
        return "owner/settings";
    }
} 