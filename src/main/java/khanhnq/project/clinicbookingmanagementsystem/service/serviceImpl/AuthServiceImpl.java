package khanhnq.project.clinicbookingmanagementsystem.service.serviceImpl;

import khanhnq.project.clinicbookingmanagementsystem.entity.ERole;
import khanhnq.project.clinicbookingmanagementsystem.entity.Role;
import khanhnq.project.clinicbookingmanagementsystem.entity.User;
import khanhnq.project.clinicbookingmanagementsystem.repository.RoleRepository;
import khanhnq.project.clinicbookingmanagementsystem.repository.UserRepository;
import khanhnq.project.clinicbookingmanagementsystem.request.ChangePasswordRequest;
import khanhnq.project.clinicbookingmanagementsystem.request.LoginRequest;
import khanhnq.project.clinicbookingmanagementsystem.request.RegisterRequest;
import khanhnq.project.clinicbookingmanagementsystem.response.MessageResponse;
import khanhnq.project.clinicbookingmanagementsystem.response.UserInfoResponse;
import khanhnq.project.clinicbookingmanagementsystem.security.jwt.JwtUtils;
import khanhnq.project.clinicbookingmanagementsystem.security.services.UserDetailsImpl;
import khanhnq.project.clinicbookingmanagementsystem.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class AuthServiceImpl implements AuthService {
    private UserRepository userRepository;

    private RoleRepository roleRepository;

    private PasswordEncoder passwordEncoder;

    private AuthenticationManager authenticationManager;

    private JwtUtils jwtUtils;

    @Autowired
    public AuthServiceImpl(UserRepository userRepository,
                           RoleRepository roleRepository,
                           PasswordEncoder passwordEncoder,
                           AuthenticationManager authenticationManager,
                           JwtUtils jwtUtils) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
    }

    @Override
    public ResponseEntity<String> register(RegisterRequest registerRequest) {
        if (!Objects.isNull(userRepository.findUserByUsername(registerRequest.getUsername()))) {
            throw new RuntimeException("Username " + registerRequest.getUsername() + " is already exist. Try again!");
        }
        if (!Objects.isNull(userRepository.findUserByEmail(registerRequest.getEmail()))) {
            throw new RuntimeException("Email " + registerRequest.getEmail() + " is already exist. Try again!");
        }
        User user = User.builder()
                .username(registerRequest.getUsername())
                .email(registerRequest.getEmail())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
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
                    case "ROLE_ADMIN":
                        if (roleRepository.findRoleByRoleName(ERole.ROLE_ADMIN) == null) {
                            roleRepository.save(Role.builder().roleName(ERole.ROLE_ADMIN).build());
                        }
                        user.setRoles(registerRequest.getRoles().stream().map(r -> roleRepository.findRoleByRoleName(ERole.valueOf(r))).collect(Collectors.toSet()));
                        break;
                    case "ROLE_DOCTOR":
                        if (roleRepository.findRoleByRoleName(ERole.ROLE_DOCTOR) == null) {
                            roleRepository.save(Role.builder().roleName(ERole.ROLE_DOCTOR).build());
                        }
                        user.setRoles(registerRequest.getRoles().stream().map(r -> roleRepository.findRoleByRoleName(ERole.valueOf(r))).collect(Collectors.toSet()));
                        break;
                    default:
                        user.setRoles(registerRequest.getRoles().stream().map(r -> roleRepository.findRoleByRoleName(ERole.valueOf(r))).collect(Collectors.toSet()));
                }
            });
        }
        userRepository.save(user);
        return MessageResponse.getResponseMessage("Register successfully!", HttpStatus.OK);
    }

    @Override
    public ResponseEntity<UserInfoResponse> login(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        ResponseCookie jwtCookie = jwtUtils.generateJwtCookie(userDetails);
        List<String> roles = userDetails.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .collect(Collectors.toList());
        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
                .body(new UserInfoResponse(
                        userDetails.getUserId(),
                        userDetails.getUsername(),
                        userDetails.getEmail(),
                        roles,
                        jwtUtils.generateTokenFromUsername(userDetails.getUsername())));
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
        User user = userRepository.findUserByUsername(authentication.getName());
        return user;
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
            return MessageResponse.getResponseMessage("New password and confirm password is not match. Try again!", HttpStatus.BAD_REQUEST);
        }
    }

    @Override
    public ResponseEntity<MessageResponse> logoutUser() {
        ResponseCookie cookie = jwtUtils.getCleanJwtCookie();
        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cookie.toString()).body(new MessageResponse("You've been log out."));
    }


}