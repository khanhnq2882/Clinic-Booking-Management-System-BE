package khanhnq.project.clinicbookingmanagementsystem.service.serviceImpl;

import khanhnq.project.clinicbookingmanagementsystem.entity.enums.ERole;
import khanhnq.project.clinicbookingmanagementsystem.entity.Role;
import khanhnq.project.clinicbookingmanagementsystem.entity.User;
import khanhnq.project.clinicbookingmanagementsystem.entity.enums.EUserStatus;
import khanhnq.project.clinicbookingmanagementsystem.repository.RoleRepository;
import khanhnq.project.clinicbookingmanagementsystem.repository.UserRepository;
import khanhnq.project.clinicbookingmanagementsystem.request.ChangePasswordRequest;
import khanhnq.project.clinicbookingmanagementsystem.request.LoginRequest;
import khanhnq.project.clinicbookingmanagementsystem.request.RegisterRequest;
import khanhnq.project.clinicbookingmanagementsystem.response.JwtResponse;
import khanhnq.project.clinicbookingmanagementsystem.response.MessageResponse;
import khanhnq.project.clinicbookingmanagementsystem.response.UserInfoResponse;
import khanhnq.project.clinicbookingmanagementsystem.security.jwt.JwtUtils;
import khanhnq.project.clinicbookingmanagementsystem.security.services.UserDetailsImpl;
import khanhnq.project.clinicbookingmanagementsystem.service.AuthService;
import lombok.AllArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class AuthServiceImpl implements AuthService {
    private UserRepository userRepository;

    private RoleRepository roleRepository;

    private PasswordEncoder passwordEncoder;

    private AuthenticationManager authenticationManager;

    private JwtUtils jwtUtils;

    @Override
    public ResponseEntity<String> register(RegisterRequest registerRequest) {
        if (!Objects.isNull(userRepository.findUserByUsername(registerRequest.getUsername()))) {
            throw new RuntimeException("Username " + registerRequest.getUsername() + " is already exist. Try again!");
        }
        if (!Objects.isNull(userRepository.findUserByEmail(registerRequest.getEmail()))) {
            throw new RuntimeException("Email " + registerRequest.getEmail() + " is already exist. Try again!");
        }
        String userCode;
        if (userRepository.findAll().size() == 0) {
            userCode = "US1";
        } else {
            Long nextId = Collections.max(userRepository.findAll().stream().map(User::getUserId).toList());
            userCode = "US" + nextId;
        }
        User user = User.builder()
                .userCode(userCode)
                .username(registerRequest.getUsername())
                .email(registerRequest.getEmail())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .status(EUserStatus.ACTIVE)
                .build();
        if (roleRepository.findRoleByRoleName(ERole.ROLE_USER) == null) {
            roleRepository.save(Role.builder().roleName(ERole.ROLE_USER).build());
        }
        if (registerRequest.getRoles().size() == 0) {
            registerRequest.getRoles().add(ERole.ROLE_USER.name());
            user.setRoles(registerRequest.getRoles().stream().map(r -> roleRepository.findRoleByRoleName(ERole.valueOf(r))).collect(Collectors.toSet()));
        } else {
            registerRequest.getRoles().forEach(role -> {
                switch (role) {
                    case "ROLE_ADMIN" -> {
                        if (roleRepository.findRoleByRoleName(ERole.ROLE_ADMIN) == null) {
                            roleRepository.save(Role.builder().roleName(ERole.ROLE_ADMIN).build());
                        }
                        user.setRoles(registerRequest.getRoles().stream().map(r -> roleRepository.findRoleByRoleName(ERole.valueOf(r))).collect(Collectors.toSet()));
                    }
                    case "ROLE_DOCTOR" -> {
                        if (roleRepository.findRoleByRoleName(ERole.ROLE_DOCTOR) == null) {
                            roleRepository.save(Role.builder().roleName(ERole.ROLE_DOCTOR).build());
                        }
                        user.setRoles(registerRequest.getRoles().stream().map(r -> roleRepository.findRoleByRoleName(ERole.valueOf(r))).collect(Collectors.toSet()));
                    }
                    default ->
                            user.setRoles(registerRequest.getRoles().stream().map(r -> roleRepository.findRoleByRoleName(ERole.valueOf(r))).collect(Collectors.toSet()));
                }
            });
        }
        userRepository.save(user);
        return MessageResponse.getResponseMessage("Register successfully!", HttpStatus.OK);
    }

    @Override
    public JwtResponse login(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        return new JwtResponse(jwtUtils.generateTokenFromUsername(userDetails.getUsername()));
    }

    @Override
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return null;
        }
        if (!authentication.isAuthenticated() || authentication.getName() == null) {
            return null;
        }
        return userRepository.findUserByUsername(authentication.getName());
    }

    @Override
    public ResponseEntity<String> changePassword(ChangePasswordRequest changePasswordRequest) {
        User currentUser = getCurrentUser();
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        if (bCryptPasswordEncoder.matches(changePasswordRequest.getCurrentPassword(), currentUser.getPassword())) {
            if (changePasswordRequest.getNewPassword().equals(changePasswordRequest.getConfirmPassword())) {
                currentUser.setPassword(passwordEncoder.encode(changePasswordRequest.getNewPassword()));
                userRepository.save(currentUser);
                return MessageResponse.getResponseMessage("Change password successfully!", HttpStatus.OK);
            } else {
                return MessageResponse.getResponseMessage("New password and confirm password is not match. Try again!", HttpStatus.BAD_REQUEST);
            }
        } else {
            return MessageResponse.getResponseMessage("Current password is wrong. Try again!", HttpStatus.BAD_REQUEST);
        }
    }

    @Override
    public UserInfoResponse getUserInfo() {
        User user = getCurrentUser();
        List<String> roles = user.getRoles()
                .stream()
                .map(role -> role.getRoleName().name())
                .collect(Collectors.toList());
        return UserInfoResponse.builder()
                .id(user.getUserId())
                .username(user.getUsername())
                .email(user.getEmail())
                .roles(roles)
                .build();
    }


}