package khanhnq.project.clinicbookingmanagementsystem.model.dto;

import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MedicalRecordDetailsDTO {
    private String bookingCode;
    private String firstName;
    private String lastName;
    private Date dateOfBirth;
    private String gender;
    private String phoneNumber;
    private String userAddress;
    private LocalDateTime visitDate;
    private String reasonForVisit;
    private String medicalHistory;
    private String allergies;
    private String diagnosis;
    private String treatmentPlan;
    private String prescribedMedications;
    private String followUpInstructions;
    private LocalDateTime nextAppointmentDate;
    private String consultationNotes;
    private String medicalRecordStatus;
    private List<LabResultDTO> labResults = new ArrayList<>();
}
