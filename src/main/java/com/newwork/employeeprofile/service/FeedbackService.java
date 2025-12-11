package com.newwork.employeeprofile.service;

import com.newwork.employeeprofile.domain.Employee;
import com.newwork.employeeprofile.domain.Feedback;
import com.newwork.employeeprofile.dto.request.CreateFeedbackRequest;
import com.newwork.employeeprofile.dto.response.FeedbackDto;
import com.newwork.employeeprofile.exception.ResourceNotFoundException;
import com.newwork.employeeprofile.exception.UnauthorizedException;
import com.newwork.employeeprofile.mapper.FeedbackMapper;
import com.newwork.employeeprofile.repository.EmployeeRepository;
import com.newwork.employeeprofile.repository.FeedbackRepository;
import com.newwork.employeeprofile.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final EmployeeRepository employeeRepository;
    private final FeedbackMapper feedbackMapper;
    private final AIService aiService;

    @Transactional
    public FeedbackDto createFeedback(CreateFeedbackRequest request, UserPrincipal currentUser) {
        log.info("Creating feedback for employee {} by user: {}", request.getEmployeeId(), currentUser.getEmail());

        Employee targetEmployee = employeeRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + request.getEmployeeId()));

        Employee author = employeeRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Author not found"));

        // Validate: Employees can only give feedback to coworkers in the same department
        if ("EMPLOYEE".equals(currentUser.getRole().getName())) {
            if (author.getDepartment() == null || targetEmployee.getDepartment() == null) {
                throw new UnauthorizedException("Both author and target employee must be assigned to a department");
            }
            if (!author.getDepartment().getId().equals(targetEmployee.getDepartment().getId())) {
                throw new UnauthorizedException("You can only provide feedback to coworkers in your department");
            }
            if (author.getId().equals(targetEmployee.getId())) {
                throw new UnauthorizedException("You cannot provide feedback to yourself");
            }
            // Employees cannot give feedback to managers
            if ("MANAGER".equals(targetEmployee.getRole().getName())) {
                throw new UnauthorizedException("You cannot provide feedback to managers");
            }
        }

        Feedback feedback = feedbackMapper.toEntity(request);
        feedback.setAuthorId(author.getId());
        feedback.setAuthorName(author.getFullName());

        if (Boolean.TRUE.equals(request.getPolishWithAI())) {
            try {
                String polishedContent = aiService.polishFeedback(request.getContent());
                feedback.setPolishedContent(polishedContent);
                feedback.setIsPolished(true);
                log.info("Feedback polished with AI for feedback on employee {}", request.getEmployeeId());
            } catch (Exception e) {
                log.warn("Failed to polish feedback with AI, using original content", e);
                feedback.setIsPolished(false);
            }
        }

        feedback = feedbackRepository.save(feedback);

        log.info("Feedback created with ID: {}", feedback.getId());
        FeedbackDto dto = feedbackMapper.toDto(feedback);
        dto.setEmployeeName(targetEmployee.getFullName());
        return dto;
    }

    @Transactional(readOnly = true)
    public List<FeedbackDto> getFeedbackForEmployee(Long employeeId, UserPrincipal currentUser) {
        log.info("Fetching feedback for employee {} by user: {}", employeeId, currentUser.getEmail());

        Employee targetEmployee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + employeeId));

        List<Feedback> feedbacks = feedbackRepository.findVisibleFeedbackByEmployeeId(employeeId);

        // Filter feedback based on access control
        // Managers can see all feedback
        if ("MANAGER".equals(currentUser.getRole().getName())) {
            return feedbacks.stream()
                    .map(this::enrichFeedbackDto)
                    .toList();
        }

        // Feedback authors can see their own feedback
        // Target employee cannot see feedback (as per requirements - only manager and feedbacker)
        return feedbacks.stream()
                .filter(f -> f.getAuthorId().equals(currentUser.getId()))
                .map(this::enrichFeedbackDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<FeedbackDto> getMyFeedbacks(UserPrincipal currentUser) {
        log.info("Fetching feedbacks created by user: {}", currentUser.getEmail());

        List<Feedback> feedbacks = feedbackRepository.findByAuthorId(currentUser.getId());
        return feedbacks.stream()
                .map(this::enrichFeedbackDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<FeedbackDto> getAllVisibleFeedbacks(UserPrincipal currentUser) {
        log.info("Fetching all visible feedbacks for user: {}", currentUser.getEmail());

        Employee user = employeeRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Managers can see all feedback for employees in their department
        if ("MANAGER".equals(currentUser.getRole().getName())) {
            if (user.getDepartment() == null) {
                return List.of();
            }

            List<Employee> departmentEmployees = employeeRepository.findByDepartmentId(user.getDepartment().getId());
            List<Long> employeeIds = departmentEmployees.stream().map(Employee::getId).toList();

            return feedbackRepository.findAll().stream()
                    .filter(f -> f.getVisible() && employeeIds.contains(f.getEmployeeId()))
                    .map(this::enrichFeedbackDto)
                    .toList();
        }

        // Employees see only feedback they've given
        return getMyFeedbacks(currentUser);
    }

    @Transactional
    public void deleteFeedback(String id, UserPrincipal currentUser) {
        log.info("Deleting feedback {} by user: {}", id, currentUser.getEmail());

        Feedback feedback = feedbackRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Feedback not found with id: " + id));

        if (!canDeleteFeedback(feedback, currentUser)) {
            throw new UnauthorizedException("You don't have permission to delete this feedback");
        }

        feedbackRepository.deleteById(id);
        log.info("Feedback {} deleted successfully", id);
    }

    @Transactional
    public void hideFeedback(String id, UserPrincipal currentUser) {
        log.info("Hiding feedback {} by user: {}", id, currentUser.getEmail());

        if (!"MANAGER".equals(currentUser.getRole().getName())) {
            throw new UnauthorizedException("Only managers can hide feedback");
        }

        Feedback feedback = feedbackRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Feedback not found with id: " + id));

        feedback.setVisible(false);
        feedbackRepository.save(feedback);

        log.info("Feedback {} hidden successfully", id);
    }

    private boolean canViewFeedback(Long employeeId, UserPrincipal currentUser) {
        if ("MANAGER".equals(currentUser.getRole().getName())) {
            return true;
        }

        return currentUser.getId().equals(employeeId);
    }

    private boolean canDeleteFeedback(Feedback feedback, UserPrincipal currentUser) {
        if ("MANAGER".equals(currentUser.getRole().getName())) {
            return true;
        }

        return feedback.getAuthorId().equals(currentUser.getId());
    }

    private FeedbackDto enrichFeedbackDto(Feedback feedback) {
        FeedbackDto dto = feedbackMapper.toDto(feedback);
        employeeRepository.findById(feedback.getEmployeeId()).ifPresent(employee -> {
            dto.setEmployeeName(employee.getFullName());
        });
        return dto;
    }
}
