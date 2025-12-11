package com.newwork.employeeprofile.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeedbackDto {

    private String id;
    private Long employeeId;
    private String employeeName;  // Added for UI display
    private Long authorId;
    private String authorName;
    private String content;
    private Boolean isPolished;
    private Integer rating;
    private String category;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
