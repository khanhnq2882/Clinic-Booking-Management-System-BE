package khanhnq.project.clinicbookingmanagementsystem.service.serviceImpl;

import khanhnq.project.clinicbookingmanagementsystem.mapper.CityMapper;
import khanhnq.project.clinicbookingmanagementsystem.mapper.DistrictMapper;
import khanhnq.project.clinicbookingmanagementsystem.mapper.WardMapper;
import khanhnq.project.clinicbookingmanagementsystem.model.response.ResponseEntityBase;
import khanhnq.project.clinicbookingmanagementsystem.repository.CityRepository;
import khanhnq.project.clinicbookingmanagementsystem.repository.DistrictRepository;
import khanhnq.project.clinicbookingmanagementsystem.repository.WardRepository;
import khanhnq.project.clinicbookingmanagementsystem.model.response.CityResponse;
import khanhnq.project.clinicbookingmanagementsystem.model.response.DistrictResponse;
import khanhnq.project.clinicbookingmanagementsystem.model.response.WardResponse;
import khanhnq.project.clinicbookingmanagementsystem.service.AddressService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
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
    public ResponseEntityBase getCities() {
        ResponseEntityBase response = new ResponseEntityBase(HttpStatus.OK.value(), null, null);
        List<CityResponse> cities = new ArrayList<>();
        cityRepository.findAll().forEach(city -> {
            cities.add(CityMapper.CITY_MAPPER.mapToCityResponse(city));
        });
        response.setData(cities);
        return response;
    }

    @Override
    public ResponseEntityBase getDistricts(Long cityId) {
        ResponseEntityBase response = new ResponseEntityBase(HttpStatus.OK.value(), null, null);
        List<DistrictResponse> districts = new ArrayList<>();
        districtRepository.getDistrictsByCityId(cityId).forEach(district -> {
            DistrictResponse districtResponse = DistrictMapper.DISTRICT_MAPPER.mapToDistrictResponse(district);
            districtResponse.setCityId(cityId);
            districts.add(districtResponse);
        });
        response.setData(districts);
        return response;
    }

    @Override
    public ResponseEntityBase getWards(Long districtId) {
        ResponseEntityBase response = new ResponseEntityBase(HttpStatus.OK.value(), null, null);
        List<WardResponse> wards = new ArrayList<>();
        wardRepository.getWardsByDistrictId(districtId).forEach(ward -> {
            WardResponse wardResponse = WardMapper.WARD_MAPPER.mapToWardResponse(ward);
            wardResponse.setDistrictId(districtId);
            wards.add(wardResponse);
        });
        response.setData(wards);
        return response;
    }

}