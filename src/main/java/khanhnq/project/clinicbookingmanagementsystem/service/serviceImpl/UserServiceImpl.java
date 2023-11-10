package khanhnq.project.clinicbookingmanagementsystem.service.serviceImpl;

import khanhnq.project.clinicbookingmanagementsystem.entity.Address;
import khanhnq.project.clinicbookingmanagementsystem.entity.File;
import khanhnq.project.clinicbookingmanagementsystem.entity.User;
import khanhnq.project.clinicbookingmanagementsystem.repository.AddressRepository;
import khanhnq.project.clinicbookingmanagementsystem.repository.UserRepository;
import khanhnq.project.clinicbookingmanagementsystem.repository.WardRepository;
import khanhnq.project.clinicbookingmanagementsystem.request.UserProfileRequest;
import khanhnq.project.clinicbookingmanagementsystem.response.MessageResponse;
import khanhnq.project.clinicbookingmanagementsystem.service.FileService;
import khanhnq.project.clinicbookingmanagementsystem.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

@Service
public class UserServiceImpl implements UserService {

    private FileService fileService;

    private UserRepository userRepository;

    private WardRepository wardRepository;

    private AddressRepository addressRepository;

    public UserServiceImpl(FileService fileService, UserRepository userRepository, WardRepository wardRepository, AddressRepository addressRepository) {
        this.fileService = fileService;
        this.userRepository = userRepository;
        this.wardRepository = wardRepository;
        this.addressRepository = addressRepository;
    }

    @Override
    public ResponseEntity<String> updateProfile(UserProfileRequest userProfileRequest) {
        try {
            DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
            User currentUser = userRepository.findById(2L).orElse(null);
//            User currentUser = authService.getCurrentUser();
            currentUser.setFirstName(userProfileRequest.getFirstName());
            currentUser.setLastName(userProfileRequest.getLastName());
            currentUser.setDateOfBirth(dateFormat.parse(userProfileRequest.getDateOfBirth()));
            currentUser.setGender(userProfileRequest.getGender());
            currentUser.setPhoneNumber(userProfileRequest.getPhoneNumber());
            Address address = new Address();
            address.setSpecificAddress(userProfileRequest.getSpecificAddress());
            address.setWard(wardRepository.findById(userProfileRequest.getWardId()).orElse(null));
            currentUser.setAddress(address);
            userRepository.save(currentUser);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        return MessageResponse.getResponseMessage("Update profile successfully!", HttpStatus.OK);
    }

    @Override
    public ResponseEntity<String> uploadAvatar(MultipartFile file) {
        try {
            fileService.saveAvatar(file);
            User currentUser = userRepository.findById(2L).orElse(null);
//            User currentUser = authService.getCurrentUser();
            currentUser.setAvatarFile(File.builder().filePath(currentUser.getUsername()+"/"+file.getOriginalFilename()).build());
            userRepository.save(currentUser);
            return MessageResponse.getResponseMessage("Uploaded the file successfully: " + file.getOriginalFilename(), HttpStatus.OK);
        } catch (Exception e) {
            return MessageResponse.getResponseMessage("Could not upload the file: " + file.getOriginalFilename() + ". Error: " + e.getMessage(), HttpStatus.EXPECTATION_FAILED);
        }
    }
}