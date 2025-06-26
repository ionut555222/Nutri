package com.example.project3;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class StaffDetailsImpl implements UserDetails {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String username;
    private String email;
    private String employeeId;
    private String firstName;
    private String lastName;
    private String department;
    @JsonIgnore
    private String password;
    private boolean isActive;

    private Collection<? extends GrantedAuthority> authorities;

    public StaffDetailsImpl(Long id, String username, String email, String employeeId, 
                           String firstName, String lastName, String department, String password,
                           boolean isActive, Collection<? extends GrantedAuthority> authorities) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.employeeId = employeeId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.department = department;
        this.password = password;
        this.isActive = isActive;
        this.authorities = authorities;
    }

    public static StaffDetailsImpl build(StaffMember staff) {
        List<GrantedAuthority> authorities = staff.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.getName().name()))
                .collect(Collectors.toList());

        return new StaffDetailsImpl(
                staff.getId(),
                staff.getUsername(),
                staff.getEmail(),
                staff.getEmployeeId(),
                staff.getFirstName(),
                staff.getLastName(),
                staff.getDepartment(),
                staff.getPassword(),
                staff.getIsActive(),
                authorities);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }
    
    public String getEmployeeId() {
        return employeeId;
    }
    
    public String getFirstName() {
        return firstName;
    }
    
    public String getLastName() {
        return lastName;
    }
    
    public String getFullName() {
        return firstName + " " + lastName;
    }
    
    public String getDepartment() {
        return department;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return isActive;
    }

    @Override
    public boolean isAccountNonLocked() {
        return isActive;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return isActive;
    }

    @Override
    public boolean isEnabled() {
        return isActive;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StaffDetailsImpl staff = (StaffDetailsImpl) o;
        return Objects.equals(id, staff.id);
    }
} 