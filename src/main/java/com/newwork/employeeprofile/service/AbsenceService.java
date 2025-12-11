package com.newwork.employeeprofile.service;

import com.newwork.employeeprofile.domain.Absence;
import com.newwork.employeeprofile.domain.AbsenceStatus;
import com.newwork.employeeprofile.domain.Employee;
import com.newwork.employeeprofile.dto.request.CreateAbsenceRequest;
import com.newwork.employeeprofile.dto.request.ReviewAbsenceRequest;
import com.newwork.employeeprofile.dto.response.AbsenceDto;
import com.newwork.employeeprofile.exception.BadRequestException;
import com.newwork.employeeprofile.exception.ResourceNotFoundException;
import com.newwork.employeeprofile.exception.UnauthorizedException;
import com.newwork.employeeprofile.mapper.AbsenceMapper;
import com.newwork.employeeprofile.repository.AbsenceRepository;
import com.newwork.employeeprofile.repository.EmployeeRepository;
import com.newwork.employeeprofile.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AbsenceService {

    private final AbsenceRepository absenceRepository;
    private final EmployeeRepository employeeRepository;
    private final AbsenceMapper absenceMapper;

    @Transactional
    public AbsenceDto createAbsence(CreateAbsenceRequest request, UserPrincipal currentUser) {
        log.info("Creating absence request for employee: {}", currentUser.getId());

        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new BadRequestException("End date must be after start date");
        }

        Employee employee = employeeRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));

        List<Absence> conflicts = absenceRepository.findConflictingAbsences(
                employee.getId(),
                request.getStartDate(),
                request.getEndDate()
        );

        if (!conflicts.isEmpty()) {
            throw new BadRequestException("You already have an absence request for this period");
        }

        Absence absence = absenceMapper.toEntity(request);
        absence.setEmployee(employee);
        absence = absenceRepository.save(absence);

        log.info("Absence request created with ID: {}", absence.getId());
        return absenceMapper.toDto(absence);
    }

    @Transactional(readOnly = true)
    public List<AbsenceDto> getMyAbsences(UserPrincipal currentUser) {
        log.info("Fetching absences for employee: {}", currentUser.getId());

        List<Absence> absences = absenceRepository.findByEmployeeId(currentUser.getId());
        return absences.stream()
                .map(absenceMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AbsenceDto> getAllAbsences(UserPrincipal currentUser) {
        log.info("Fetching all absences by user: {}", currentUser.getEmail());

        if (!"MANAGER".equals(currentUser.getRole().getName())) {
            throw new UnauthorizedException("Only managers can view all absence requests");
        }

        Employee manager = employeeRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Manager not found"));

        // Get all absences from employees in manager's department
        List<Absence> absences = absenceRepository.findAll();

        return absences.stream()
                .filter(absence -> {
                    Employee employee = absence.getEmployee();
                    // Only show requests from employees in the same department
                    return employee.getDepartment() != null &&
                           manager.getDepartment() != null &&
                           employee.getDepartment().getId().equals(manager.getDepartment().getId());
                })
                .map(absenceMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AbsenceDto> getPendingAbsences(UserPrincipal currentUser) {
        log.info("Fetching pending absences by user: {}", currentUser.getEmail());

        if (!"MANAGER".equals(currentUser.getRole().getName())) {
            throw new UnauthorizedException("Only managers can view pending absence requests");
        }

        Employee manager = employeeRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Manager not found"));

        // Get all pending absences from employees in manager's department
        List<Absence> absences = absenceRepository.findByStatus(AbsenceStatus.PENDING);

        return absences.stream()
                .filter(absence -> {
                    Employee employee = absence.getEmployee();
                    // Only show requests from employees in the same department
                    return employee.getDepartment() != null &&
                           manager.getDepartment() != null &&
                           employee.getDepartment().getId().equals(manager.getDepartment().getId());
                })
                .map(absenceMapper::toDto)
                .toList();
    }

    @Transactional
    public AbsenceDto reviewAbsence(Long id, ReviewAbsenceRequest request, UserPrincipal currentUser) {
        log.info("Reviewing absence {} by user: {}", id, currentUser.getEmail());

        if (!"MANAGER".equals(currentUser.getRole().getName())) {
            throw new UnauthorizedException("Only managers can review absence requests");
        }

        Absence absence = absenceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Absence not found with id: " + id));

        Employee manager = employeeRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Manager not found"));

        // Ensure manager is in the same department as the employee
        Employee employee = absence.getEmployee();
        if (employee.getDepartment() == null || manager.getDepartment() == null ||
            !employee.getDepartment().getId().equals(manager.getDepartment().getId())) {
            throw new UnauthorizedException("You can only review absence requests from employees in your department");
        }

        if (absence.getStatus() != AbsenceStatus.PENDING) {
            throw new BadRequestException("Absence request has already been reviewed");
        }

        if (request.getStatus() == AbsenceStatus.PENDING) {
            throw new BadRequestException("Invalid status for review");
        }

        absence.setStatus(request.getStatus());
        absence.setReviewedBy(currentUser.getId());
        absence.setReviewedAt(LocalDateTime.now());
        absence.setReviewComment(request.getReviewComment());

        absence = absenceRepository.save(absence);

        log.info("Absence {} reviewed with status: {}", id, request.getStatus());
        return absenceMapper.toDto(absence);
    }

    @Transactional
    public void cancelAbsence(Long id, UserPrincipal currentUser) {
        log.info("Cancelling absence {} by user: {}", id, currentUser.getId());

        Absence absence = absenceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Absence not found with id: " + id));

        if (!absence.getEmployee().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException("You can only cancel your own absence requests");
        }

        if (absence.getStatus() == AbsenceStatus.CANCELLED) {
            throw new BadRequestException("Absence request is already cancelled");
        }

        absence.setStatus(AbsenceStatus.CANCELLED);
        absenceRepository.save(absence);

        log.info("Absence {} cancelled successfully", id);
    }
}
