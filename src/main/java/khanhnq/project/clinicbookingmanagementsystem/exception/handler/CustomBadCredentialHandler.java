package khanhnq.project.clinicbookingmanagementsystem.exception.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import khanhnq.project.clinicbookingmanagementsystem.model.dto.ErrorResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.time.LocalDateTime;

@Component
public class CustomBadCredentialHandler implements AuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException authenticationException) throws IOException, ServletException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType("application/json;charset=UTF-8");
        response.setHeader("cbms-error-reason", "Authentication failed.");
        String message = (authenticationException != null && authenticationException.getMessage() != null) ?
                authenticationException.getMessage() : "Authentication failed.";
        ErrorResponseDTO errorResponseDto = ErrorResponseDTO.builder()
                .apiPath(request.getRequestURI())
                .errorCode(HttpStatus.UNAUTHORIZED)
                .errorMessage(message)
                .errorTime(LocalDateTime.now())
                .build();
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonString = objectMapper.writeValueAsString(errorResponseDto);
        response.getWriter().write(jsonString);
    }
}
