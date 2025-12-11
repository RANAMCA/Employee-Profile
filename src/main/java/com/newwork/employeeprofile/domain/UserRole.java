package com.newwork.employeeprofile.domain;

public enum UserRole {
    EMPLOYEE,   // Default role: Access to own profile and absence requests
    MANAGER     // Elevated role: Full access to department employees and approval workflows
}
