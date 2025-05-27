package khanhnq.project.clinicbookingmanagementsystem.mapper;

import khanhnq.project.clinicbookingmanagementsystem.model.dto.TestResultDetailsDTO;
import khanhnq.project.clinicbookingmanagementsystem.model.projection.MedicalRecordDetailsProjection;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface TestResultMapper {
    TestResultMapper TEST_RESULT_MAPPER = Mappers.getMapper(TestResultMapper.class);
    TestResultDetailsDTO mapToTestResultDetailsDTO(MedicalRecordDetailsProjection medicalRecordDetailsProjection);
}