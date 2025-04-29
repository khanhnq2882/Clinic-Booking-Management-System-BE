package khanhnq.project.clinicbookingmanagementsystem.mapper;

import khanhnq.project.clinicbookingmanagementsystem.entity.District;
import khanhnq.project.clinicbookingmanagementsystem.model.response.DistrictResponse;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface DistrictMapper {
    DistrictMapper DISTRICT_MAPPER= Mappers.getMapper(DistrictMapper.class);
    DistrictResponse mapToDistrictResponse (District district);
}
