package khanhnq.project.clinicbookingmanagementsystem.mapper;

import khanhnq.project.clinicbookingmanagementsystem.entity.LabResult;
import khanhnq.project.clinicbookingmanagementsystem.model.dto.LabResultInfoDTO;
import khanhnq.project.clinicbookingmanagementsystem.model.projection.MedicalRecordDetailsProjection;
import khanhnq.project.clinicbookingmanagementsystem.model.request.LabResultRequest;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

@Mapper
public interface LabResultMapper {
    LabResultMapper LAB_RESULT_MAPPER = Mappers.getMapper(LabResultMapper.class);
    LabResult mapToLabResult(LabResultRequest labResultRequest);
    void mapToLabResultInfoDTO(@MappingTarget LabResultInfoDTO labResultInfoDTO, MedicalRecordDetailsProjection medicalRecordDetailsProjection);
}
