package khanhnq.project.clinicbookingmanagementsystem.service.serviceImpl;

import jakarta.servlet.http.HttpServletRequest;
import khanhnq.project.clinicbookingmanagementsystem.constant.MessageConstants;
import khanhnq.project.clinicbookingmanagementsystem.entity.enums.ERole;
import khanhnq.project.clinicbookingmanagementsystem.entity.Role;
import khanhnq.project.clinicbookingmanagementsystem.entity.User;
import khanhnq.project.clinicbookingmanagementsystem.entity.enums.EUserStatus;
import khanhnq.project.clinicbookingmanagementsystem.exception.BusinessException;
import khanhnq.project.clinicbookingmanagementsystem.exception.ResourceAlreadyExistException;
import khanhnq.project.clinicbookingmanagementsystem.exception.ResourceNotFoundException;
import khanhnq.project.clinicbookingmanagementsystem.repository.RoleRepository;
import khanhnq.project.clinicbookingmanagementsystem.repository.UserRepository;
import khanhnq.project.clinicbookingmanagementsystem.request.ChangePasswordRequest;
import khanhnq.project.clinicbookingmanagementsystem.request.LoginRequest;
import khanhnq.project.clinicbookingmanagementsystem.request.RegisterRequest;
import khanhnq.project.clinicbookingmanagementsystem.response.JwtResponse;
import khanhnq.project.clinicbookingmanagementsystem.response.UserInfoResponse;
import khanhnq.project.clinicbookingmanagementsystem.security.jwt.JwtUtils;
import khanhnq.project.clinicbookingmanagementsystem.security.services.UserDetailsImpl;
import khanhnq.project.clinicbookingmanagementsystem.service.AuthService;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.*;
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
    public String register(RegisterRequest registerRequest) {
        if (!Objects.isNull(userRepository.findUserByUsername(registerRequest.getUsername()))) {
            throw new ResourceAlreadyExistException("Username", registerRequest.getUsername());
        }
        if (!Objects.isNull(userRepository.findUserByEmail(registerRequest.getEmail()))) {
            throw new ResourceAlreadyExistException("Email", registerRequest.getEmail());
        }
        String userCode;
        if (userRepository.findAll().size() == 0) {
            userCode = "US1";
        } else {
            Long nextId = Collections.max(userRepository.findAll().stream().map(User::getUserId).toList());
            userCode = "US" + (++nextId);
        }
        User user = User.builder()
                .userCode(userCode)
                .username(registerRequest.getUsername())
                .email(registerRequest.getEmail())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .status(EUserStatus.ACTIVE)
                .build();
        if (registerRequest.getRoles() == null) {
            Role role = roleRepository.findRoleByRoleName(ERole.ROLE_USER)
                    .orElseThrow(() -> new ResourceNotFoundException("Role", "ROLE_USER"));
            Set<Role> roles = new HashSet<>();
            roles.add(role);
            user.setRoles(roles);
        } else {
            registerRequest.getRoles().forEach(role -> {
                switch (role) {
                    case "ROLE_ADMIN" -> {
                        if (Objects.isNull(roleRepository.findRoleByRoleName(ERole.ROLE_ADMIN)))
                            throw new ResourceNotFoundException("Role", role);
                    }
                    case "ROLE_DOCTOR" -> {
                        if (Objects.isNull(roleRepository.findRoleByRoleName(ERole.ROLE_DOCTOR)))
                            throw new ResourceNotFoundException("Role", role);
                    }
                    case "ROLE_USER" -> {
                        if (Objects.isNull(roleRepository.findRoleByRoleName(ERole.ROLE_USER)))
                            throw new ResourceNotFoundException("Role", role);
                    }
                    default -> throw new ResourceNotFoundException("Role", role);
                }
            });
            Set<Role> roles = registerRequest.getRoles().stream()
                    .map(r -> roleRepository.findRoleByRoleName(ERole.valueOf(r)).get()).collect(Collectors.toSet());
            user.setRoles(roles);
        }
        userRepository.save(user);
        return MessageConstants.REGISTER_SUCCESS;
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
    public String logout(HttpServletRequest request) {
        if (request.getHeader("Authorization") != null && request.getHeader("Authorization").startsWith("Bearer ")) {
            SecurityContextHolder.clearContext();
        } else {
            return MessageConstants.LOGOUT_FAILED;
        }
        return MessageConstants.LOGOUT_SUCCESS;
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
    public String changePassword(ChangePasswordRequest changePasswordRequest) {
        User currentUser = getCurrentUser();
        if (new BCryptPasswordEncoder().matches(changePasswordRequest.getCurrentPassword(), currentUser.getPassword())) {
            if (changePasswordRequest.getNewPassword().equals(changePasswordRequest.getConfirmPassword())) {
                currentUser.setPassword(passwordEncoder.encode(changePasswordRequest.getNewPassword()));
                currentUser.setUpdatedBy(currentUser.getUsername());
                userRepository.save(currentUser);
                return MessageConstants.CHANGE_PASSWORD_SUCCESS;
            }
            throw new BusinessException(MessageConstants.NOT_MATCH_PASSWORD);
        }
        throw new BusinessException(MessageConstants.INCORRECT_CURRENT_PASSWORD);
    }

    @Override
    public UserInfoResponse getUserByUsername(String username) {
        return getUser(userRepository.findUserByUsername(username));
    }

    @Override
    public UserInfoResponse getUserInfo() {
        return getUser(getCurrentUser());
    }

    public UserInfoResponse getUser (User user) {
        List<String> roles = user.getRoles().stream()
                .map(role -> role.getRoleName().name()).collect(Collectors.toList());
        return UserInfoResponse.builder()
                .id(user.getUserId())
                .username(user.getUsername())
                .email(user.getEmail())
                .roles(roles)
                .build();
    }

}