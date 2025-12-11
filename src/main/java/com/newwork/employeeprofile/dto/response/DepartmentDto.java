package com.newwork.employeeprofile.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DepartmentDto {

    @JsonView(EmployeeDto.Views.Public.class)
    private Long id;

    @JsonView(EmployeeDto.Views.Public.class)
    private String name;

    @JsonView(EmployeeDto.Views.Extended.class)
    private String description;

    @JsonView(EmployeeDto.Views.Extended.class)
    private String location;

    @JsonView(EmployeeDto.Views.Extended.class)
    private Long managerId;

    @JsonView(EmployeeDto.Views.Extended.class)
    private String managerName;

    @JsonView(EmployeeDto.Views.Extended.class)
    private Long parentDepartmentId;

    @JsonView(EmployeeDto.Views.Extended.class)
    private String parentDepartmentName;

    @JsonView(EmployeeDto.Views.Extended.class)
    private List<DepartmentDto> subDepartments;

    @JsonView(EmployeeDto.Views.Extended.class)
    private Integer employeeCount;

    @JsonView(EmployeeDto.Views.Full.class)
    private Boolean active;

    @JsonView(EmployeeDto.Views.Extended.class)
    private LocalDateTime createdAt;

    @JsonView(EmployeeDto.Views.Extended.class)
    private LocalDateTime updatedAt;
}
