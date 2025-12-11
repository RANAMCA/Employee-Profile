package com.newwork.employeeprofile.mapper;

import com.newwork.employeeprofile.domain.Feedback;
import com.newwork.employeeprofile.dto.request.CreateFeedbackRequest;
import com.newwork.employeeprofile.dto.response.FeedbackDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface FeedbackMapper {

    @Mapping(target = "content", expression = "java(feedback.getDisplayContent())")
    FeedbackDto toDto(Feedback feedback);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "employeeId", source = "employeeId")
    @Mapping(target = "originalContent", source = "content")
    @Mapping(target = "polishedContent", ignore = true)
    @Mapping(target = "isPolished", constant = "false")
    @Mapping(target = "visible", constant = "true")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "authorId", ignore = true)
    @Mapping(target = "authorName", ignore = true)
    Feedback toEntity(CreateFeedbackRequest request);
}
