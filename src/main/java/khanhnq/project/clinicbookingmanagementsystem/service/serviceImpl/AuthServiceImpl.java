package khanhnq.project.clinicbookingmanagementsystem.service.serviceImpl;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.HttpServletRequest;
import khanhnq.project.clinicbookingmanagementsystem.common.MessageConstants;
import khanhnq.project.clinicbookingmanagementsystem.entity.Doctor;
import khanhnq.project.clinicbookingmanagementsystem.entity.RefreshToken;
import khanhnq.project.clinicbookingmanagementsystem.entity.enums.ERole;
import khanhnq.project.clinicbookingmanagementsystem.entity.Role;
import khanhnq.project.clinicbookingmanagementsystem.entity.User;
import khanhnq.project.clinicbookingmanagementsystem.entity.enums.EUserStatus;
import khanhnq.project.clinicbookingmanagementsystem.exception.*;
import khanhnq.project.clinicbookingmanagementsystem.model.response.RefreshTokenResponse;
import khanhnq.project.clinicbookingmanagementsystem.model.response.ResponseEntityBase;
import khanhnq.project.clinicbookingmanagementsystem.repository.DoctorRepository;
import khanhnq.project.clinicbookingmanagementsystem.repository.RefreshTokenRepository;
import khanhnq.project.clinicbookingmanagementsystem.repository.RoleRepository;
import khanhnq.project.clinicbookingmanagementsystem.repository.UserRepository;
import khanhnq.project.clinicbookingmanagementsystem.model.request.AccountSystemRequest;
import khanhnq.project.clinicbookingmanagementsystem.model.request.ChangePasswordRequest;
import khanhnq.project.clinicbookingmanagementsystem.model.request.LoginRequest;
import khanhnq.project.clinicbookingmanagementsystem.model.request.RegisterRequest;
import khanhnq.project.clinicbookingmanagementsystem.model.response.UserInfoResponse;
import khanhnq.project.clinicbookingmanagementsystem.security.jwt.JwtUtils;
import khanhnq.project.clinicbookingmanagementsystem.security.services.BruteForceProtectionService;
import khanhnq.project.clinicbookingmanagementsystem.security.services.UserDetailsImpl;
import khanhnq.project.clinicbookingmanagementsystem.service.AuthService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class AuthServiceImpl implements AuthService {
    private UserRepository userRepository;
    private RoleRepository roleRepository;
    private DoctorRepository doctorRepository;
    private RefreshTokenRepository refreshTokenRepository;
    private PasswordEncoder passwordEncoder;
    private AuthenticationManager authenticationManager;
    private JwtUtils jwtUtils;
    private final JavaMailSender mailSender;
    private final BruteForceProtectionService bruteForceProtectionService;

    @Value("${khanhnq.cbms.jwtRefreshExpirationMs}")
    private Long refreshTokenDurationMs;

    public AuthServiceImpl(UserRepository userRepository,
                           RoleRepository roleRepository,
                           DoctorRepository doctorRepository,
                           RefreshTokenRepository refreshTokenRepository,
                           PasswordEncoder passwordEncoder,
                           AuthenticationManager authenticationManager,
                           JwtUtils jwtUtils, JavaMailSender mailSender,
                           BruteForceProtectionService bruteForceProtectionService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.doctorRepository = doctorRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
        this.mailSender = mailSender;
        this.bruteForceProtectionService = bruteForceProtectionService;
    }

    @Override
    public ResponseEntityBase register(RegisterRequest registerRequest) {
        ResponseEntityBase response = new ResponseEntityBase(HttpStatus.OK.value(), null, null);
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
        response.setData(MessageConstants.REGISTER_SUCCESS);
        return response;
    }

    @Override
    public ResponseEntityBase newSystemAccount(AccountSystemRequest accountSystemRequest) throws MessagingException {
        ResponseEntityBase response = new ResponseEntityBase(HttpStatus.OK.value(), null, null);
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
                    doctor.setCreatedBy(currentUser.getUsername());
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
        user.setCreatedBy(currentUser.getUsername());
        userRepository.save(user);
        doctor.setUser(user);
        doctorRepository.save(doctor);
        newAccountEmail(accountSystemRequest.getEmail(), accountSystemRequest.getUsername(), password);
        response.setData(MessageConstants.ADD_NEW_SYSTEM_ACCOUNT_SUCCESS);
        return response;
    }

    @Override
    public ResponseEntityBase login(LoginRequest loginRequest) throws UnknownHostException {
        ResponseEntityBase response = new ResponseEntityBase(HttpStatus.OK.value(), null, null);
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        String accessToken = jwtUtils.generateTokenFromUsername(userDetails.getUsername());
        if (!loginRequest.isRememberMe()) {
            response.setData(accessToken);
        } else {
            User user = userRepository.findById(userDetails.getUserId()).get();
            RefreshToken existRefreshToken = refreshTokenRepository.findRefreshTokenByUserId(userDetails.getUserId());
            RefreshToken refreshToken = Objects.isNull(existRefreshToken) ? new RefreshToken() : existRefreshToken;
            refreshToken.setToken(UUID.randomUUID().toString());
            refreshToken.setExpiryDate(Instant.now().plusMillis(refreshTokenDurationMs));
            refreshToken.setDeviceInfo(InetAddress.getLocalHost().getHostName());
            refreshToken.setUser(user);
            refreshToken.setCreatedAt(LocalDateTime.now());
            refreshToken.setCreatedBy(loginRequest.getUsername());
            refreshTokenRepository.save(refreshToken);
            RefreshTokenResponse refreshTokenResponse = RefreshTokenResponse.builder()
                    .refreshToken(refreshToken.getToken())
                    .accessToken(accessToken)
                    .tokenType("Bearer")
                    .build();
            response.setData(refreshTokenResponse);
        }
        return response;
    }

    @Override
    public ResponseEntityBase refreshToken(String token) {
        ResponseEntityBase response = new ResponseEntityBase(HttpStatus.OK.value(), null, null);
        RefreshToken refreshToken = refreshTokenRepository.findRefreshTokenByToken(token);
        if (Objects.nonNull(refreshToken)) {
            if (refreshToken.getExpiryDate().compareTo(Instant.now()) < 0) {
                refreshTokenRepository.delete(refreshToken);
                throw new ForbiddenException(MessageConstants.REFRESH_TOKEN_EXPIRED);
            }
            String accessToken = jwtUtils.generateTokenFromUsername(refreshToken.getUser().getUsername());
            RefreshTokenResponse refreshTokenResponse = RefreshTokenResponse.builder()
                    .refreshToken(refreshToken.getToken())
                    .accessToken(accessToken)
                    .tokenType("Bearer")
                    .build();
            response.setData(refreshTokenResponse);
        } else {
            throw new ResourceNotFoundException("Refresh token", token);
        }
        return response;
    }

    @Override
    public ResponseEntityBase logout(HttpServletRequest request) {
        ResponseEntityBase response = new ResponseEntityBase(HttpStatus.OK.value(), null, null);
        if (request.getHeader("Authorization") == null || !request.getHeader("Authorization").startsWith("Bearer ")) {
            response.setStatusCode(401);
            response.setErrorMessage(MessageConstants.LOGOUT_FAILED);
        }
        SecurityContextHolder.clearContext();
        response.setData(MessageConstants.LOGOUT_SUCCESS);
        return response;
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
    public ResponseEntityBase changePassword(ChangePasswordRequest changePasswordRequest) {
        ResponseEntityBase response = new ResponseEntityBase(HttpStatus.OK.value(), null, null);
        User currentUser = getCurrentUser();
        String username = currentUser.getUsername();
        if (currentUser != null && currentUser.getStatus().equals(EUserStatus.BANNED)) {
            throw new UnauthorizedException("Account with username '" +username+ "' is permanent lock. Please contact to admin.");
        }
        if (bruteForceProtectionService.isChangePasswordLocked(username)) {
            currentUser.setStatus(EUserStatus.BANNED);
            userRepository.save(currentUser);
            throw new UnauthorizedException("Account with username '" +username+ "' is permanent lock. Please contact to admin.");
        }
        if (passwordEncoder.matches(changePasswordRequest.getCurrentPassword(), currentUser.getPassword())) {
            if (!changePasswordRequest.getNewPassword().equals(changePasswordRequest.getConfirmPassword())) {
                throw new BadRequestException(MessageConstants.NOT_MATCH_PASSWORD);
            }
            currentUser.setPassword(passwordEncoder.encode(changePasswordRequest.getNewPassword()));
            currentUser.setUpdatedBy(username);
            userRepository.save(currentUser);
            bruteForceProtectionService.passwordChangeSucceeded(username);
            response.setData(MessageConstants.CHANGE_PASSWORD_SUCCESS);
            return response;
        } else {
            bruteForceProtectionService.passwordChangeFailed(username);
            throw new UnauthorizedException(MessageConstants.INCORRECT_CURRENT_PASSWORD);
        }
    }

    @Override
    public ResponseEntityBase forgotPassword(String email) throws MessagingException{
        ResponseEntityBase response = new ResponseEntityBase(HttpStatus.OK.value(), null, null);
        response.setData(resetPasswordEmail(email));
        return response;
    }

    @Override
    public ResponseEntityBase getUserByUsername(String username) {
        ResponseEntityBase response = new ResponseEntityBase(HttpStatus.OK.value(), null, null);
        UserInfoResponse userInfoResponse = getUser(userRepository.findUserByUsername(username));
        response.setData(userInfoResponse);
        return response;
    }

    @Override
    public ResponseEntityBase getUserInfo() {
        ResponseEntityBase response = new ResponseEntityBase(HttpStatus.OK.value(), null, null);
        User user = getCurrentUser();
        UserInfoResponse userInfoResponse = getUser(user);
        response.setData(userInfoResponse);
        return response;
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
                            "<li><b>Username</b> is " +username+ "</li>" +
                            "<li><b>Password</b> is " +password+ "</li>" +
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