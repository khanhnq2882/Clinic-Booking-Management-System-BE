package khanhnq.project.clinicbookingmanagementsystem.service.serviceImpl;

import khanhnq.project.clinicbookingmanagementsystem.mapper.CityMapper;
import khanhnq.project.clinicbookingmanagementsystem.mapper.DistrictMapper;
import khanhnq.project.clinicbookingmanagementsystem.mapper.WardMapper;
import khanhnq.project.clinicbookingmanagementsystem.repository.CityRepository;
import khanhnq.project.clinicbookingmanagementsystem.repository.DistrictRepository;
import khanhnq.project.clinicbookingmanagementsystem.repository.WardRepository;
import khanhnq.project.clinicbookingmanagementsystem.response.CityResponse;
import khanhnq.project.clinicbookingmanagementsystem.response.DistrictResponse;
import khanhnq.project.clinicbookingmanagementsystem.response.WardResponse;
import khanhnq.project.clinicbookingmanagementsystem.service.AddressService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
public class AddressServiceImpl implements AddressService {

    private final CityRepository cityRepository;
    private final DistrictRepository districtRepository;
    private final WardRepository wardRepository;

    @Override
    public List<CityResponse> getCities() {
        List<CityResponse> cities = new ArrayList<>();
        cityRepository.findAll().forEach(city -> {
            cities.add(CityMapper.CITY_MAPPER.mapToCityResponse(city));
        });
        return cities;
    }

    @Override
    public List<DistrictResponse> getDistricts(Long cityId) {
        List<DistrictResponse> districts = new ArrayList<>();
        districtRepository.getDistrictsByCityId(cityId).forEach(district -> {
            DistrictResponse districtResponse = DistrictMapper.DISTRICT_MAPPER.mapToDistrictResponse(district);
            districtResponse.setCityId(cityId);
            districts.add(districtResponse);
        });
        return districts;
    }

    @Override
    public List<WardResponse> getWards(Long districtId) {
        List<WardResponse> wards = new ArrayList<>();
        wardRepository.getWardsByDistrictId(districtId).forEach(ward -> {
            WardResponse wardResponse = WardMapper.WARD_MAPPER.mapToWardResponse(ward);
            wardResponse.setDistrictId(districtId);
            wards.add(wardResponse);
        });
        return wards;
    }

}