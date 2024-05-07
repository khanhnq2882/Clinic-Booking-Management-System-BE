package khanhnq.project.clinicbookingmanagementsystem.mapper;

import khanhnq.project.clinicbookingmanagementsystem.entity.Ward;
import khanhnq.project.clinicbookingmanagementsystem.response.WardResponse;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface WardMapper {
    WardMapper WARD_MAPPER= Mappers.getMapper(WardMapper.class);
    WardResponse mapToWardResponse (Ward ward);
}
