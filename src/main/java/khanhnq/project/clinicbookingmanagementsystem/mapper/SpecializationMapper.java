package khanhnq.project.clinicbookingmanagementsystem.mapper;

import khanhnq.project.clinicbookingmanagementsystem.dto.SpecializationDTO;
import khanhnq.project.clinicbookingmanagementsystem.entity.Specialization;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface SpecializationMapper {
    SpecializationMapper SPECIALIZATION_MAPPER = Mappers.getMapper(SpecializationMapper.class);
    SpecializationDTO mapToSpecializationDTO(Specialization specialization);
}
