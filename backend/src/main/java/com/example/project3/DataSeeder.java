package com.example.project3;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import java.util.Arrays;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Component
public class DataSeeder implements CommandLineRunner {

    @Autowired
    private final FruitRepository fruitRepository;
    @Autowired
    private final CategoryRepository categoryRepository;
    @Autowired
    private final RoleRepository roleRepository;
    @Autowired
    private final StaffRepository staffRepository;
    @Autowired
    private final CustomerRepository customerRepository;
    @Autowired
    private final CustomerProfileService customerProfileService;
    @Autowired
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(FruitRepository fruitRepository, CategoryRepository categoryRepository, RoleRepository roleRepository, StaffRepository staffRepository, CustomerRepository customerRepository, CustomerProfileService customerProfileService, PasswordEncoder passwordEncoder) {
        this.fruitRepository = fruitRepository;
        this.categoryRepository = categoryRepository;
        this.roleRepository = roleRepository;
        this.staffRepository = staffRepository;
        this.customerRepository = customerRepository;
        this.customerProfileService = customerProfileService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("🌱 DataSeeder: Starting data seeding...");
        seedRoles();
        seedDefaultStaff();
        seedCategoriesAndFruits();
        System.out.println("✅ DataSeeder: Data seeding completed!");
    }

    private void seedRoles() {
        // Create all roles from the enum
        if (roleRepository.findByName(RoleName.ROLE_OWNER).isEmpty()) {
            roleRepository.save(new Role(RoleName.ROLE_OWNER));
        }
        if (roleRepository.findByName(RoleName.ROLE_ADMIN).isEmpty()) {
            roleRepository.save(new Role(RoleName.ROLE_ADMIN));
        }
        if (roleRepository.findByName(RoleName.ROLE_EMPLOYEE).isEmpty()) {
            roleRepository.save(new Role(RoleName.ROLE_EMPLOYEE));
        }
        if (roleRepository.findByName(RoleName.ROLE_CUSTOMER).isEmpty()) {
            roleRepository.save(new Role(RoleName.ROLE_CUSTOMER));
        }
    }

    private void seedDefaultStaff() {
        if (staffRepository.count() == 0) {
            System.out.println("🔑 Creating default staff accounts...");
            
            // Create Owner Staff
            Role adminRole = roleRepository.findByName(RoleName.ROLE_ADMIN)
                .orElseThrow(() -> new RuntimeException("Error: Admin role not found."));
            Set<Role> adminRoles = new HashSet<>();
            adminRoles.add(adminRole);

            StaffMember ownerStaff = new StaffMember();
            ownerStaff.setUsername("owner");
            ownerStaff.setEmail("owner@fruitstore.com");
            ownerStaff.setPassword(passwordEncoder.encode("owner123"));
            ownerStaff.setFirstName("Store");
            ownerStaff.setLastName("Owner");
            ownerStaff.setRoles(adminRoles);
            staffRepository.save(ownerStaff);
            System.out.println("✅ Default owner created: username='owner', password='owner123'");

            // Create Admin Staff for testing
            StaffMember adminStaff = new StaffMember();
            adminStaff.setUsername("admin");
            adminStaff.setEmail("admin@fruitstore.com");
            adminStaff.setPassword(passwordEncoder.encode("admin123"));
            adminStaff.setFirstName("Store");
            adminStaff.setLastName("Admin");
            adminStaff.setRoles(adminRoles);
            staffRepository.save(adminStaff);
            System.out.println("✅ Default admin created: username='admin', password='admin123'");
        } else {
            System.out.println("ℹ️ Staff accounts already exist, skipping creation");
        }
    }

    private void seedCategoriesAndFruits() {
        if (categoryRepository.count() == 0) {
            // Create Categories
            Category fruitsCategory = new Category("Fruits");
            Category vegetablesCategory = new Category("Vegetables");
            Category dairyCategory = new Category("Dairy");

            categoryRepository.saveAll(Arrays.asList(fruitsCategory, vegetablesCategory, dairyCategory));

            // Create Fruits
            createFruit("Apple", "Fresh, crisp red apples. Perfect for snacking or baking.", 1.50, fruitsCategory, 100, Unit.KG);
            createFruit("Banana", "Sweet, ripe bananas. Great source of potassium and energy.", 0.75, fruitsCategory, 150, Unit.KG);
            createFruit("Orange", "Juicy, vitamin C-rich oranges. Perfect for fresh juice.", 1.25, fruitsCategory, 120, Unit.KG);
            createFruit("Carrot", "Fresh, crunchy carrots. Excellent for cooking and salads.", 0.50, vegetablesCategory, 200, Unit.KG);
            createFruit("Broccoli", "Nutritious green broccoli crowns. Rich in vitamins and minerals.", 2.50, vegetablesCategory, 80, Unit.PIECE);
            createFruit("Milk", "Fresh whole milk. Perfect for drinking, cereal, and cooking.", 3.00, dairyCategory, 50, Unit.LITER);
            createFruit("Cheese", "Artisan cheese selection. Great for sandwiches and snacking.", 5.50, dairyCategory, 70, Unit.PACK);
        }
    }

    private void createFruit(String name, String description, double priceValue, Category category, int stock, Unit unit) {
        Fruit fruit = new Fruit();
        fruit.setName(name);
        fruit.setDescription(description);
        fruit.setPrice(BigDecimal.valueOf(priceValue));
        fruit.setCategory(category);
        fruit.setStock(stock);
        fruit.setUnit(unit);
        fruitRepository.save(fruit);
    }
} 