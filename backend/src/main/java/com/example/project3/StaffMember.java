package com.example.project3;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "staff_members")
public class StaffMember {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "employee_id", unique = true)
    private String employeeId;

    @Column(name = "department")
    private String department;

    @Column(name = "hire_date")
    private LocalDateTime hireDate;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "last_login")
    private LocalDateTime lastLogin;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "created_by")
    private Long createdBy; // Staff member who created this account

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "staff_roles",
               joinColumns = @JoinColumn(name = "staff_id"),
               inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles = new HashSet<>();

    public StaffMember() {
        this.createdAt = LocalDateTime.now();
    }

    public StaffMember(String username, String email, String password, String firstName, String lastName) {
        this();
        this.username = username;
        this.email = email;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public LocalDateTime getHireDate() {
        return hireDate;
    }

    public void setHireDate(LocalDateTime hireDate) {
        this.hireDate = hireDate;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(LocalDateTime lastLogin) {
        this.lastLogin = lastLogin;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }

    public Set<Role> getRoles() {
        return roles;
    }

    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }

    public boolean hasRole(RoleName roleName) {
        return roles.stream().anyMatch(role -> role.getName() == roleName);
    }

    public boolean isOwner() {
        return hasRole(RoleName.ROLE_OWNER);
    }

    public boolean isAdmin() {
        return hasRole(RoleName.ROLE_ADMIN) || isOwner();
    }

    public boolean isEmployee() {
        return hasRole(RoleName.ROLE_EMPLOYEE) || isAdmin() || isOwner();
    }
} 