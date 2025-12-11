package com.newwork.employeeprofile.service;

import com.newwork.employeeprofile.domain.Department;
import com.newwork.employeeprofile.domain.Employee;
import com.newwork.employeeprofile.dto.request.CreateDepartmentRequest;
import com.newwork.employeeprofile.dto.request.UpdateDepartmentRequest;
import com.newwork.employeeprofile.dto.response.DepartmentDto;
import com.newwork.employeeprofile.exception.BadRequestException;
import com.newwork.employeeprofile.exception.ResourceNotFoundException;
import com.newwork.employeeprofile.mapper.DepartmentMapper;
import com.newwork.employeeprofile.repository.DepartmentRepository;
import com.newwork.employeeprofile.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final EmployeeRepository employeeRepository;
    private final DepartmentMapper departmentMapper;

    @Transactional(readOnly = true)
    public List<DepartmentDto> getAllDepartments() {
        log.info("Fetching all departments");
        return departmentRepository.findAll().stream()
                .map(departmentMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public DepartmentDto getDepartmentById(Long id) {
        log.info("Fetching department with id: {}", id);
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + id));
        return departmentMapper.toDto(department);
    }

    @Transactional
    public DepartmentDto createDepartment(CreateDepartmentRequest request) {
        log.info("Creating new department: {}", request.getName());

        if (departmentRepository.findByName(request.getName()).isPresent()) {
            throw new BadRequestException("Department with name '" + request.getName() + "' already exists");
        }

        Department department = departmentMapper.toEntity(request);

        // Set manager if provided
        if (request.getManagerId() != null) {
            Employee manager = employeeRepository.findById(request.getManagerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Manager not found with id: " + request.getManagerId()));
            department.setManager(manager);
        }

        // Set parent department if provided
        if (request.getParentDepartmentId() != null) {
            Department parentDepartment = departmentRepository.findById(request.getParentDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Parent department not found with id: " + request.getParentDepartmentId()));
            department.setParentDepartment(parentDepartment);
        }

        department = departmentRepository.save(department);
        log.info("Department created successfully with ID: {}", department.getId());

        return departmentMapper.toDto(department);
    }

    @Transactional
    public DepartmentDto updateDepartment(Long id, UpdateDepartmentRequest request) {
        log.info("Updating department with id: {}", id);

        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + id));

        // Check if name is being changed and if it conflicts with existing department
        if (request.getName() != null && !request.getName().equals(department.getName())) {
            if (departmentRepository.findByName(request.getName()).isPresent()) {
                throw new BadRequestException("Department with name '" + request.getName() + "' already exists");
            }
        }

        departmentMapper.updateEntityFromDto(request, department);

        // Update manager if provided
        if (request.getManagerId() != null) {
            Employee manager = employeeRepository.findById(request.getManagerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Manager not found with id: " + request.getManagerId()));
            department.setManager(manager);
        }

        // Update parent department if provided
        if (request.getParentDepartmentId() != null) {
            Department parentDepartment = departmentRepository.findById(request.getParentDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Parent department not found with id: " + request.getParentDepartmentId()));
            department.setParentDepartment(parentDepartment);
        }

        department = departmentRepository.save(department);
        log.info("Department {} updated successfully", id);

        return departmentMapper.toDto(department);
    }

    @Transactional
    public void deleteDepartment(Long id) {
        log.info("Deleting department with id: {}", id);

        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + id));

        // Check if department has employees
        if (!department.getEmployees().isEmpty()) {
            throw new BadRequestException("Cannot delete department with existing employees. Please reassign employees first.");
        }

        // Check if department has sub-departments
        if (!department.getSubDepartments().isEmpty()) {
            throw new BadRequestException("Cannot delete department with sub-departments. Please delete or reassign sub-departments first.");
        }

        departmentRepository.delete(department);
        log.info("Department {} deleted successfully", id);
    }

    @Transactional(readOnly = true)
    public Department getDepartmentEntityById(Long id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + id));
    }
}
