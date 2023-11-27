package khanhnq.project.clinicbookingmanagementsystem.service;

import khanhnq.project.clinicbookingmanagementsystem.dto.CityDTO;
import khanhnq.project.clinicbookingmanagementsystem.entity.City;
import khanhnq.project.clinicbookingmanagementsystem.response.CityResponse;
import khanhnq.project.clinicbookingmanagementsystem.response.DistrictResponse;
import khanhnq.project.clinicbookingmanagementsystem.response.WardResponse;

import java.util.List;

public interface AddressService {
    List<CityDTO> insertData();
    List<City> getAllCities();
    List<CityResponse> getCities();
    List<DistrictResponse> getDistrictsById(Long cityId);
    List<WardResponse> getWardsById(Long districtId);
}