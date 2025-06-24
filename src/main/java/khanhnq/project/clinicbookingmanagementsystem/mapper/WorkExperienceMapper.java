package khanhnq.project.clinicbookingmanagementsystem.mapper;

import khanhnq.project.clinicbookingmanagementsystem.model.dto.WorkExperienceDTO;
import khanhnq.project.clinicbookingmanagementsystem.entity.WorkExperience;
import khanhnq.project.clinicbookingmanagementsystem.model.projection.DoctorDetailsInfoProjection;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface WorkExperienceMapper {
    WorkExperienceMapper WORK_EXPERIENCE_MAPPER = Mappers.getMapper(WorkExperienceMapper.class);
    WorkExperience mapToExperience (WorkExperienceDTO workExperienceDTO);
    WorkExperienceDTO mapToWorkExperienceDTO (DoctorDetailsInfoProjection doctorDetailsInfoProjection);
}
