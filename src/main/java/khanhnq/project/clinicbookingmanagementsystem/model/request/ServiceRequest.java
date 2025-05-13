package khanhnq.project.clinicbookingmanagementsystem.model.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ServiceRequest {
    private Long specializationId;

    @NotNull(message = "Service name can't be empty.")
    @Length(max = 50, message = "Service name has a maximum of 50 characters.")
    private String serviceName;

    private Double servicePrice;

    @NotNull(message = "Description can't be empty.")
    @Length(max = 255, message = "Description has a maximum of 255 characters.")
    private String description;
}
