package khanhnq.project.clinicbookingmanagementsystem.mapper;

import khanhnq.project.clinicbookingmanagementsystem.entity.City;
import khanhnq.project.clinicbookingmanagementsystem.response.CityResponse;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface CityMapper {
    CityMapper CITY_MAPPER= Mappers.getMapper(CityMapper.class);
    CityResponse mapToCityResponse (City city);
}
