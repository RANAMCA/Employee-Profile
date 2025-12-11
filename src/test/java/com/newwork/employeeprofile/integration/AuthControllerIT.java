package com.newwork.employeeprofile.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.newwork.employeeprofile.dto.request.CreateEmployeeRequest;
import com.newwork.employeeprofile.dto.request.LoginRequest;
import com.newwork.employeeprofile.dto.response.AuthResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
class AuthControllerIT extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldRegisterNewEmployee() throws Exception {
        CreateEmployeeRequest request = CreateEmployeeRequest.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@test.com")
                .password("password123")
                .departmentId(1L) // Engineering department
                .position("Developer")
                .build();

        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.email").value("john.doe@test.com"))
                .andExpect(jsonPath("$.roleName").value("EMPLOYEE"))
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        AuthResponse response = objectMapper.readValue(responseBody, AuthResponse.class);

        assertThat(response.getToken()).isNotEmpty();
        assertThat(response.getRefreshToken()).isNotEmpty();
        assertThat(response.getEmployeeId()).isNotNull();
    }

    @Test
    void shouldLoginWithValidCredentials() throws Exception {
        // First register
        CreateEmployeeRequest registerRequest = CreateEmployeeRequest.builder()
                .firstName("Jane")
                .lastName("Smith")
                .email("jane.smith@test.com")
                .password("password123")
                .departmentId(2L) // HR department
                .build();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk());

        // Then login
        LoginRequest loginRequest = LoginRequest.builder()
                .email("jane.smith@test.com")
                .password("password123")
                .build();

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.email").value("jane.smith@test.com"))
                .andExpect(jsonPath("$.roleName").value("EMPLOYEE"));
    }

    @Test
    void shouldRejectLoginWithInvalidCredentials() throws Exception {
        LoginRequest loginRequest = LoginRequest.builder()
                .email("nonexistent@test.com")
                .password("wrongpassword")
                .build();

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRejectRegistrationWithDuplicateEmail() throws Exception {
        CreateEmployeeRequest request = CreateEmployeeRequest.builder()
                .firstName("Bob")
                .lastName("Wilson")
                .email("duplicate@test.com")
                .password("password123")
                .departmentId(1L) // Engineering department
                .build();

        // First registration should succeed
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        // Second registration with same email should fail
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
