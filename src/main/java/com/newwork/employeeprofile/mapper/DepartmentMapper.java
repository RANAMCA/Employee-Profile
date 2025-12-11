package com.newwork.employeeprofile.mapper;

import com.newwork.employeeprofile.domain.Department;
import com.newwork.employeeprofile.dto.request.CreateDepartmentRequest;
import com.newwork.employeeprofile.dto.request.UpdateDepartmentRequest;
import com.newwork.employeeprofile.dto.response.DepartmentDto;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface DepartmentMapper {

    /**
     * Lightweight mapping for basic department info (default) - use when embedded in other entities like Employee
     * Avoids accessing lazy-loaded relationships to prevent uninitialized proxy issues
     */
    @Mapping(target = "managerId", ignore = true)
    @Mapping(target = "managerName", ignore = true)
    @Mapping(target = "parentDepartmentId", ignore = true)
    @Mapping(target = "parentDepartmentName", ignore = true)
    @Mapping(target = "subDepartments", ignore = true)
    @Mapping(target = "employeeCount", ignore = true)
    DepartmentDto toDto(Department department);

    /**
     * Full mapping with all relationships - use when department is explicitly loaded with all relationships
     */
    @Named("toDtoWithRelationships")
    @Mapping(target = "managerId", source = "manager.id")
    @Mapping(target = "managerName", expression = "java(getManagerName(department))")
    @Mapping(target = "parentDepartmentId", source = "parentDepartment.id")
    @Mapping(target = "parentDepartmentName", source = "parentDepartment.name")
    @Mapping(target = "employeeCount", expression = "java(getEmployeeCount(department))")
    DepartmentDto toDtoWithRelationships(Department department);

    default String getManagerName(Department department) {
        if (department == null || department.getManager() == null) {
            return null;
        }
        return department.getManager().getFirstName() + " " + department.getManager().getLastName();
    }

    default int getEmployeeCount(Department department) {
        if (department == null || department.getEmployees() == null) {
            return 0;
        }
        return department.getEmployees().size();
    }

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "manager", ignore = true)
    @Mapping(target = "parentDepartment", ignore = true)
    @Mapping(target = "subDepartments", ignore = true)
    @Mapping(target = "employees", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "active", constant = "true")
    Department toEntity(CreateDepartmentRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "manager", ignore = true)
    @Mapping(target = "parentDepartment", ignore = true)
    @Mapping(target = "subDepartments", ignore = true)
    @Mapping(target = "employees", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromDto(UpdateDepartmentRequest dto, @MappingTarget Department entity);
}
