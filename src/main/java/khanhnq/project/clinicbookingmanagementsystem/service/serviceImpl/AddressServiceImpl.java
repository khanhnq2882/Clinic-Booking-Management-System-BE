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
import java.util.Objects;
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
        districtRepository.saveAll(getDistricts(Objects.requireNonNull(cities)));
        wardRepository.saveAll(getWards(getDistricts(cities)));
        return Arrays.asList(Objects.requireNonNull(cities));
    }

    @Override
    public List<CityResponse> getCities() {
        return cityRepository.findAll()
                .stream()
                .map(city -> CityResponse.builder()
                        .cityId(city.getCityId())
                        .cityName(city.getCityName())
                        .build())
                .toList();
    }

    @Override
    public List<DistrictResponse> getDistrictsById(Long cityId) {
        return districtRepository.getDistrictsByCityId(cityId)
                .stream()
                .map(district -> DistrictResponse.builder()
                        .districtId(district.getDistrictId())
                        .districtName(district.getDistrictName())
                        .cityId(cityId)
                        .build())
                .toList();
    }

    @Override
    public List<WardResponse> getWardsById(Long districtId) {
        return wardRepository.getWardsByDistrictId(districtId)
                .stream()
                .map(ward -> WardResponse.builder()
                        .wardId(ward.getWardId())
                        .wardName(ward.getWardName())
                        .districtId(districtId)
                        .build())
                .toList();
    }

    public List<City> getCities(CityDTO[] cities) {
        return Arrays.stream(cities)
                .map(cityDTO -> City.builder()
                        .cityId(cityDTO.getCode())
                        .cityName(cityDTO.getName())
                        .build())
                .toList();
    }

    public List<District> getDistricts(CityDTO[] cities) {
        List<District> districtList = new ArrayList<>();
        for (CityDTO cityDTO : cities) {
            List<District> districts = cityDTO.getDistricts()
                    .stream()
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
                    .toList();
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
        return districtList
                .stream()
                .flatMap(district -> district.getWards().stream()
                        .map(ward -> Ward.builder()
                                .wardId(ward.getWardId())
                                .wardName(ward.getWardName())
                                .district(district)
                                .build()
                        )
                ).toList();
    }


}