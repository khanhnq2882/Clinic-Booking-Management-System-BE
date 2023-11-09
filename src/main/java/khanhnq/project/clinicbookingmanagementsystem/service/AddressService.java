package khanhnq.project.clinicbookingmanagementsystem.service;

import khanhnq.project.clinicbookingmanagementsystem.dto.CityDTO;
import khanhnq.project.clinicbookingmanagementsystem.entity.City;
import java.util.List;

public interface AddressService {
    List<CityDTO> insertData();
    List<City> getAllCities();
}