package com.newwork.employeeprofile.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.newwork.employeeprofile.dto.request.CreateEmployeeRequest;
import com.newwork.employeeprofile.dto.request.PromoteToManagerRequest;
import com.newwork.employeeprofile.dto.request.UpdateEmployeeRequest;
import com.newwork.employeeprofile.dto.response.AuthResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
class EmployeeControllerIT extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String managerToken;
    private String employeeToken;
    private Long employeeId;

    @BeforeEach
    void setup() throws Exception {
        // Create manager user (initially as EMPLOYEE, then promote)
        CreateEmployeeRequest managerRequest = CreateEmployeeRequest.builder()
                .firstName("Manager")
                .lastName("Test")
                .email("manager.test@test.com")
                .password("password123")
                .departmentId(2L) // HR department
                .build();

        MvcResult managerResult = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(managerRequest)))
                .andExpect(status().isOk())
                .andReturn();

        AuthResponse managerAuth = objectMapper.readValue(
                managerResult.getResponse().getContentAsString(),
                AuthResponse.class
        );
        Long managerId = managerAuth.getEmployeeId();

        // Promote to manager
        PromoteToManagerRequest promoteRequest = PromoteToManagerRequest.builder()
                .employeeId(managerId)
                .departmentId(2L) // HR department
                .build();

        mockMvc.perform(post("/api/employees/promote")
                        .header("Authorization", "Bearer " + managerAuth.getToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(promoteRequest)))
                .andExpect(status().isOk());

        // Login again to get updated token with manager permissions
        MvcResult managerLoginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"manager.test@test.com\",\"password\":\"password123\"}"))
                .andExpect(status().isOk())
                .andReturn();

        AuthResponse managerLoginAuth = objectMapper.readValue(
                managerLoginResult.getResponse().getContentAsString(),
                AuthResponse.class
        );
        managerToken = managerLoginAuth.getToken();

        // Create employee
        CreateEmployeeRequest employeeRequest = CreateEmployeeRequest.builder()
                .firstName("Employee")
                .lastName("Test")
                .email("employee.test@test.com")
                .password("password123")
                .departmentId(1L) // Engineering department
                .build();

        MvcResult employeeResult = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(employeeRequest)))
                .andExpect(status().isOk())
                .andReturn();

        AuthResponse employeeAuth = objectMapper.readValue(
                employeeResult.getResponse().getContentAsString(),
                AuthResponse.class
        );
        employeeToken = employeeAuth.getToken();
        employeeId = employeeAuth.getEmployeeId();
    }

    @Test
    void managerShouldGetAllEmployees() throws Exception {
        mockMvc.perform(get("/api/employees")
                        .header("Authorization", "Bearer " + managerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void employeeShouldNotGetAllEmployees() throws Exception {
        mockMvc.perform(get("/api/employees")
                        .header("Authorization", "Bearer " + employeeToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void employeeShouldGetOwnProfile() throws Exception {
        mockMvc.perform(get("/api/employees/" + employeeId)
                        .header("Authorization", "Bearer " + employeeToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("employee.test@test.com"));
    }

    @Test
    void employeeShouldUpdateOwnProfile() throws Exception {
        UpdateEmployeeRequest updateRequest = UpdateEmployeeRequest.builder()
                .bio("Updated bio")
                .skills("Java, Spring Boot")
                .build();

        mockMvc.perform(put("/api/employees/" + employeeId)
                        .header("Authorization", "Bearer " + employeeToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bio").value("Updated bio"));
    }

    @Test
    void managerShouldSearchEmployees() throws Exception {
        mockMvc.perform(get("/api/employees/search")
                        .param("keyword", "Test")
                        .header("Authorization", "Bearer " + managerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void unauthenticatedRequestShouldFail() throws Exception {
        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isUnauthorized());
    }
}
