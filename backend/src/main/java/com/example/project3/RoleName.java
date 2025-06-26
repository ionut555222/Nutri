package com.example.project3;

public enum RoleName {
    ROLE_OWNER,      // Full system access - can manage everything including other staff
    ROLE_ADMIN,      // Administrative access - can manage customers, products, orders, campaigns
    ROLE_EMPLOYEE,   // Limited operational access - can fulfill orders, manage products, limited campaigns
    ROLE_CUSTOMER    // API-only access - for backward compatibility with existing customer records
} 