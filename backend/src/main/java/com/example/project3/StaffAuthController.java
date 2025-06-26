package com.example.project3;

import com.example.project3.payload.request.LoginRequest;
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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth/staff")
public class StaffAuthController {
    
    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    StaffRepository staffRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    JwtUtils jwtUtils;

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateStaff(@RequestBody LoginRequest loginRequest) {
        try {
            // Find staff by username or email
            StaffMember staff = staffRepository.findByUsername(loginRequest.getUsername())
                    .orElse(staffRepository.findByEmail(loginRequest.getUsername()).orElse(null));
            
            if (staff == null) {
                return ResponseEntity.badRequest()
                        .body(new MessageResponse("Error: Staff member not found!"));
            }

            if (!staff.getIsActive()) {
                return ResponseEntity.badRequest()
                        .body(new MessageResponse("Error: Account is deactivated!"));
            }

            // Create authentication using staff details
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(staff.getUsername(), loginRequest.getPassword()));

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = jwtUtils.generateStaffJwtToken(staff);

            // Update last login
            staff.setLastLogin(LocalDateTime.now());
            staffRepository.save(staff);

            List<String> roles = staff.getRoles().stream()
                    .map(role -> role.getName().name())
                    .collect(Collectors.toList());

            // Return staff-specific JWT response
            return ResponseEntity.ok(new StaffJwtResponse(
                    jwt,
                    staff.getId(),
                    staff.getUsername(),
                    staff.getEmail(),
                    staff.getFullName(),
                    staff.getDepartment(),
                    staff.getEmployeeId(),
                    roles
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: Invalid credentials!"));
        }
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerStaff(@Valid @RequestBody StaffSignupRequest signUpRequest) {
        // Only owners and admins can create staff accounts
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !hasPermissionToCreateStaff(auth)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new MessageResponse("Error: Insufficient permissions!"));
        }

        if (staffRepository.existsByUsername(signUpRequest.getUsername())) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: Username is already taken!"));
        }

        if (staffRepository.existsByEmail(signUpRequest.getEmail())) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: Email is already in use!"));
        }

        if (signUpRequest.getEmployeeId() != null && 
            staffRepository.existsByEmployeeId(signUpRequest.getEmployeeId())) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: Employee ID is already in use!"));
        }

        // Create new staff member account
        StaffMember staff = new StaffMember(
                signUpRequest.getUsername(),
                signUpRequest.getEmail(),
                encoder.encode(signUpRequest.getPassword()),
                signUpRequest.getFirstName(),
                signUpRequest.getLastName()
        );

        staff.setEmployeeId(signUpRequest.getEmployeeId());
        staff.setDepartment(signUpRequest.getDepartment());
        staff.setHireDate(LocalDateTime.now());

        // Set roles based on request
        Set<Role> roles = new HashSet<>();
        Set<String> strRoles = signUpRequest.getRoles();

        if (strRoles == null || strRoles.isEmpty()) {
            Role employeeRole = roleRepository.findByName(RoleName.ROLE_EMPLOYEE)
                    .orElseThrow(() -> new RuntimeException("Error: Employee role not found."));
            roles.add(employeeRole);
        } else {
            strRoles.forEach(role -> {
                switch (role.toLowerCase()) {
                    case "owner":
                        // Only existing owners can create new owners
                        if (isCurrentUserOwner(auth)) {
                            Role ownerRole = roleRepository.findByName(RoleName.ROLE_OWNER)
                                    .orElseThrow(() -> new RuntimeException("Error: Owner role not found."));
                            roles.add(ownerRole);
                        }
                        break;
                    case "admin":
                        // Owners can create admins
                        if (isCurrentUserOwner(auth)) {
                            Role adminRole = roleRepository.findByName(RoleName.ROLE_ADMIN)
                                    .orElseThrow(() -> new RuntimeException("Error: Admin role not found."));
                            roles.add(adminRole);
                        }
                        break;
                    case "employee":
                    default:
                        Role employeeRole = roleRepository.findByName(RoleName.ROLE_EMPLOYEE)
                                .orElseThrow(() -> new RuntimeException("Error: Employee role not found."));
                        roles.add(employeeRole);
                }
            });
        }

        staff.setRoles(roles);
        
        // Set creator information
        StaffMember creator = getCurrentStaffMember(auth);
        if (creator != null) {
            staff.setCreatedBy(creator.getId());
        }

        staffRepository.save(staff);

        return ResponseEntity.ok(new MessageResponse("Staff member registered successfully!"));
    }

    private boolean hasPermissionToCreateStaff(Authentication auth) {
        return auth.getAuthorities().stream()
                .anyMatch(grantedAuthority -> 
                    grantedAuthority.getAuthority().equals("ROLE_OWNER") ||
                    grantedAuthority.getAuthority().equals("ROLE_ADMIN"));
    }

    private boolean isCurrentUserOwner(Authentication auth) {
        return auth.getAuthorities().stream()
                .anyMatch(grantedAuthority -> 
                    grantedAuthority.getAuthority().equals("ROLE_OWNER"));
    }

    private StaffMember getCurrentStaffMember(Authentication auth) {
        String username = auth.getName();
        return staffRepository.findByUsername(username).orElse(null);
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

    // Custom response class for staff authentication
    public static class StaffJwtResponse {
        private String token;
        private String type = "Bearer";
        private Long id;
        private String username;
        private String email;
        private String fullName;
        private String department;
        private String employeeId;
        private List<String> roles;

        public StaffJwtResponse(String accessToken, Long id, String username, String email, 
                               String fullName, String department, String employeeId, List<String> roles) {
            this.token = accessToken;
            this.id = id;
            this.username = username;
            this.email = email;
            this.fullName = fullName;
            this.department = department;
            this.employeeId = employeeId;
            this.roles = roles;
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
        public String getDepartment() { return department; }
        public void setDepartment(String department) { this.department = department; }
        public String getEmployeeId() { return employeeId; }
        public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }
        public List<String> getRoles() { return roles; }
        public void setRoles(List<String> roles) { this.roles = roles; }
    }

    // Custom signup request for staff
    public static class StaffSignupRequest {
        private String username;
        private String email;
        private String password;
        private String firstName;
        private String lastName;
        private String employeeId;
        private String department;
        private Set<String> roles;

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
        public String getEmployeeId() { return employeeId; }
        public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }
        public String getDepartment() { return department; }
        public void setDepartment(String department) { this.department = department; }
        public Set<String> getRoles() { return roles; }
        public void setRoles(Set<String> roles) { this.roles = roles; }
    }
} 