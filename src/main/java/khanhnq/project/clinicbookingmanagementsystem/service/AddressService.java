package khanhnq.project.clinicbookingmanagementsystem.service;

import khanhnq.project.clinicbookingmanagementsystem.model.response.ResponseEntityBase;

public interface AddressService {
    ResponseEntityBase getCities();
    ResponseEntityBase getDistricts(Long cityId);
    ResponseEntityBase getWards(Long districtId);
}
