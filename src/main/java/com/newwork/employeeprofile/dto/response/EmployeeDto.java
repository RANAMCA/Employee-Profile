package com.newwork.employeeprofile.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonView;
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
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EmployeeDto {

    public interface Views {
        interface Public {}
        interface Extended extends Public {} // description, location, managerId, managerName, etc.
        interface Full extends Extended {}
    }

    @JsonView(Views.Public.class)
    private Long id;

    @JsonView(Views.Public.class)
    private String firstName;

    @JsonView(Views.Public.class)
    private String lastName;

    @JsonView(Views.Public.class)
    private String email;

    @JsonView(Views.Public.class)
    private RoleDto role;

    @JsonView(Views.Public.class)
    private DepartmentDto department;

    @JsonView(Views.Public.class)
    private String position;

    @JsonView(Views.Extended.class)
    private String phone;

    @JsonView(Views.Extended.class)
    private LocalDate dateOfBirth;

    @JsonView(Views.Extended.class)
    private LocalDate hireDate;

    @JsonView(Views.Extended.class)
    private String bio;

    @JsonView(Views.Extended.class)
    private String skills;

    @JsonView(Views.Full.class)
    private Boolean active;

    @JsonView(Views.Extended.class)
    private LocalDateTime createdAt;

    @JsonView(Views.Extended.class)
    private LocalDateTime updatedAt;
}
