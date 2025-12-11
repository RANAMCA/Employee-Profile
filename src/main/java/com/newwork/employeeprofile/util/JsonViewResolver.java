package com.newwork.employeeprofile.util;

import com.newwork.employeeprofile.domain.Employee;
import com.newwork.employeeprofile.exception.ResourceNotFoundException;
import com.newwork.employeeprofile.repository.EmployeeRepository;
import com.newwork.employeeprofile.security.JsonViewSecurity;
import com.newwork.employeeprofile.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JsonViewResolver {

//  private final EmployeeRepository employeeRepository;
//
//  public Class<?> resolveJsonView(Long targetEmployeeId) {
//    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//
//    if (authentication == null || !authentication.isAuthenticated()) {
//      return JsonViewSecurity.PublicView.class;
//    }
//
//    UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
//    Long currentEmployeeId = userPrincipal.getId();
//
//    // Check if current user is the employee
//    if (currentEmployeeId != null && currentEmployeeId.equals(targetEmployeeId)) {
//      return JsonViewSecurity.OwnerOrManagerView.class;
//    }
//
//    // Check if current user is the manager
//    // You'll need to fetch this from database
//    Employee employee = employeeRepository.findById(targetEmployeeId)
//            .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));
//
//    if (employee.getDepartment().getManager() != null &&
//            employee.getDepartment().getManager().getId().equals(currentEmployeeId)) {
//      return JsonViewSecurity.OwnerOrManagerView.class;
//    }
//
//    return JsonViewSecurity.PublicView.class;
//  }
}
