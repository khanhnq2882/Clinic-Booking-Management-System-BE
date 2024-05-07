package khanhnq.project.clinicbookingmanagementsystem.service;

import khanhnq.project.clinicbookingmanagementsystem.response.CityResponse;
import khanhnq.project.clinicbookingmanagementsystem.response.DistrictResponse;
import khanhnq.project.clinicbookingmanagementsystem.response.WardResponse;
import java.util.List;

public interface AddressService {
    List<CityResponse> getCities();
    List<DistrictResponse> getDistricts(Long cityId);
    List<WardResponse> getWards(Long districtId);
}
