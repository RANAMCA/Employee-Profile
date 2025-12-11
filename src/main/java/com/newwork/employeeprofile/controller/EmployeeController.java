package com.newwork.employeeprofile.controller;

import com.fasterxml.jackson.annotation.JsonView;
import com.newwork.employeeprofile.dto.request.PromoteToManagerRequest;
import com.newwork.employeeprofile.dto.request.UpdateEmployeeRequest;
import com.newwork.employeeprofile.dto.response.EmployeeDto;
import com.newwork.employeeprofile.security.UserPrincipal;
import com.newwork.employeeprofile.service.EmployeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearer-jwt")
@Tag(name = "Employees", description = "Employee management endpoints")
public class EmployeeController {

    private final EmployeeService employeeService;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('EMPLOYEE:READ:ALL', 'EMPLOYEE:READ:DEPARTMENT', 'EMPLOYEE:READ:OWN')")
    @JsonView(EmployeeDto.Views.Extended.class)
    @Operation(summary = "Get all employees", description = "Retrieves employees based on permission scope")
    public ResponseEntity<List<EmployeeDto>> getAllEmployees(@AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(employeeService.getAllEmployees(currentUser));
    }

    @GetMapping("/{id}")
    @JsonView(EmployeeDto.Views.Full.class)
    @Operation(summary = "Get employee by ID", description = "Retrieves employee details based on role permissions")
    public ResponseEntity<EmployeeDto> getEmployeeById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        EmployeeDto employee = employeeService.getEmployeeById(id, currentUser);
        log.info("check at controller level {}::", employee.getRole().getName());
        return ResponseEntity.ok(employee);
    }

    @PutMapping("/{id}")
    @JsonView(EmployeeDto.Views.Full.class)
    @Operation(summary = "Update employee", description = "Updates employee information (Own profile or Manager)")
    public ResponseEntity<EmployeeDto> updateEmployee(
            @PathVariable Long id,
            @Valid @RequestBody UpdateEmployeeRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(employeeService.updateEmployee(id, request, currentUser));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('EMPLOYEE:DELETE:ALL')")
    @Operation(summary = "Deactivate employee", description = "Deactivates an employee account")
    public ResponseEntity<Void> deactivateEmployee(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        employeeService.deactivateEmployee(id, currentUser);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyAuthority('EMPLOYEE:READ:ALL', 'EMPLOYEE:READ:DEPARTMENT', 'EMPLOYEE:READ:OWN')")
    @JsonView(EmployeeDto.Views.Extended.class)
    @Operation(summary = "Search employees", description = "Searches employees by keyword based on permission scope")
    public ResponseEntity<List<EmployeeDto>> searchEmployees(
            @RequestParam String keyword,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(employeeService.searchEmployees(keyword, currentUser));
    }

    @GetMapping("/department/{departmentId}")
    @PreAuthorize("hasAnyAuthority('EMPLOYEE:READ:ALL', 'EMPLOYEE:READ:DEPARTMENT')")
    @JsonView(EmployeeDto.Views.Extended.class)
    @Operation(summary = "Get employees by department", description = "Retrieves all employees in a department")
    public ResponseEntity<List<EmployeeDto>> getEmployeesByDepartment(
            @PathVariable Long departmentId,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(employeeService.getEmployeesByDepartment(departmentId, currentUser));
    }

    @PostMapping("/promote")
    @PreAuthorize("hasAuthority('EMPLOYEE:UPDATE:ALL')")
    @JsonView(EmployeeDto.Views.Full.class)
    @Operation(summary = "Promote employee to manager", description = "Promotes an employee to manager role and assigns them to a department")
    public ResponseEntity<EmployeeDto> promoteToManager(
            @Valid @RequestBody PromoteToManagerRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(employeeService.promoteToManager(
                request.getEmployeeId(),
                request.getDepartmentId(),
                currentUser));
    }
}
