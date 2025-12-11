package com.newwork.employeeprofile.mapper;

import com.newwork.employeeprofile.domain.Absence;
import com.newwork.employeeprofile.dto.request.CreateAbsenceRequest;
import com.newwork.employeeprofile.dto.response.AbsenceDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AbsenceMapper {

    @Mapping(target = "employeeId", source = "employee.id")
    @Mapping(target = "employeeName", expression = "java(absence.getEmployee().getFullName())")
    @Mapping(target = "reviewerName", expression = "java(getReviewerName(absence))")
    @Mapping(target = "durationInDays", expression = "java(absence.getDurationInDays())")
    AbsenceDto toDto(Absence absence);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "employee", ignore = true)
    @Mapping(target = "status", constant = "PENDING")
    @Mapping(target = "reviewedBy", ignore = true)
    @Mapping(target = "reviewedAt", ignore = true)
    @Mapping(target = "reviewComment", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    Absence toEntity(CreateAbsenceRequest request);

    default String getReviewerName(Absence absence) {
        return null;
    }
}
