package com.newwork.employeeprofile.mapper;

import com.newwork.employeeprofile.domain.Employee;
import com.newwork.employeeprofile.domain.Role;
import com.newwork.employeeprofile.dto.request.CreateEmployeeRequest;
import com.newwork.employeeprofile.dto.request.UpdateEmployeeRequest;
import com.newwork.employeeprofile.dto.response.EmployeeDto;
import com.newwork.employeeprofile.dto.response.RoleDto;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = {RoleMapper.class, DepartmentMapper.class})
public interface EmployeeMapper {

    /**
     * Map Employee to EmployeeDto
     * Uses lightweight mappers for nested role and department (without lazy-loaded collections)
     */
    EmployeeDto toDto(Employee employee);


    @Mapping(target = "id", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "department", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "active", constant = "true")
    Employee toEntity(CreateEmployeeRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "department", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    void updateEntityFromDto(UpdateEmployeeRequest dto, @MappingTarget Employee entity);
}
