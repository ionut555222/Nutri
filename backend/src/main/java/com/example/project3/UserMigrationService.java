package com.example.project3;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Service
@Transactional
public class UserMigrationService {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private StaffRepository staffRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CustomerProfileService customerProfileService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public void run(String... args) throws Exception {
        // Initialize roles first
        initializeRoles();
        
        // Create default owner account if none exists
        createDefaultOwnerIfNeeded();
        
        // Migrate existing users (uncomment when ready to migrate)
        // migrateExistingUsers();
    }

    private void initializeRoles() {
        // Create roles if they don't exist
        if (roleRepository.findByName(RoleName.ROLE_OWNER).isEmpty()) {
            Role ownerRole = new Role(RoleName.ROLE_OWNER);
            roleRepository.save(ownerRole);
            System.out.println("Created ROLE_OWNER");
        }

        if (roleRepository.findByName(RoleName.ROLE_ADMIN).isEmpty()) {
            Role adminRole = new Role(RoleName.ROLE_ADMIN);
            roleRepository.save(adminRole);
            System.out.println("Created ROLE_ADMIN");
        }

        if (roleRepository.findByName(RoleName.ROLE_EMPLOYEE).isEmpty()) {
            Role employeeRole = new Role(RoleName.ROLE_EMPLOYEE);
            roleRepository.save(employeeRole);
            System.out.println("Created ROLE_EMPLOYEE");
        }

        if (roleRepository.findByName(RoleName.ROLE_CUSTOMER).isEmpty()) {
            Role customerRole = new Role(RoleName.ROLE_CUSTOMER);
            roleRepository.save(customerRole);
            System.out.println("Created ROLE_CUSTOMER");
        }
    }

    private void createDefaultOwnerIfNeeded() {
        // Check if any owner exists
        if (staffRepository.findOwners().isEmpty()) {
            // Create default owner account
            StaffMember owner = new StaffMember(
                    "owner",
                    "owner@fruitstore.com",
                    passwordEncoder.encode("owner123"), // Change this password!
                    "System",
                    "Owner"
            );
            
            owner.setEmployeeId("OWNER001");
            owner.setDepartment("Management");
            owner.setHireDate(LocalDateTime.now());

            Role ownerRole = roleRepository.findByName(RoleName.ROLE_OWNER)
                    .orElseThrow(() -> new RuntimeException("Owner role not found"));
            
            Set<Role> roles = new HashSet<>();
            roles.add(ownerRole);
            owner.setRoles(roles);

            staffRepository.save(owner);
            System.out.println("Created default owner account: owner / owner123");
            System.out.println("IMPORTANT: Change the default password immediately!");
        }
    }

    /**
     * Migrate existing User entities to either Customer or StaffMember entities
     * This method should be run once during the migration process
     */
    public void migrateExistingUsers() {
        System.out.println("Starting user migration...");
        
        for (User user : userRepository.findAll()) {
            try {
                if (isStaffUser(user)) {
                    migrateToStaff(user);
                } else {
                    migrateToCustomer(user);
                }
            } catch (Exception e) {
                System.err.println("Error migrating user " + user.getUsername() + ": " + e.getMessage());
            }
        }
        
        System.out.println("User migration completed.");
    }

    private boolean isStaffUser(User user) {
        // Check if user has admin or employee roles
        return user.getRoles().stream()
                .anyMatch(role -> role.getName() == RoleName.ROLE_ADMIN || 
                                role.getName() == RoleName.ROLE_EMPLOYEE);
    }

    private void migrateToStaff(User user) {
        // Check if staff member already exists
        if (staffRepository.findByUsername(user.getUsername()).isPresent()) {
            System.out.println("Staff member already exists: " + user.getUsername());
            return;
        }

        StaffMember staff = new StaffMember(
                user.getUsername(),
                user.getEmail(),
                user.getPassword(), // Password is already encoded
                user.getUsername(), // Use username as first name temporarily
                "Staff" // Default last name
        );

        staff.setEmployeeId("EMP" + user.getId());
        staff.setDepartment("General");
        staff.setHireDate(LocalDateTime.now());

        // Migrate roles
        Set<Role> newRoles = new HashSet<>();
        for (Role oldRole : user.getRoles()) {
            if (oldRole.getName() == RoleName.ROLE_ADMIN) {
                Role adminRole = roleRepository.findByName(RoleName.ROLE_ADMIN)
                        .orElseThrow(() -> new RuntimeException("Admin role not found"));
                newRoles.add(adminRole);
            } else if (oldRole.getName() == RoleName.ROLE_EMPLOYEE) {
                Role employeeRole = roleRepository.findByName(RoleName.ROLE_EMPLOYEE)
                        .orElseThrow(() -> new RuntimeException("Employee role not found"));
                newRoles.add(employeeRole);
            }
        }

        if (newRoles.isEmpty()) {
            // Default to employee
            Role employeeRole = roleRepository.findByName(RoleName.ROLE_EMPLOYEE)
                    .orElseThrow(() -> new RuntimeException("Employee role not found"));
            newRoles.add(employeeRole);
        }

        staff.setRoles(newRoles);
        staffRepository.save(staff);
        
        System.out.println("Migrated to staff: " + user.getUsername());
    }

    private void migrateToCustomer(User user) {
        // Check if customer already exists
        if (customerRepository.findByUsername(user.getUsername()).isPresent()) {
            System.out.println("Customer already exists: " + user.getUsername());
            return;
        }

        Customer customer = new Customer(
                user.getUsername(),
                user.getEmail(),
                user.getPassword() // Password is already encoded
        );

        customer.setFirstName(user.getUsername()); // Use username as first name temporarily
        customer.setLastName("Customer"); // Default last name

        Customer savedCustomer = customerRepository.save(customer);
        
        // Create customer profile
        customerProfileService.createInitialCustomerProfile(savedCustomer);
        
        System.out.println("Migrated to customer: " + user.getUsername());
    }

    /**
     * Create sample staff members for testing
     */
    public void createSampleStaff() {
        // Create sample admin
        if (staffRepository.findByUsername("admin").isEmpty()) {
            StaffMember admin = new StaffMember(
                    "admin",
                    "admin@fruitstore.com",
                    passwordEncoder.encode("admin123"),
                    "Store",
                    "Administrator"
            );
            
            admin.setEmployeeId("ADM001");
            admin.setDepartment("Administration");
            admin.setHireDate(LocalDateTime.now());

            Role adminRole = roleRepository.findByName(RoleName.ROLE_ADMIN)
                    .orElseThrow(() -> new RuntimeException("Admin role not found"));
            
            Set<Role> roles = new HashSet<>();
            roles.add(adminRole);
            admin.setRoles(roles);

            staffRepository.save(admin);
            System.out.println("Created sample admin: admin / admin123");
        }

        // Create sample employee
        if (staffRepository.findByUsername("employee").isEmpty()) {
            StaffMember employee = new StaffMember(
                    "employee",
                    "employee@fruitstore.com",
                    passwordEncoder.encode("emp123"),
                    "Store",
                    "Employee"
            );
            
            employee.setEmployeeId("EMP001");
            employee.setDepartment("Operations");
            employee.setHireDate(LocalDateTime.now());

            Role empRole = roleRepository.findByName(RoleName.ROLE_EMPLOYEE)
                    .orElseThrow(() -> new RuntimeException("Employee role not found"));
            
            Set<Role> roles = new HashSet<>();
            roles.add(empRole);
            employee.setRoles(roles);

            staffRepository.save(employee);
            System.out.println("Created sample employee: employee / emp123");
        }
    }

    /**
     * Create sample customers for testing
     */
    public void createSampleCustomers() {
        if (customerRepository.findByUsername("customer1").isEmpty()) {
            Customer customer = new Customer(
                    "customer1",
                    "customer1@example.com",
                    passwordEncoder.encode("cust123")
            );
            
            customer.setFirstName("John");
            customer.setLastName("Doe");
            customer.setPhoneNumber("+1234567890");
            customer.setEmailVerified(true);

            Customer savedCustomer = customerRepository.save(customer);
            customerProfileService.createInitialCustomerProfile(savedCustomer);
            
            System.out.println("Created sample customer: customer1 / cust123");
        }
    }
} 