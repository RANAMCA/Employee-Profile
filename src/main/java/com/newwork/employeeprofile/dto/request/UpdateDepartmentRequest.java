package com.newwork.employeeprofile.dto.request;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateDepartmentRequest {

    @Size(min = 2, max = 100)
    private String name;

    @Size(max = 500)
    private String description;

    @Size(max = 255)
    private String location;

    private Long managerId;

    private Long parentDepartmentId;

    private Boolean active;
}
