package khanhnq.project.clinicbookingmanagementsystem.mapper;

import khanhnq.project.clinicbookingmanagementsystem.entity.TestPackage;
import khanhnq.project.clinicbookingmanagementsystem.model.request.TestPackageRequest;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

@Mapper
public interface TestPackageMapper {
    TestPackageMapper TEST_PACKAGE_MAPPER = Mappers.getMapper(TestPackageMapper.class);
    TestPackage mapToTestPackage(TestPackageRequest testPackageRequest);
    void mapToTestPackage(@MappingTarget TestPackage testPackage, TestPackageRequest testPackageRequest);
}
