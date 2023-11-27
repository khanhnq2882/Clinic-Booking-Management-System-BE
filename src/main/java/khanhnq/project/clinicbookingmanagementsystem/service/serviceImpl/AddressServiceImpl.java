package khanhnq.project.clinicbookingmanagementsystem.service.serviceImpl;

import khanhnq.project.clinicbookingmanagementsystem.dto.CityDTO;
import khanhnq.project.clinicbookingmanagementsystem.entity.City;
import khanhnq.project.clinicbookingmanagementsystem.entity.District;
import khanhnq.project.clinicbookingmanagementsystem.entity.Ward;
import khanhnq.project.clinicbookingmanagementsystem.repository.CityRepository;
import khanhnq.project.clinicbookingmanagementsystem.repository.DistrictRepository;
import khanhnq.project.clinicbookingmanagementsystem.repository.WardRepository;
import khanhnq.project.clinicbookingmanagementsystem.response.CityResponse;
import khanhnq.project.clinicbookingmanagementsystem.response.DistrictResponse;
import khanhnq.project.clinicbookingmanagementsystem.response.WardResponse;
import khanhnq.project.clinicbookingmanagementsystem.service.AddressService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class AddressServiceImpl implements AddressService {

    private final CityRepository cityRepository;
    private final DistrictRepository districtRepository;
    private final WardRepository wardRepository;

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

    @Override
    public List<CityResponse> getCities() {
        List<CityResponse> cities = new ArrayList<>();
        for (City city : getAllCities()) {
            cities.add(CityResponse.builder()
                    .cityId(city.getCityId())
                    .cityName(city.getCityName())
                    .build());
        }
        return cities;
    }

    @Override
    public List<DistrictResponse> getDistrictsById(Long cityId) {
        List<DistrictResponse> districts = new ArrayList<>();
        for (District district : districtRepository.getDistrictsByCityId(cityId)) {
            districts.add(DistrictResponse.builder()
                    .districtId(district.getDistrictId())
                    .districtName(district.getDistrictName())
                    .cityId(cityId)
                    .build());
        }
        return districts;
    }

    @Override
    public List<WardResponse> getWardsById(Long districtId) {
        List<WardResponse> wards = new ArrayList<>();
        for (Ward ward : wardRepository.getWardsByDistrictId(districtId)) {
            wards.add(WardResponse.builder()
                    .wardId(ward.getWardId())
                    .wardName(ward.getWardName())
                    .districtId(districtId)
                    .build());
        }
        return wards;
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