package com.newwork.employeeprofile.controller;

import com.newwork.employeeprofile.dto.request.CreateDepartmentRequest;
import com.newwork.employeeprofile.dto.request.UpdateDepartmentRequest;
import com.newwork.employeeprofile.dto.response.DepartmentDto;
import com.newwork.employeeprofile.service.DepartmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/departments")
@RequiredArgsConstructor
public class DepartmentController {

    private final DepartmentService departmentService;

    @GetMapping("/public")
    public ResponseEntity<List<DepartmentDto>> getAllDepartmentsPublic() {
        log.info("GET /api/departments/public - Get all departments (public)");
        List<DepartmentDto> departments = departmentService.getAllDepartments();
        return ResponseEntity.ok(departments);
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('DEPARTMENT:READ:ALL', 'DEPARTMENT:READ:DEPARTMENT', 'DEPARTMENT:READ:OWN')")
    public ResponseEntity<List<DepartmentDto>> getAllDepartments() {
        log.info("GET /api/departments - Get all departments");
        List<DepartmentDto> departments = departmentService.getAllDepartments();
        return ResponseEntity.ok(departments);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('DEPARTMENT:READ:ALL', 'DEPARTMENT:READ:DEPARTMENT', 'DEPARTMENT:READ:OWN')")
    public ResponseEntity<DepartmentDto> getDepartmentById(@PathVariable Long id) {
        log.info("GET /api/departments/{} - Get department by ID", id);
        DepartmentDto department = departmentService.getDepartmentById(id);
        return ResponseEntity.ok(department);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('DEPARTMENT:CREATE:ALL')")
    public ResponseEntity<DepartmentDto> createDepartment(@Valid @RequestBody CreateDepartmentRequest request) {
        log.info("POST /api/departments - Create department: {}", request.getName());
        DepartmentDto department = departmentService.createDepartment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(department);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('DEPARTMENT:UPDATE:ALL')")
    public ResponseEntity<DepartmentDto> updateDepartment(
            @PathVariable Long id,
            @Valid @RequestBody UpdateDepartmentRequest request) {
        log.info("PUT /api/departments/{} - Update department", id);
        DepartmentDto department = departmentService.updateDepartment(id, request);
        return ResponseEntity.ok(department);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('DEPARTMENT:DELETE:ALL')")
    public ResponseEntity<Void> deleteDepartment(@PathVariable Long id) {
        log.info("DELETE /api/departments/{} - Delete department", id);
        departmentService.deleteDepartment(id);
        return ResponseEntity.noContent().build();
    }
}
