package com.newwork.employeeprofile.mapper;

import com.newwork.employeeprofile.domain.Role;
import com.newwork.employeeprofile.dto.response.RoleDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = {PermissionMapper.class})
public interface RoleMapper {

    /**
     * Lightweight mapping for basic role info (default) - use when embedded in other entities like Employee
     * Avoids accessing lazy-loaded permissions to prevent uninitialized proxy issues
     */
    @Mapping(target = "permissions", ignore = true)
    RoleDto toDto(Role role);

    /**
     * Full mapping with permissions - use when role is explicitly loaded with all permissions
     */
    @Named("toDtoWithPermissions")
    RoleDto toDtoWithPermissions(Role role);
}
