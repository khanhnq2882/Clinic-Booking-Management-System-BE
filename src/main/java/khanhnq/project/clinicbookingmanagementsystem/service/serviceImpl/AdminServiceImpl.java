package khanhnq.project.clinicbookingmanagementsystem.service.serviceImpl;

import khanhnq.project.clinicbookingmanagementsystem.entity.Role;
import khanhnq.project.clinicbookingmanagementsystem.entity.User;
import khanhnq.project.clinicbookingmanagementsystem.entity.enums.ERole;
import khanhnq.project.clinicbookingmanagementsystem.repository.UserRepository;
import khanhnq.project.clinicbookingmanagementsystem.response.MessageResponse;
import khanhnq.project.clinicbookingmanagementsystem.service.AdminService;
import khanhnq.project.clinicbookingmanagementsystem.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class AdminServiceImpl implements AdminService {

    private UserRepository userRepository;

    private AuthService authService;

    public AdminServiceImpl(UserRepository userRepository, AuthService authService) {
        this.userRepository = userRepository;
        this.authService = authService;
    }

    @Override
    public ResponseEntity<String> updateUserRoles(Long userId) {
        User currentUser = authService.getCurrentUser();
        User user = userRepository.findById(userId).orElse(null);
        if (currentUser.getRoles().stream().filter(role -> role.getRoleName().equals(ERole.ROLE_ADMIN.name())).findAny().isPresent()) {
            user.getRoles().add(Role.builder().roleName(ERole.ROLE_DOCTOR).build());
            return MessageResponse.getResponseMessage("Update role user "+user.getFirstName()+" "+user.getLastName()+" successfully.", HttpStatus.OK);
        }
        return MessageResponse.getResponseMessage("You are not authorized to update role of user.", HttpStatus.OK);
    }
}
