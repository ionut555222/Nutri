package com.example.project3;

import com.example.project3.payload.request.LoginRequest;
import com.example.project3.payload.request.SignupRequest;
import com.example.project3.payload.response.JwtResponse;
import com.example.project3.payload.response.MessageResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth/customer")
public class CustomerAuthController {
    
    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    CustomerRepository customerRepository;

    @Autowired
    CustomerProfileService customerProfileService;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    JwtUtils jwtUtils;

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateCustomer(@RequestBody LoginRequest loginRequest) {
        try {
            // Find customer by username or email
            Customer customer = customerRepository.findByUsername(loginRequest.getUsername())
                    .orElse(customerRepository.findByEmail(loginRequest.getUsername()).orElse(null));
            
            if (customer == null) {
                return ResponseEntity.badRequest()
                        .body(new MessageResponse("Error: Customer not found!"));
            }

            if (!customer.getIsActive()) {
                return ResponseEntity.badRequest()
                        .body(new MessageResponse("Error: Account is deactivated!"));
            }

            // Verify password directly (customers don't use the staff authentication system)
            if (!encoder.matches(loginRequest.getPassword(), customer.getPassword())) {
                return ResponseEntity.badRequest()
                        .body(new MessageResponse("Error: Invalid credentials!"));
            }

            String jwt = jwtUtils.generateCustomerJwtToken(customer);

            // Update last login
            customer.setLastLogin(LocalDateTime.now());
            customerRepository.save(customer);

            // Return customer-specific JWT response
            return ResponseEntity.ok(new CustomerJwtResponse(
                    jwt,
                    customer.getId(),
                    customer.getUsername(),
                    customer.getEmail(),
                    customer.getFullName(),
                    customer.getEmailVerified()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: Invalid credentials!"));
        }
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerCustomer(@Valid @RequestBody CustomerSignupRequest signUpRequest) {
        if (customerRepository.existsByUsername(signUpRequest.getUsername())) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: Username is already taken!"));
        }

        if (customerRepository.existsByEmail(signUpRequest.getEmail())) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: Email is already in use!"));
        }

        // Create new customer account
        Customer customer = new Customer(
                signUpRequest.getUsername(),
                signUpRequest.getEmail(),
                encoder.encode(signUpRequest.getPassword())
        );

        customer.setFirstName(signUpRequest.getFirstName());
        customer.setLastName(signUpRequest.getLastName());
        customer.setPhoneNumber(signUpRequest.getPhoneNumber());

        Customer savedCustomer = customerRepository.save(customer);

        // Create customer profile
        customerProfileService.createInitialCustomerProfile(savedCustomer);

        return ResponseEntity.ok(new MessageResponse("Customer registered successfully!"));
    }

    @PostMapping("/verify-email/{customerId}")
    public ResponseEntity<?> verifyEmail(@PathVariable Long customerId, @RequestParam String token) {
        try {
            Customer customer = customerRepository.findById(customerId)
                    .orElseThrow(() -> new RuntimeException("Customer not found"));
            
            // Verify email token logic here
            customer.setEmailVerified(true);
            customerRepository.save(customer);
            
            return ResponseEntity.ok(new MessageResponse("Email verified successfully!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: Invalid verification token!"));
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestParam String email) {
        try {
            Customer customer = customerRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Customer not found"));
            
            // Send password reset email logic here
            return ResponseEntity.ok(new MessageResponse("Password reset email sent!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: Email not found!"));
        }
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Map<String, String> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return errors;
    }

    // Custom response class for customer authentication
    public static class CustomerJwtResponse {
        private String token;
        private String type = "Bearer";
        private Long id;
        private String username;
        private String email;
        private String fullName;
        private Boolean emailVerified;

        public CustomerJwtResponse(String accessToken, Long id, String username, String email, String fullName, Boolean emailVerified) {
            this.token = accessToken;
            this.id = id;
            this.username = username;
            this.email = email;
            this.fullName = fullName;
            this.emailVerified = emailVerified;
        }

        // Getters and setters
        public String getToken() { return token; }
        public void setToken(String token) { this.token = token; }
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }
        public Boolean getEmailVerified() { return emailVerified; }
        public void setEmailVerified(Boolean emailVerified) { this.emailVerified = emailVerified; }
    }

    // Custom signup request for customers
    public static class CustomerSignupRequest {
        private String username;
        private String email;
        private String password;
        private String firstName;
        private String lastName;
        private String phoneNumber;

        // Getters and setters
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        public String getFirstName() { return firstName; }
        public void setFirstName(String firstName) { this.firstName = firstName; }
        public String getLastName() { return lastName; }
        public void setLastName(String lastName) { this.lastName = lastName; }
        public String getPhoneNumber() { return phoneNumber; }
        public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    }
} 