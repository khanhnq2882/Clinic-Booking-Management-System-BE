package khanhnq.project.clinicbookingmanagementsystem.mapper;

import khanhnq.project.clinicbookingmanagementsystem.entity.MedicalRecord;
import khanhnq.project.clinicbookingmanagementsystem.model.dto.MedicalRecordInfoDTO;
import khanhnq.project.clinicbookingmanagementsystem.model.projection.MedicalRecordDetailsProjection;
import khanhnq.project.clinicbookingmanagementsystem.model.request.MedicalRecordRequest;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface MedicalRecordMapper {
    MedicalRecordMapper MEDICAL_RECORD_MAPPER = Mappers.getMapper(MedicalRecordMapper.class);
    MedicalRecord mapToMedicalRecord(MedicalRecordRequest medicalRecordRequest);
    MedicalRecordInfoDTO mapToMedicalRecordInfoDTO (MedicalRecordDetailsProjection medicalRecordDetailsProjection);
}
