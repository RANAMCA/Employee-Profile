package com.newwork.employeeprofile.controller;

import com.newwork.employeeprofile.dto.request.CreateFeedbackRequest;
import com.newwork.employeeprofile.dto.response.FeedbackDto;
import com.newwork.employeeprofile.security.UserPrincipal;
import com.newwork.employeeprofile.service.FeedbackService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/feedbacks")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearer-jwt")
@Tag(name = "Feedbacks", description = "Employee feedback management endpoints")
public class FeedbackController {

    private final FeedbackService feedbackService;

    @PostMapping
    @Operation(summary = "Create feedback", description = "Creates feedback for an employee with optional AI enhancement")
    public ResponseEntity<FeedbackDto> createFeedback(
            @Valid @RequestBody CreateFeedbackRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.status(HttpStatus.CREATED).body(feedbackService.createFeedback(request, currentUser));
    }

    @GetMapping("/employee/{employeeId}")
    @Operation(summary = "Get feedback for employee", description = "Retrieves all visible feedback for an employee")
    public ResponseEntity<List<FeedbackDto>> getFeedbackForEmployee(
            @PathVariable Long employeeId,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(feedbackService.getFeedbackForEmployee(employeeId, currentUser));
    }

    @GetMapping("/my")
    @Operation(summary = "Get my feedbacks", description = "Retrieves all feedbacks created by the current user")
    public ResponseEntity<List<FeedbackDto>> getMyFeedbacks(@AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(feedbackService.getMyFeedbacks(currentUser));
    }

    @GetMapping("/visible")
    @Operation(summary = "Get all visible feedbacks", description = "Managers see all feedback in their department, employees see feedback they gave")
    public ResponseEntity<List<FeedbackDto>> getAllVisibleFeedbacks(@AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(feedbackService.getAllVisibleFeedbacks(currentUser));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete feedback", description = "Deletes feedback (Author or Manager)")
    public ResponseEntity<Void> deleteFeedback(
            @PathVariable String id,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        feedbackService.deleteFeedback(id, currentUser);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/hide")
    @PreAuthorize("hasRole('MANAGER')")
    @Operation(summary = "Hide feedback", description = "Hides feedback from public view (Manager only)")
    public ResponseEntity<Void> hideFeedback(
            @PathVariable String id,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        feedbackService.hideFeedback(id, currentUser);
        return ResponseEntity.noContent().build();
    }
}
