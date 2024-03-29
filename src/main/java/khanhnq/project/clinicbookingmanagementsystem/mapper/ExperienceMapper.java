package khanhnq.project.clinicbookingmanagementsystem.mapper;

import khanhnq.project.clinicbookingmanagementsystem.dto.ExperienceDTO;
import khanhnq.project.clinicbookingmanagementsystem.entity.Experience;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface ExperienceMapper {
    ExperienceMapper EXPERIENCE_MAPPER= Mappers.getMapper(ExperienceMapper.class);
    Experience mapToExperience (ExperienceDTO experienceDTO);
}
