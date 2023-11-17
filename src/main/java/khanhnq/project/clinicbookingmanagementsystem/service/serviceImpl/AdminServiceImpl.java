package khanhnq.project.clinicbookingmanagementsystem.service.serviceImpl;

import khanhnq.project.clinicbookingmanagementsystem.entity.Address;
import khanhnq.project.clinicbookingmanagementsystem.entity.Role;
import khanhnq.project.clinicbookingmanagementsystem.entity.User;
import khanhnq.project.clinicbookingmanagementsystem.entity.enums.ERole;
import khanhnq.project.clinicbookingmanagementsystem.repository.AddressRepository;
import khanhnq.project.clinicbookingmanagementsystem.repository.RoleRepository;
import khanhnq.project.clinicbookingmanagementsystem.repository.UserRepository;
import khanhnq.project.clinicbookingmanagementsystem.response.MessageResponse;
import khanhnq.project.clinicbookingmanagementsystem.response.UserResponse;
import khanhnq.project.clinicbookingmanagementsystem.service.AdminService;
import khanhnq.project.clinicbookingmanagementsystem.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;

    private final AuthService authService;

    private final RoleRepository roleRepository;

    private final AddressRepository addressRepository;

    public AdminServiceImpl(UserRepository userRepository,
                            AuthService authService,
                            RoleRepository roleRepository,
                            AddressRepository addressRepository) {
        this.userRepository = userRepository;
        this.authService = authService;
        this.roleRepository = roleRepository;
        this.addressRepository = addressRepository;
    }

    @Override
    public ResponseEntity<String> updateUserRoles(Long userId) {
        User currentUser = authService.getCurrentUser();
        User user = userRepository.findById(userId).orElse(null);
        if (currentUser.getRoles().stream().filter(role -> role.getRoleName().equals(ERole.ROLE_ADMIN)).findAny().isPresent()) {
            Role role = roleRepository.findRoleByRoleName(ERole.ROLE_DOCTOR);
            if (role == null) {
                roleRepository.save(Role.builder().roleName(ERole.ROLE_DOCTOR).build());
            }
            user.getRoles().add(role);
            userRepository.save(user);
            return MessageResponse.getResponseMessage("Update successfully .User "+user.getFirstName()+" "+user.getLastName()+" is became a doctor in the system.", HttpStatus.OK);
        }
        return MessageResponse.getResponseMessage("You do not have permission to update user roles.", HttpStatus.OK);
    }

    @Override
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        List<UserResponse> userList = userRepository.getAllUsers().stream().map(user -> {
            UserResponse userResponse = new UserResponse();
            userResponse.setUserId(user.getUserId());
            userResponse.setUserCode(user.getUserCode());
            userResponse.setEmail(user.getEmail());
            userResponse.setFirstName(user.getFirstName());
            userResponse.setLastName(user.getLastName());
            userResponse.setDateOfBirth(user.getDateOfBirth());
            userResponse.setGender(user.getGender());
            userResponse.setPhoneNumber(user.getPhoneNumber());
            userResponse.setRoles(user.getRoles().stream()
                    .map(role -> role.getRoleName().name())
                    .collect(Collectors.toList()));
            userResponse.setStatus(user.getStatus().name());
            if (user.getAddress() != null) {
                Address address = addressRepository.findById(user.getAddress().getAddressId()).orElse(null);
                userResponse.setSpecificAddress(address.getSpecificAddress());
                userResponse.setWardName(address.getWard().getWardName());
                userResponse.setDistrictName(address.getWard().getDistrict().getDistrictName());
                userResponse.setCityName(address.getWard().getDistrict().getCity().getCityName());
            }
            return userResponse;
        }).collect(Collectors.toList());
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(userList);
    }
}
