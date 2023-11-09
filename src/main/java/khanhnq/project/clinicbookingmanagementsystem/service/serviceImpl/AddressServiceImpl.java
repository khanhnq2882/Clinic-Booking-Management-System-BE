package khanhnq.project.clinicbookingmanagementsystem.service.serviceImpl;

import khanhnq.project.clinicbookingmanagementsystem.dto.CityDTO;
import khanhnq.project.clinicbookingmanagementsystem.entity.City;
import khanhnq.project.clinicbookingmanagementsystem.entity.District;
import khanhnq.project.clinicbookingmanagementsystem.entity.Ward;
import khanhnq.project.clinicbookingmanagementsystem.repository.CityRepository;
import khanhnq.project.clinicbookingmanagementsystem.repository.DistrictRepository;
import khanhnq.project.clinicbookingmanagementsystem.repository.WardRepository;
import khanhnq.project.clinicbookingmanagementsystem.service.AddressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AddressServiceImpl implements AddressService {

    private CityRepository cityRepository;
    private DistrictRepository districtRepository;
    private WardRepository wardRepository;
    private RestTemplate restTemplate;

    @Autowired
    public AddressServiceImpl(CityRepository cityRepository, DistrictRepository districtRepository, WardRepository wardRepository, RestTemplate restTemplate) {
        this.cityRepository = cityRepository;
        this.districtRepository = districtRepository;
        this.wardRepository = wardRepository;
        this.restTemplate = restTemplate;
    }

    @Override
    public List<CityDTO> insertData() {
        String externalApi = "https://provinces.open-api.vn/api/?depth=3";
        RestTemplate restTemplate = new RestTemplate();
        CityDTO[] cities = restTemplate.getForObject(externalApi, CityDTO[].class);
        cityRepository.saveAll(getCities(cities));
        districtRepository.saveAll(getDistricts(cities));
        wardRepository.saveAll(getWards(getDistricts(cities)));
        return Arrays.asList(cities);
    }

    @Override
    public List<City> getAllCities() {
        return cityRepository.findAll();
    }

    public List<City> listCities() {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        HttpEntity<String> entity = new HttpEntity<String>(headers);
        ResponseEntity<List<City>> response = restTemplate.exchange(
                "http://localhost:8080/address/cities", HttpMethod.GET,entity,
                new ParameterizedTypeReference<List<City>>() {});
        return response.getBody();
    }

    public List<City> getCities(CityDTO[] cities) {
        return Arrays.asList(cities).stream()
                .map(cityDTO -> City.builder()
                        .cityId(cityDTO.getCode())
                        .cityName(cityDTO.getName())
                        .build())
                .collect(Collectors.toList());
    }

    public List<District> getDistricts(CityDTO[] cities) {
        List<District> districtList = new ArrayList<>();
        for (CityDTO cityDTO : Arrays.asList(cities)) {
            List<District> districts = cityDTO.getDistricts().stream()
                    .map(districtDTO -> District.builder()
                            .districtId(districtDTO.getCode())
                            .districtName(districtDTO.getName())
                            .wards(districtDTO.getWards().stream()
                                    .map(wardDTO -> Ward.builder()
                                            .wardId(wardDTO.getCode())
                                            .wardName(wardDTO.getName())
                                            .build())
                                    .collect(Collectors.toList()))
                            .build())
                    .collect(Collectors.toList());
            for (District district : districts) {
                districtList.add(District.builder()
                        .districtId(district.getDistrictId())
                        .districtName(district.getDistrictName())
                        .wards(district.getWards())
                        .city(cityRepository.findById(cityDTO.getCode()).orElse(null))
                        .build());
            }
        }
        return districtList;
    }

    public List<Ward> getWards(List<District> districtList) {
        List<Ward> wardList = new ArrayList<>();
        for (District district : districtList) {
            for (Ward ward : district.getWards()) {
                wardList.add(Ward.builder()
                        .wardId(ward.getWardId())
                        .wardName(ward.getWardName())
                        .district(district)
                        .build());
            }
        }
        return wardList;
    }


}