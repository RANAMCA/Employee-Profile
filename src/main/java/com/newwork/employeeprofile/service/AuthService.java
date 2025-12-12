package com.newwork.employeeprofile.service;

import com.newwork.employeeprofile.domain.Employee;
import com.newwork.employeeprofile.dto.request.CreateEmployeeRequest;
import com.newwork.employeeprofile.dto.request.LoginRequest;
import com.newwork.employeeprofile.dto.response.AuthResponse;
import com.newwork.employeeprofile.exception.BadRequestException;
import com.newwork.employeeprofile.mapper.EmployeeMapper;
import com.newwork.employeeprofile.repository.EmployeeRepository;
import com.newwork.employeeprofile.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final EmployeeRepository employeeRepository;
    private final EmployeeMapper employeeMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final RoleService roleService;
    private final DepartmentService departmentService;

    @Transactional
    public AuthResponse register(CreateEmployeeRequest request) {
        log.info("Registering new employee with email: {}", request.getEmail());

        if (employeeRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already in use");
        }

        Employee employee = employeeMapper.toEntity(request);
        employee.setPassword(passwordEncoder.encode(request.getPassword()));

        // Set default EMPLOYEE role
        employee.setRole(roleService.getDefaultEmployeeRole());

        // Set department
        employee.setDepartment(departmentService.getDepartmentEntityById(request.getDepartmentId()));

        employee = employeeRepository.save(employee);

        String token = tokenProvider.generateToken(employee.getId(), employee.getEmail(), false);
        String refreshToken = tokenProvider.generateRefreshToken(employee.getId(), employee.getEmail());

        log.info("Employee registered successfully with ID: {}", employee.getId());

        return AuthResponse.builder()
                .token(token)
                .refreshToken(refreshToken)
                .success(true)
                .expiresIn(tokenProvider.getExpirationMs())
                .employeeId(employee.getId())
                .email(employee.getEmail())
                .fullName(employee.getFullName())
                .roleName(employee.getRole().getName())
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        try{
            log.info("Login attempt for email: {}", request.getEmail());

            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
        } catch (BadCredentialsException e) {
            log.warn("Login failed for email: {} - Wrong credentials", request.getEmail());
            return AuthResponse.builder()
                    .success(false)
                    .message("Wrong credentials")
                    .build();

        } catch (DisabledException e) {
            log.warn("Login failed for email: {} - Account disabled", request.getEmail());
            return AuthResponse.builder()
                    .success(false)
                    .message("Account is disabled")
                    .build();

        } catch (LockedException e) {
            log.warn("Login failed for email: {} - Account locked", request.getEmail());
            return AuthResponse.builder()
                    .success(false)
                    .message("Account is locked")
                    .build();

        } catch (Exception e) {
            log.error("Login failed for email: {} - Unexpected error", request.getEmail(), e);
            return AuthResponse.builder()
                    .success(false)
                    .message("Authentication failed")
                    .build();
        }
        Employee employee = employeeRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadRequestException("Invalid credentials"));

        if (!employee.getActive()) {
            return AuthResponse.builder()
                    .success(false)
                    .message("Account is deactivated")
                    .build();
        }

        String token = tokenProvider.generateToken(employee.getId(), employee.getEmail(), false);
        String refreshToken = tokenProvider.generateRefreshToken(employee.getId(), employee.getEmail());

        log.info("Login successful for employee ID: {}", employee.getId());

        return AuthResponse.builder()
                .token(token)
                .refreshToken(refreshToken)
                .success(true)
                .expiresIn(tokenProvider.getExpirationMs())
                .employeeId(employee.getId())
                .email(employee.getEmail())
                .fullName(employee.getFullName())
                .roleName(employee.getRole().getName())
                .build();
    }
}
