package com.newwork.employeeprofile.mapper;

import com.newwork.employeeprofile.domain.Permission;
import com.newwork.employeeprofile.dto.response.PermissionDto;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PermissionMapper {

    PermissionDto toDto(Permission permission);
}
