package com.newwork.employeeprofile.controller;

import com.newwork.employeeprofile.dto.response.RoleDto;
import com.newwork.employeeprofile.service.RoleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @GetMapping
    //@PreAuthorize("hasAuthority('ROLE:READ:ALL')")
    public ResponseEntity<List<RoleDto>> getAllRoles() {
        log.info("GET /api/roles - Get all roles");
        List<RoleDto> roles = roleService.getAllRoles();
        return ResponseEntity.ok(roles);
    }
}
