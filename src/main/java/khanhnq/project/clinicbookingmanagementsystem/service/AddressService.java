package khanhnq.project.clinicbookingmanagementsystem.service;

import khanhnq.project.clinicbookingmanagementsystem.dto.CityDTO;
import khanhnq.project.clinicbookingmanagementsystem.entity.City;
import khanhnq.project.clinicbookingmanagementsystem.entity.Ward;
import khanhnq.project.clinicbookingmanagementsystem.response.CityResponse;
import khanhnq.project.clinicbookingmanagementsystem.response.DistrictResponse;
import khanhnq.project.clinicbookingmanagementsystem.response.WardResponse;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface AddressService {
    List<CityDTO> insertData();
    List<City> getAllCities();
    ResponseEntity<List<CityResponse>> getCities();
    ResponseEntity<List<DistrictResponse>> getDistrictsById(Long cityId);
    ResponseEntity<List<WardResponse>> getWardsById(Long districtId);
}