package khanhnq.project.clinicbookingmanagementsystem.controller;

import khanhnq.project.clinicbookingmanagementsystem.model.response.ResponseEntityBase;
import khanhnq.project.clinicbookingmanagementsystem.service.AddressService;
import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping("/address")
public class AddressController {
    private final AddressService addressService;

    @GetMapping( "/cities")
    public ResponseEntity<ResponseEntityBase> getCities()
    {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(addressService.getCities());
    }

    @GetMapping( "/districts/{cityId}")
    public ResponseEntity<ResponseEntityBase> getDistricts(@PathVariable("cityId") Long cityId)
    {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(addressService.getDistricts(cityId));
    }

    @GetMapping( "/wards/{districtId}")
    public ResponseEntity<ResponseEntityBase> getWards(@PathVariable("districtId") Long districtId)
    {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(addressService.getWards(districtId));
    }

}
