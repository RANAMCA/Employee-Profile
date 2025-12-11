package com.newwork.employeeprofile.service;

import com.newwork.employeeprofile.domain.Role;
import com.newwork.employeeprofile.dto.response.RoleDto;
import com.newwork.employeeprofile.exception.ResourceNotFoundException;
import com.newwork.employeeprofile.mapper.RoleMapper;
import com.newwork.employeeprofile.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoleService {

    private final RoleRepository roleRepository;
    private final RoleMapper roleMapper;

    @Transactional(readOnly = true)
    public Role getDefaultEmployeeRole() {
        return roleRepository.findByName("EMPLOYEE")
                .orElseThrow(() -> new ResourceNotFoundException("Default EMPLOYEE role not found"));
    }

    @Transactional(readOnly = true)
    public Role getManagerRole() {
        return roleRepository.findByName("MANAGER")
                .orElseThrow(() -> new ResourceNotFoundException("MANAGER role not found"));
    }

    @Transactional(readOnly = true)
    public Role getRoleById(Long id) {
        return roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public Role getRoleByName(String name) {
        return roleRepository.findByName(name)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with name: " + name));
    }

    @Transactional(readOnly = true)
    public List<RoleDto> getAllRoles() {
        log.info("Fetching all roles");
        return roleRepository.findAll().stream()
                .map(roleMapper::toDto)
                .toList();
    }
}
