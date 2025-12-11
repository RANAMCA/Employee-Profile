package com.newwork.employeeprofile.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RoleDto {

    @JsonView(EmployeeDto.Views.Public.class)
    private Long id;

    @JsonView(EmployeeDto.Views.Public.class)
    private String name;

    @JsonView(EmployeeDto.Views.Extended.class)
    private String description;

    @JsonView(EmployeeDto.Views.Extended.class)
    private Set<PermissionDto> permissions;

    @JsonView(EmployeeDto.Views.Extended.class)
    private LocalDateTime createdAt;

    @JsonView(EmployeeDto.Views.Extended.class)
    private LocalDateTime updatedAt;
}
