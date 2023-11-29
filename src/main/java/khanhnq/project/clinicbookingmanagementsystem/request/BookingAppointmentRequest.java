package khanhnq.project.clinicbookingmanagementsystem.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BookingAppointmentRequest {
    private String firstName;
    private String lastName;
    private Date dateOfBirth;
    private int gender;
    private String phoneNumber;
    private String specificAddress;
    private Long wardId;
    private Date appointmentDate;
    private Long workScheduleId;
    private String describeSymptoms;
}
