package khanhnq.project.clinicbookingmanagementsystem.controller;

import khanhnq.project.clinicbookingmanagementsystem.repository.UserRepository;
import khanhnq.project.clinicbookingmanagementsystem.request.UserProfileRequest;
import khanhnq.project.clinicbookingmanagementsystem.service.FileService;
import khanhnq.project.clinicbookingmanagementsystem.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@CrossOrigin(origins = "http://localhost:4200", maxAge = 3600, allowCredentials="true")
@RestController
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    private final FileService fileService;

    private final UserRepository userRepository;

    public UserController(UserService userService, FileService fileService, UserRepository userRepository) {
        this.userService = userService;
        this.fileService = fileService;
        this.userRepository = userRepository;
    }

    @PostMapping("/update-profile")
    public ResponseEntity<String> updateProfile(@RequestBody UserProfileRequest userProfileRequest) {
        return userService.updateProfile(userProfileRequest);
    }

    @PostMapping("/upload-avatar")
    public ResponseEntity<String> uploadFile(@RequestParam("avatar") MultipartFile file) {
        return userService.uploadAvatar(file);
    }



}