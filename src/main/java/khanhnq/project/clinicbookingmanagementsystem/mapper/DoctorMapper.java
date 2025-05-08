package khanhnq.project.clinicbookingmanagementsystem.mapper;

import khanhnq.project.clinicbookingmanagementsystem.model.dto.DoctorInfoDTO;
import khanhnq.project.clinicbookingmanagementsystem.model.dto.projection.DoctorInfoProjection;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface DoctorMapper {
    DoctorMapper DOCTOR_MAPPER= Mappers.getMapper(DoctorMapper.class);
    DoctorInfoDTO mapToDoctorInfo(DoctorInfoProjection doctorInfoProjection);
}
