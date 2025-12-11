package com.newwork.employeeprofile.dto.request;

import com.newwork.employeeprofile.domain.AbsenceStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewAbsenceRequest {

    @NotNull(message = "Status is required")
    private AbsenceStatus status;

    @Size(max = 500, message = "Comment must not exceed 500 characters")
    private String reviewComment;
}
