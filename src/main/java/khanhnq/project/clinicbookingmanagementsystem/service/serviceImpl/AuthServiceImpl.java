package khanhnq.project.clinicbookingmanagementsystem.service.serviceImpl;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.HttpServletRequest;
import khanhnq.project.clinicbookingmanagementsystem.constant.MessageConstants;
import khanhnq.project.clinicbookingmanagementsystem.entity.Doctor;
import khanhnq.project.clinicbookingmanagementsystem.entity.enums.ERole;
import khanhnq.project.clinicbookingmanagementsystem.entity.Role;
import khanhnq.project.clinicbookingmanagementsystem.entity.User;
import khanhnq.project.clinicbookingmanagementsystem.entity.enums.EUserStatus;
import khanhnq.project.clinicbookingmanagementsystem.exception.*;
import khanhnq.project.clinicbookingmanagementsystem.repository.DoctorRepository;
import khanhnq.project.clinicbookingmanagementsystem.repository.RoleRepository;
import khanhnq.project.clinicbookingmanagementsystem.repository.UserRepository;
import khanhnq.project.clinicbookingmanagementsystem.request.AccountSystemRequest;
import khanhnq.project.clinicbookingmanagementsystem.request.ChangePasswordRequest;
import khanhnq.project.clinicbookingmanagementsystem.request.LoginRequest;
import khanhnq.project.clinicbookingmanagementsystem.request.RegisterRequest;
import khanhnq.project.clinicbookingmanagementsystem.response.JwtResponse;
import khanhnq.project.clinicbookingmanagementsystem.response.UserInfoResponse;
import khanhnq.project.clinicbookingmanagementsystem.security.jwt.JwtUtils;
import khanhnq.project.clinicbookingmanagementsystem.security.services.UserDetailsImpl;
import khanhnq.project.clinicbookingmanagementsystem.service.AuthService;
import lombok.AllArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.security.SecureRandom;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@AllArgsConstructor
public class AuthServiceImpl implements AuthService {
    private UserRepository userRepository;
    private DoctorRepository doctorRepository;
    private RoleRepository roleRepository;
    private PasswordEncoder passwordEncoder;
    private AuthenticationManager authenticationManager;
    private JwtUtils jwtUtils;
    private final JavaMailSender mailSender;

    @Override
    public String register(RegisterRequest registerRequest) {
        checkAccountExist(registerRequest.getUsername(), registerRequest.getEmail());
        User user = User.builder()
                .userCode(createUserCode(userRepository.getUsers(), "US"))
                .username(registerRequest.getUsername())
                .email(registerRequest.getEmail())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .status(EUserStatus.ACTIVE)
                .build();
        if (registerRequest.getRoles().size() == 0) {
            Role role = roleRepository.findRoleByRoleName(ERole.ROLE_USER)
                    .orElseThrow(() -> new ResourceNotFoundException("Role", "ROLE_USER"));
            Set<Role> roles = new HashSet<>();
            roles.add(role);
            user.setRoles(roles);
        } else {
            registerRequest.getRoles().forEach(role -> {
                if (role.equals("ROLE_USER")) {
                    if (Objects.isNull(roleRepository.findRoleByRoleName(ERole.ROLE_USER)))
                        throw new ResourceNotFoundException("Role", role);
                } else {
                    throw new ResourceNotFoundException("Role", role);
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
    public String newSystemAccount(AccountSystemRequest accountSystemRequest) throws MessagingException {
        User currentUser = getCurrentUser();
        if (Objects.isNull(currentUser)) {
            throw new UnauthorizedException(MessageConstants.UNAUTHORIZED_ACCESS);
        }
        if (currentUser.getRoles().stream().noneMatch(role -> role.getRoleName().name().equals("ROLE_ADMIN"))) {
            throw new ForbiddenException(MessageConstants.FORBIDDEN_ACCESS);
        }
        checkAccountExist(accountSystemRequest.getUsername(), accountSystemRequest.getEmail());
        String password = randomPassword();
        User user = User.builder()
                .email(accountSystemRequest.getEmail())
                .username(accountSystemRequest.getUsername())
                .password(passwordEncoder.encode(password))
                .status(EUserStatus.ACTIVE)
                .build();
        Doctor doctor = new Doctor();
        accountSystemRequest.getRoles().forEach(role -> {
            switch (role) {
                case "ROLE_ADMIN" -> {
                    if (Objects.isNull(roleRepository.findRoleByRoleName(ERole.ROLE_ADMIN))) {
                        throw new ResourceNotFoundException("Role", role);
                    }
                    user.setUserCode(createUserCode(userRepository.getAdmins(), "AD"));
                }
                case "ROLE_DOCTOR" -> {
                    if (Objects.isNull(roleRepository.findRoleByRoleName(ERole.ROLE_DOCTOR))) {
                        throw new ResourceNotFoundException("Role", role);
                    }
                    user.setUserCode(createUserCode(userRepository.getDoctors(), "DT"));
                }
                case "ROLE_USER" -> {
                    if (Objects.isNull(roleRepository.findRoleByRoleName(ERole.ROLE_USER))) {
                        throw new ResourceNotFoundException("Role", role);
                    }
                    user.setUserCode(createUserCode(userRepository.getUsers(), "US"));
                }
                default -> throw new ResourceNotFoundException("Role", role);
            }
        });
        Set<Role> roles = accountSystemRequest.getRoles().stream()
                .map(role -> roleRepository.findRoleByRoleName(ERole.valueOf(role)).get()).collect(Collectors.toSet());
        user.setRoles(roles);
        doctor.setUser(user);
        doctor.setCreatedBy(currentUser.getEmail());
        userRepository.save(user);
        doctorRepository.save(doctor);
        newAccountEmail(accountSystemRequest.getEmail(), accountSystemRequest.getUsername(), password);
        return MessageConstants.REGISTER_SUCCESS;
    }

    @Override
    public JwtResponse login(LoginRequest loginRequest) {
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));
        } catch (Exception ex) {
            throw new ForbiddenException(MessageConstants.LOGIN_FAILED);
        }
        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        return new JwtResponse(jwtUtils.generateTokenFromUsername(userDetails.getUsername()));
    }

    @Override
    public String logout(HttpServletRequest request) {
        if (request.getHeader("Authorization") == null || !request.getHeader("Authorization").startsWith("Bearer ")) {
            return MessageConstants.LOGOUT_FAILED;
        }
        SecurityContextHolder.clearContext();
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
    public String forgotPassword(String email) throws MessagingException{
        return resetPasswordEmail(email);
    }

    @Override
    public UserInfoResponse getUserByUsername(String username) {
        return getUser(userRepository.findUserByUsername(username));
    }

    @Override
    public UserInfoResponse getUserInfo() {
        return getUser(getCurrentUser());
    }

    public UserInfoResponse getUser(User user) {
        List<String> roles = user.getRoles().stream()
                .map(role -> role.getRoleName().name()).collect(Collectors.toList());
        return UserInfoResponse.builder()
                .id(user.getUserId())
                .username(user.getUsername())
                .email(user.getEmail())
                .roles(roles)
                .build();
    }

    public String resetPasswordEmail(String email) throws MessagingException {
        User user = userRepository.findUserByEmail(email);
        if (Objects.nonNull(user)) {
            MimeMessage message = mailSender.createMimeMessage();
            message.setFrom(new InternetAddress(email));
            message.setRecipients(MimeMessage.RecipientType.TO, "quockhanhnguyen2882@gmail.com");
            message.setSubject("Request reset password from email "+email);
            String htmlContent =
                    "<body>" +
                    "<p>Dear Admin Teams,</p>" +
                    "<p>Request reset password from email <b>'" +email+ "'</b>. Please check account information and reset password.</p>" +
                    "<p>Thanks and Best Regards</p>" +
                    "</body>";
            message.setContent(htmlContent, "text/html; charset=utf-8");
            mailSender.send(message);
            return MessageConstants.REQUEST_RESET_PASSWORD_SUCCESS;
        } else {
            throw new ResourceNotFoundException("Email", email);
        }
    }

    public String newAccountEmail(String email, String username, String password) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        message.setFrom(new InternetAddress(email));
        message.setRecipients(MimeMessage.RecipientType.TO, "quockhanhnguyen2882@gmail.com");
        message.setSubject("Account Credentials for Clinic System Access");
        String htmlContent =
                "<body>" +
                        "<p>Hello,</p>" +
                        "<p>We are pleased to inform you that your account has been created for accessing our clinic system. Below are your login details:</p>" +
                        "<ul>" +
                            "<li><b>Username:</b>" +username+ "</li>" +
                            "<li><b>Password:</b>" +password+ "</li>" +
                        "</ul>" +
                        "<p>For security reasons, we recommend changing your password after your first login.</p>" +
                        "<p>Thanks and Best Regards</p>" +
                        "</body>";
        message.setContent(htmlContent, "text/html; charset=utf-8");
        mailSender.send(message);
        return MessageConstants.SEND_ACCOUNT_CREDENTIALS_SUCCESS;
    }

    private void checkAccountExist(String username, String email) {
        if (!Objects.isNull(userRepository.findUserByUsername(username))) {
            throw new ResourceAlreadyExistException("Username", username);
        }
        if (!Objects.isNull(userRepository.findUserByEmail(email))) {
            throw new ResourceAlreadyExistException("Email", email);
        }
    }

    private String randomPassword() {
        final String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        SecureRandom random = new SecureRandom();
        return IntStream.range(0, 8)
                .map(i -> random.nextInt(chars.length()))
                .mapToObj(randomIndex -> String.valueOf(chars.charAt(randomIndex)))
                .collect(Collectors.joining());
    }

    private String createUserCode(List<User> users, String roleType) {
        StringBuilder userCode = new StringBuilder();
        if (users.size() == 0) {
            userCode.append(roleType).append("1");
        } else {
            Long nextId = Collections.max(users.stream()
                            .map(user -> Long.parseLong(user.getUserCode().substring(2))).toList());
            userCode.append(roleType).append(++nextId);
        }
        return userCode.toString();
    }

}