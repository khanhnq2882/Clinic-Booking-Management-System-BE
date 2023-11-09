package khanhnq.project.clinicbookingmanagementsystem.service.serviceImpl;

import khanhnq.project.clinicbookingmanagementsystem.entity.Address;
import khanhnq.project.clinicbookingmanagementsystem.entity.User;
import khanhnq.project.clinicbookingmanagementsystem.mapper.UserMapper;
import khanhnq.project.clinicbookingmanagementsystem.repository.AddressRepository;
import khanhnq.project.clinicbookingmanagementsystem.repository.UserRepository;
import khanhnq.project.clinicbookingmanagementsystem.repository.WardRepository;
import khanhnq.project.clinicbookingmanagementsystem.request.UserProfileRequest;
import khanhnq.project.clinicbookingmanagementsystem.response.MessageResponse;
import khanhnq.project.clinicbookingmanagementsystem.service.AuthService;
import khanhnq.project.clinicbookingmanagementsystem.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WardRepository wardRepository;

    @Autowired
    private AddressRepository addressRepository;

    @Override
    public ResponseEntity<String> updateProfile(UserProfileRequest userProfileRequest) {
        try {
            DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
            User currentUser = userRepository.findById(1L).orElse(null);
//            User currentUser = authService.getCurrentUser();
            currentUser.setFirstName(userProfileRequest.getFirstName());
            currentUser.setLastName(userProfileRequest.getLastName());
            currentUser.setDateOfBirth(dateFormat.parse(userProfileRequest.getDateOfBirth()));
            currentUser.setGender(userProfileRequest.getGender());
            currentUser.setPhoneNumber(userProfileRequest.getPhoneNumber());
            Address address = new Address();
            address.setSpecificAddress(userProfileRequest.getSpecificAddress());
            address.setWard(wardRepository.findById(userProfileRequest.getWardId()).orElse(null));
            addressRepository.save(address);
            currentUser.setAddress(address);
            userRepository.save(currentUser);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        return MessageResponse.getResponseMessage("Update profile successfully!", HttpStatus.OK);
    }
}