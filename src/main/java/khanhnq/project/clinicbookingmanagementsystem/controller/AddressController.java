package khanhnq.project.clinicbookingmanagementsystem.controller;

import khanhnq.project.clinicbookingmanagementsystem.dto.CityDTO;
import khanhnq.project.clinicbookingmanagementsystem.response.CityResponse;
import khanhnq.project.clinicbookingmanagementsystem.response.DistrictResponse;
import khanhnq.project.clinicbookingmanagementsystem.response.WardResponse;
import khanhnq.project.clinicbookingmanagementsystem.service.AddressService;
import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@CrossOrigin(origins = "http://localhost:4200", maxAge = 3600, allowCredentials = "true")
@RestController
@AllArgsConstructor
@RequestMapping("/address")
public class AddressController {
    private final AddressService addressService;

    @GetMapping("/insert-data")
    public List<CityDTO> insertData() {
        return addressService.insertData();
    }

    @GetMapping( "/cities")
    public ResponseEntity<List<CityResponse>> getCities()
    {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(addressService.getCities());
    }

    @GetMapping( "/districts/{cityId}")
    public ResponseEntity<List<DistrictResponse>> getDistricts(@PathVariable("cityId") Long cityId)
    {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(addressService.getDistrictsById(cityId));
    }

    @GetMapping( "/wards/{districtId}")
    public ResponseEntity<List<WardResponse>> getWards(@PathVariable("districtId") Long districtId)
    {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(addressService.getWardsById(districtId));
    }


}