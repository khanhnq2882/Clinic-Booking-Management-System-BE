package khanhnq.project.clinicbookingmanagementsystem.model.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MedicalRecordRequest {
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
    private Long bookingId;
}
