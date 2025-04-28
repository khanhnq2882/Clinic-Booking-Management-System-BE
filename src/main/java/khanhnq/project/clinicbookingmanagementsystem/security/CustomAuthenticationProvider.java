package khanhnq.project.clinicbookingmanagementsystem.security;

import khanhnq.project.clinicbookingmanagementsystem.entity.User;
import khanhnq.project.clinicbookingmanagementsystem.entity.enums.EUserStatus;
import khanhnq.project.clinicbookingmanagementsystem.exception.UnauthorizedException;
import khanhnq.project.clinicbookingmanagementsystem.repository.UserRepository;
import khanhnq.project.clinicbookingmanagementsystem.security.services.BruteForceProtectionService;
import khanhnq.project.clinicbookingmanagementsystem.security.services.UserDetailsServiceImpl;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import java.util.Objects;

@Component
@Slf4j
@AllArgsConstructor
public class CustomAuthenticationProvider implements AuthenticationProvider {

    private UserRepository userRepository;

    private BruteForceProtectionService bruteForceProtectionService;

    private UserDetailsServiceImpl userDetailsService;

    private BeanConfig beanConfig;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = authentication.getName();
        String password = (String) authentication.getCredentials();
        User user = userRepository.findUserByUsername(username);
        if (Objects.nonNull(user)) {
            if (user.getStatus().equals(EUserStatus.BANNED))
                throw new UnauthorizedException("Account with username '" +username+ "' is permanent lock. Please contact to admin.");
        } else {
            throw new UsernameNotFoundException("Account with username '" +username+ "' is not found.");
        }
        if (bruteForceProtectionService.isPermanentlyLocked(username)) {
            user.setStatus(EUserStatus.BANNED);
            userRepository.save(user);
            throw new UnauthorizedException("Account with username '" +username+ "' is permanent lock. Please contact to admin.");
        }
        if (bruteForceProtectionService.isBlocked(username)) {
            throw new UnauthorizedException("Account temporarily locked due to 5 incorrect password attempts. Try again in 5 minutes.");
        }
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        if (!beanConfig.passwordEncoder().matches(password, user.getPassword())) {
            bruteForceProtectionService.loginFailed(username);
            throw new UnauthorizedException("Login failed due to incorrect password entered. Try again.");
        }
        bruteForceProtectionService.loginSucceeded(username);
        return new UsernamePasswordAuthenticationToken(userDetails, password, userDetails.getAuthorities());
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }
}
