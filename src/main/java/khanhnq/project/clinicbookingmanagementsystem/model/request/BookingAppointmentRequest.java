package khanhnq.project.clinicbookingmanagementsystem.model.request;

import jakarta.validation.constraints.NotNull;
import khanhnq.project.clinicbookingmanagementsystem.annotation.PhoneNumberConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BookingAppointmentRequest {
    @NotNull(message = "First name can't be empty.")
    @Length(max = 20, message = "First name has a maximum of 20 characters.")
    private String firstName;

    @NotNull(message = "Last name can't be empty.")
    @Length(max = 20, message = "Last name has a maximum of 20 characters.")
    private String lastName;

    private Date dateOfBirth;
    private int gender;

    @PhoneNumberConstraint
    private String phoneNumber;

    @NotNull(message = "Specific address can't be empty.")
    @Length(max = 50, message = "Specific address has a maximum of 50 characters.")
    private String specificAddress;


    private Long wardId;
    private Date appointmentDate;
    private Long workScheduleId;

    @NotNull(message = "Describe symptoms can't be empty.")
    @Length(max = 255, message = "Describe symptoms has a maximum of 255 characters.")
    private String describeSymptoms;
}
