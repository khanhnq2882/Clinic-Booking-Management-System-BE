package khanhnq.project.clinicbookingmanagementsystem.exception.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import khanhnq.project.clinicbookingmanagementsystem.model.dto.ErrorResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.time.LocalDateTime;

@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException, ServletException {
        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType("application/json;charset=UTF-8");
        response.setHeader("cbms-denied-reason", "Authorization failed.");
        String message = (accessDeniedException != null && accessDeniedException.getMessage() != null) ?
                accessDeniedException.getMessage() : "Authorization failed.";
        ErrorResponseDTO errorResponseDto = ErrorResponseDTO.builder()
                .apiPath(request.getRequestURI())
                .errorCode(HttpStatus.FORBIDDEN)
                .errorMessage(message)
                .errorTime(LocalDateTime.now())
                .build();
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonString = objectMapper.writeValueAsString(errorResponseDto);
        response.getWriter().write(jsonString);
    }
}
