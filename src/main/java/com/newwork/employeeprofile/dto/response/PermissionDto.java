package com.newwork.employeeprofile.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonView;
import com.newwork.employeeprofile.domain.PermissionAction;
import com.newwork.employeeprofile.domain.PermissionResource;
import com.newwork.employeeprofile.domain.PermissionScope;
import com.newwork.employeeprofile.security.JsonViewSecurity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PermissionDto {

    @JsonView(JsonViewSecurity.PublicView.class)
    private Long id;

    @JsonView(JsonViewSecurity.PublicView.class)
    private String name;

    @JsonView(JsonViewSecurity.PublicView.class)
    private PermissionResource resource;

    @JsonView(JsonViewSecurity.PublicView.class)
    private PermissionAction action;

    @JsonView(JsonViewSecurity.PublicView.class)
    private PermissionScope scope;

    @JsonView(JsonViewSecurity.PublicView.class)
    private String description;
}
