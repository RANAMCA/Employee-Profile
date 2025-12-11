package com.newwork.employeeprofile.domain;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "feedbacks")
@CompoundIndex(name = "employee_author_idx", def = "{'employeeId': 1, 'authorId': 1}")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Feedback {

    @Id
    private String id;

    @Indexed
    private Long employeeId;

    @Indexed
    private Long authorId;

    private String authorName;

    private String originalContent;

    private String polishedContent;

    private Boolean isPolished = false;

    private Integer rating;

    private String category;

    @Indexed
    private Boolean visible = true;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    public String getDisplayContent() {
        return isPolished && polishedContent != null ? polishedContent : originalContent;
    }
}
