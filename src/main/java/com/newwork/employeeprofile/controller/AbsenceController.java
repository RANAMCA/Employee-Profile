package com.newwork.employeeprofile.controller;

import com.newwork.employeeprofile.dto.request.CreateAbsenceRequest;
import com.newwork.employeeprofile.dto.request.ReviewAbsenceRequest;
import com.newwork.employeeprofile.dto.response.AbsenceDto;
import com.newwork.employeeprofile.security.UserPrincipal;
import com.newwork.employeeprofile.service.AbsenceService;
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
@RequestMapping("/api/absences")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearer-jwt")
@Tag(name = "Absences", description = "Absence request management endpoints")
public class AbsenceController {

    private final AbsenceService absenceService;

    @PostMapping
    @Operation(summary = "Create absence request", description = "Creates a new absence request")
    public ResponseEntity<AbsenceDto> createAbsence(
            @Valid @RequestBody CreateAbsenceRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.status(HttpStatus.CREATED).body(absenceService.createAbsence(request, currentUser));
    }

    @GetMapping("/my")
    @Operation(summary = "Get my absences", description = "Retrieves all absence requests for the current user")
    public ResponseEntity<List<AbsenceDto>> getMyAbsences(@AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(absenceService.getMyAbsences(currentUser));
    }

    @GetMapping
    @PreAuthorize("hasRole('MANAGER')")
    @Operation(summary = "Get all absences", description = "Retrieves all absence requests (Manager only)")
    public ResponseEntity<List<AbsenceDto>> getAllAbsences(@AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(absenceService.getAllAbsences(currentUser));
    }

    @GetMapping("/pending")
    @PreAuthorize("hasRole('MANAGER')")
    @Operation(summary = "Get pending absences", description = "Retrieves all pending absence requests (Manager only)")
    public ResponseEntity<List<AbsenceDto>> getPendingAbsences(@AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(absenceService.getPendingAbsences(currentUser));
    }

    @PatchMapping("/{id}/review")
    @PreAuthorize("hasRole('MANAGER')")
    @Operation(summary = "Review absence request", description = "Approves or rejects an absence request (Manager only)")
    public ResponseEntity<AbsenceDto> reviewAbsence(
            @PathVariable Long id,
            @Valid @RequestBody ReviewAbsenceRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(absenceService.reviewAbsence(id, request, currentUser));
    }

    @PatchMapping("/{id}/cancel")
    @Operation(summary = "Cancel absence request", description = "Cancels own absence request")
    public ResponseEntity<Void> cancelAbsence(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        absenceService.cancelAbsence(id, currentUser);
        return ResponseEntity.noContent().build();
    }
}
