package com.newwork.employeeprofile.service;

import com.newwork.employeeprofile.domain.*;
import com.newwork.employeeprofile.dto.request.UpdateEmployeeRequest;
import com.newwork.employeeprofile.dto.response.EmployeeDto;
import com.newwork.employeeprofile.exception.BadRequestException;
import com.newwork.employeeprofile.exception.ResourceNotFoundException;
import com.newwork.employeeprofile.exception.UnauthorizedException;
import com.newwork.employeeprofile.mapper.EmployeeMapper;
import com.newwork.employeeprofile.repository.EmployeeRepository;
import com.newwork.employeeprofile.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final EmployeeMapper employeeMapper;
    private final DepartmentService departmentService;
    private final RoleService roleService;

    @Transactional(readOnly = true)
    public List<EmployeeDto> getAllEmployees(UserPrincipal currentUser) {
        log.info("Fetching all employees for user: {} with role: {}", currentUser.getEmail(), currentUser.getRole().getName());

        List<Employee> employees;

        // Check permission scope hierarchy: ALL > DEPARTMENT > OWN
        log.info("Current user {} with role name {} ",currentUser.getEmail(),currentUser.getRole().getName());
        if (currentUser.getRole().hasPermission(PermissionResource.EMPLOYEE, PermissionAction.READ, PermissionScope.ALL)) {
            log.info("At first block ");
            employees = employeeRepository.findAllActive();
        } else if (currentUser.getRole().hasPermission(PermissionResource.EMPLOYEE, PermissionAction.READ, PermissionScope.DEPARTMENT)) {
            log.info("At second block ");
            Employee user = employeeRepository.findById(currentUser.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));
            employees = employeeRepository.findByDepartmentId(user.getDepartment().getId());
        } else if (currentUser.getRole().hasPermission(PermissionResource.EMPLOYEE, PermissionAction.READ, PermissionScope.OWN)) {
            log.info("At third block ");
            Employee user = employeeRepository.findById(currentUser.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));
            employees = List.of(user);
        } else {
            throw new UnauthorizedException("You don't have permission to view employees");
        }

        return employees.stream()
                .map(employeeMapper::toDto)
                .map(dto -> filterSensitiveData(dto, currentUser))
                .toList();
    }

    @Transactional(readOnly = true)
    public EmployeeDto getEmployeeById(Long id, UserPrincipal currentUser) {
        log.info("Fetching employee {} for user: {}", id, currentUser.getEmail());

        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + id));

        validateReadAccess(employee, currentUser);

        log.info("What is cmng from repo :: {} and department :: {}",employee.getRole().getName(), employee.getDepartment().getName());
        employee.getRole().getPermissions().stream()
                .forEach(p -> log.info("Whats inside persmission {}",p.getName()));

        EmployeeDto dto= employeeMapper.toDto(employee);
        log.info("after mapstruct {}",dto.getRole().getName());
        return filterSensitiveData(dto, currentUser);
    }

    @Transactional
    public EmployeeDto updateEmployee(Long id, UpdateEmployeeRequest request, UserPrincipal currentUser) {
        log.info("Updating employee {} by user: {}", id, currentUser.getEmail());

        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + id));

        validateUpdateAccess(employee, currentUser);

        employeeMapper.updateEntityFromDto(request, employee);

        // Update department if provided
        if (request.getDepartmentId() != null) {
            employee.setDepartment(departmentService.getDepartmentEntityById(request.getDepartmentId()));
        }

        employee = employeeRepository.save(employee);

        log.info("Employee {} updated successfully", id);
        return employeeMapper.toDto(employee);
    }

    @Transactional
    public void deactivateEmployee(Long id, UserPrincipal currentUser) {
        log.info("Deactivating employee {} by user: {}", id, currentUser.getEmail());

        if (!currentUser.getRole().hasPermission(PermissionResource.EMPLOYEE, PermissionAction.DELETE, PermissionScope.ALL)) {
            throw new UnauthorizedException("You don't have permission to deactivate employees");
        }

        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + id));

        employee.setActive(false);
        employeeRepository.save(employee);

        log.info("Employee {} deactivated successfully", id);
    }

    @Transactional(readOnly = true)
    public List<EmployeeDto> searchEmployees(String keyword, UserPrincipal currentUser) {
        log.info("Searching employees with keyword: {} by user: {}", keyword, currentUser.getEmail());

        List<Employee> employees = employeeRepository.searchEmployees(keyword);

        // Filter results based on user's permission scope
        List<Employee> filteredEmployees = filterEmployeesByPermission(employees, currentUser);

        return filteredEmployees.stream()
                .map(employeeMapper::toDto)
                .map(dto -> filterSensitiveData(dto, currentUser))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<EmployeeDto> getEmployeesByDepartment(Long departmentId, UserPrincipal currentUser) {
        log.info("Fetching employees in department: {} by user: {}", departmentId, currentUser.getEmail());

        // Validate department exists
        departmentService.getDepartmentEntityById(departmentId);

        // Check if user can read employees in this department
        Employee user = employeeRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!currentUser.getRole().hasPermission(PermissionResource.EMPLOYEE, PermissionAction.READ, PermissionScope.ALL)) {
            if (!currentUser.getRole().hasPermission(PermissionResource.EMPLOYEE, PermissionAction.READ, PermissionScope.DEPARTMENT)) {
                throw new UnauthorizedException("You don't have permission to view employees by department");
            }
            // If DEPARTMENT scope, can only view own department
            if (!user.getDepartment().getId().equals(departmentId)) {
                throw new UnauthorizedException("You can only view employees in your own department");
            }
        }

        List<Employee> employees = employeeRepository.findByDepartmentId(departmentId);
        return employees.stream()
                .map(employeeMapper::toDto)
                .map(dto -> filterSensitiveData(dto, currentUser))
                .toList();
    }

    @Transactional
    public EmployeeDto promoteToManager(Long employeeId, Long departmentId, UserPrincipal currentUser) {
        log.info("Promoting employee {} to manager of department {} by user: {}", employeeId, departmentId, currentUser.getEmail());

        // Only users with UPDATE permission on EMPLOYEE:ALL can promote
        if (!currentUser.getRole().hasPermission(PermissionResource.EMPLOYEE, PermissionAction.UPDATE, PermissionScope.ALL)) {
            throw new UnauthorizedException("You don't have permission to promote employees to manager");
        }

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + employeeId));

        Department department = departmentService.getDepartmentEntityById(departmentId);

        // Assign MANAGER role
        employee.setRole(roleService.getManagerRole());

        // Set as department manager
        department.setManager(employee);

        employee = employeeRepository.save(employee);

        log.info("Employee {} promoted to manager successfully", employeeId);
        return employeeMapper.toDto(employee);
    }

    private void validateReadAccess(Employee employee, UserPrincipal currentUser) {
        log.info("I want to check {} whats inside validateRead Access", currentUser.toString());
        if (currentUser.getRole().hasPermission(PermissionResource.EMPLOYEE, PermissionAction.READ, PermissionScope.ALL)) {
            return;
        }

        Employee user = employeeRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (currentUser.getRole().hasPermission(PermissionResource.EMPLOYEE, PermissionAction.READ, PermissionScope.DEPARTMENT)) {
            if (employee.getDepartment().getId().equals(user.getDepartment().getId())) {
                return;
            }
        }

        if (currentUser.getRole().hasPermission(PermissionResource.EMPLOYEE, PermissionAction.READ, PermissionScope.OWN)) {
            if (employee.getId().equals(currentUser.getId())) {
                return;
            }
        }

        throw new UnauthorizedException("You don't have permission to view this employee");
    }

    private void validateUpdateAccess(Employee employee, UserPrincipal currentUser) {
        if (currentUser.getRole().hasPermission(PermissionResource.EMPLOYEE, PermissionAction.UPDATE, PermissionScope.ALL)) {
            return;
        }

        Employee user = employeeRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (currentUser.getRole().hasPermission(PermissionResource.EMPLOYEE, PermissionAction.UPDATE, PermissionScope.DEPARTMENT)) {
            if (employee.getDepartment().getId().equals(user.getDepartment().getId())) {
                return;
            }
        }

        if (currentUser.getRole().hasPermission(PermissionResource.EMPLOYEE, PermissionAction.UPDATE, PermissionScope.OWN)) {
            if (employee.getId().equals(currentUser.getId())) {
                return;
            }
        }

        throw new UnauthorizedException("You don't have permission to update this employee");
    }

    private List<Employee> filterEmployeesByPermission(List<Employee> employees, UserPrincipal currentUser) {
        if (currentUser.getRole().hasPermission(PermissionResource.EMPLOYEE, PermissionAction.READ, PermissionScope.ALL)) {
            return employees;
        }

        Employee user = employeeRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (currentUser.getRole().hasPermission(PermissionResource.EMPLOYEE, PermissionAction.READ, PermissionScope.DEPARTMENT)) {
            return employees.stream()
                    .filter(e -> e.getDepartment().getId().equals(user.getDepartment().getId()))
                    .toList();
        }

        if (currentUser.getRole().hasPermission(PermissionResource.EMPLOYEE, PermissionAction.READ, PermissionScope.OWN)) {
            return employees.stream()
                    .filter(e -> e.getId().equals(currentUser.getId()))
                    .toList();
        }

        return List.of();
    }

    /**
     * Filters sensitive data (phone, dateOfBirth, hireDate) based on access control.
     * Sensitive fields are only visible to:
     * 1. Managers (users with READ:ALL permission)
     * 2. The employee themselves (viewing own profile)
     *
     * Coworkers from the same department cannot see these fields.
     */
    private EmployeeDto filterSensitiveData(EmployeeDto dto, UserPrincipal currentUser) {
        // Managers can see everything
        if (currentUser.getRole().hasPermission(PermissionResource.EMPLOYEE, PermissionAction.READ, PermissionScope.ALL)) {
            log.debug("User {} has manager permissions, showing all sensitive data for employee {}",
                    currentUser.getEmail(), dto.getId());
            return dto;
        }

        // Employees can see their own sensitive data
        if (dto.getId().equals(currentUser.getId())) {
            log.debug("User {} viewing own profile, showing all sensitive data", currentUser.getEmail());
            return dto;
        }

        // Coworkers cannot see sensitive data
        log.debug("User {} viewing coworker {}, hiding sensitive data (phone, dateOfBirth, hireDate)",
                currentUser.getEmail(), dto.getId());
        dto.setPhone(null);
        dto.setDateOfBirth(null);
        dto.setHireDate(null);

        return dto;
    }
}
