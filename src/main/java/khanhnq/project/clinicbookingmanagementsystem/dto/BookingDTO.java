package khanhnq.project.clinicbookingmanagementsystem.dto;

import khanhnq.project.clinicbookingmanagementsystem.response.AddressResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalTime;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookingDTO {
    private Long bookingId;
    private String bookingCode;
    private String firstName;
    private String lastName;
    private Date dateOfBirth;
    private int gender;
    private String phoneNumber;
    private AddressResponse userAddress;
    private Date appointmentDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private String specialization;
    private String describeSymptoms;
    private String status;
}
