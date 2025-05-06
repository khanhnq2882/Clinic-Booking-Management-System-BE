package khanhnq.project.clinicbookingmanagementsystem.exception.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import khanhnq.project.clinicbookingmanagementsystem.model.response.ResponseEntityBase;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;;
import org.springframework.stereotype.Component;
import java.io.IOException;

@Component
public class CustomBadCredentialHandler implements AuthenticationEntryPoint {
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse httpServletResponse, AuthenticationException authException) throws IOException, ServletException {
        httpServletResponse.setStatus(HttpStatus.UNAUTHORIZED.value());
        httpServletResponse.setContentType("application/json;charset=UTF-8");
        httpServletResponse.setHeader("cbms-error-reason", "Authentication failed.");
        String message = (authException != null && authException.getMessage() != null) ?
                authException.getMessage() : "Authentication failed.";
        ResponseEntityBase response = new ResponseEntityBase(HttpStatus.UNAUTHORIZED.value(), message, null);
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonString = objectMapper.writeValueAsString(response);
        httpServletResponse.getWriter().write(jsonString);
    }
}
