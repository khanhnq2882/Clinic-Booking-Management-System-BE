package khanhnq.project.clinicbookingmanagementsystem.controller;

import khanhnq.project.clinicbookingmanagementsystem.dto.CityDTO;
import khanhnq.project.clinicbookingmanagementsystem.entity.City;
import khanhnq.project.clinicbookingmanagementsystem.service.AddressService;
import khanhnq.project.clinicbookingmanagementsystem.service.serviceImpl.AddressServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@CrossOrigin(origins = "http://localhost:4200", maxAge = 3600, allowCredentials = "true")
@RestController
@RequestMapping("/address")
public class AddressController {
    private AddressService addressService;

    private AddressServiceImpl addressServiceImpl;
    @Autowired
    public AddressController(AddressService addressService, AddressServiceImpl addressServiceImpl) {
        this.addressService = addressService;
        this.addressServiceImpl = addressServiceImpl;
    }
    @GetMapping("/insert-data")
    public List<CityDTO> insertData() {
        return addressService.insertData();
    }
    @GetMapping("/cities")
    public List<City> getAllCities() {
        return addressService.getAllCities();
    }

    @GetMapping( "/listCities")
    public List<City> getCities()
    {
        return addressServiceImpl.listCities();
    }




}