package khanhnq.project.clinicbookingmanagementsystem.mapper;

import khanhnq.project.clinicbookingmanagementsystem.model.dto.DoctorDTO;
import khanhnq.project.clinicbookingmanagementsystem.model.dto.DoctorDetailsDTO;
import khanhnq.project.clinicbookingmanagementsystem.model.dto.DoctorInfoDTO;
import khanhnq.project.clinicbookingmanagementsystem.model.projection.DoctorDetailsInfoProjection;
import khanhnq.project.clinicbookingmanagementsystem.model.projection.DoctorInfoProjection;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

@Mapper
public interface DoctorMapper {
    DoctorMapper DOCTOR_MAPPER= Mappers.getMapper(DoctorMapper.class);
    DoctorInfoDTO mapToDoctorInfo(DoctorInfoProjection doctorInfoProjection);
    void mapToDoctorDetailsDTO(@MappingTarget DoctorDetailsDTO doctorDetailsDTO, DoctorDetailsInfoProjection doctorDetailsInfoProjection);
    void mapToDoctorDTO (@MappingTarget DoctorDTO doctorDTO, DoctorDetailsInfoProjection doctorDetailsInfoProjection);
}
