package com.newwork.employeeprofile.dto.response;

import com.newwork.employeeprofile.domain.AbsenceStatus;
import com.newwork.employeeprofile.domain.AbsenceType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AbsenceDto {

    private Long id;
    private Long employeeId;
    private String employeeName;
    private AbsenceType type;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer durationInDays;
    private String reason;
    private AbsenceStatus status;
    private Long reviewedBy;
    private String reviewerName;
    private LocalDateTime reviewedAt;
    private String reviewComment;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
