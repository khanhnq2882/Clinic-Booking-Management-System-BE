package khanhnq.project.clinicbookingmanagementsystem.controller;

import khanhnq.project.clinicbookingmanagementsystem.dto.CityDTO;
import khanhnq.project.clinicbookingmanagementsystem.entity.City;
import khanhnq.project.clinicbookingmanagementsystem.repository.CityRepository;
import khanhnq.project.clinicbookingmanagementsystem.response.CityResponse;
import khanhnq.project.clinicbookingmanagementsystem.response.DistrictResponse;
import khanhnq.project.clinicbookingmanagementsystem.response.WardResponse;
import khanhnq.project.clinicbookingmanagementsystem.service.AddressService;
import khanhnq.project.clinicbookingmanagementsystem.service.serviceImpl.AddressServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.web.servlet.function.RequestPredicates.contentType;

@CrossOrigin(origins = "http://localhost:4200", maxAge = 3600, allowCredentials = "true")
@RestController
@RequestMapping("/address")
public class AddressController {
    private AddressService addressService;

    @Autowired
    public AddressController(AddressService addressService) {
        this.addressService = addressService;
    }

    @GetMapping("/insert-data")
    public List<CityDTO> insertData() {
        return addressService.insertData();
    }

    @GetMapping( "/cities")
    public ResponseEntity<List<CityResponse>> getCities()
    {
        return addressService.getCities();
    }

    @GetMapping( "/districts/{cityId}")
    public ResponseEntity<List<DistrictResponse>> getDistricts(@PathVariable("cityId") Long cityId)
    {
        return addressService.getDistrictsById(cityId);
    }

    @GetMapping( "/wards/{districtId}")
    public ResponseEntity<List<WardResponse>> getWards(@PathVariable("districtId") Long districtId)
    {
        return addressService.getWardsById(districtId);
    }


}