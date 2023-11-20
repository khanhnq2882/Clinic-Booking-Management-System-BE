package khanhnq.project.clinicbookingmanagementsystem.mapper;

import khanhnq.project.clinicbookingmanagementsystem.entity.Experience;
import khanhnq.project.clinicbookingmanagementsystem.request.ExperienceRequest;
import khanhnq.project.clinicbookingmanagementsystem.response.RequestDoctorResponse;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface ExperienceMapper {
    ExperienceMapper EXPERIENCE_MAPPER = Mappers.getMapper(ExperienceMapper.class);
    Experience mapToExperience(ExperienceRequest experienceRequest);
    RequestDoctorResponse mapToRequestDoctorResponse(Experience experience);
}
