package com.newwork.employeeprofile.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateEmployeeRequest {

    @Size(min = 2, max = 100)
    private String firstName;

    @Size(min = 2, max = 100)
    private String lastName;

    @Email
    private String email;

    private Long departmentId;

    private String position;

    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$")
    private String phone;

    private LocalDate dateOfBirth;

    @Size(max = 1000)
    private String bio;

    private String skills;
}
