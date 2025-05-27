package khanhnq.project.clinicbookingmanagementsystem.model.dto;

import lombok.*;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Objects;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MedicalRecordInfoDTO {
    private Long medicalRecordId;
    private Long bookingId;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MedicalRecordInfoDTO that)) return false;
        return Objects.equals(medicalRecordId, that.medicalRecordId) &&
                Objects.equals(bookingId, that.bookingId) &&
                Objects.equals(firstName, that.firstName) &&
                Objects.equals(lastName, that.lastName) &&
                Objects.equals(dateOfBirth, that.dateOfBirth) &&
                Objects.equals(gender, that.gender) &&
                Objects.equals(phoneNumber, that.phoneNumber) &&
                Objects.equals(userAddress, that.userAddress) &&
                Objects.equals(visitDate, that.visitDate) &&
                Objects.equals(reasonForVisit, that.reasonForVisit) &&
                Objects.equals(medicalHistory, that.medicalHistory) &&
                Objects.equals(allergies, that.allergies) &&
                Objects.equals(diagnosis, that.diagnosis) &&
                Objects.equals(treatmentPlan, that.treatmentPlan) &&
                Objects.equals(prescribedMedications, that.prescribedMedications) &&
                Objects.equals(followUpInstructions, that.followUpInstructions) &&
                Objects.equals(nextAppointmentDate, that.nextAppointmentDate) &&
                Objects.equals(consultationNotes, that.consultationNotes) &&
                Objects.equals(medicalRecordStatus, that.medicalRecordStatus);
    }

    @Override
    public int hashCode() {
        return Objects.hash(medicalRecordId, bookingId, firstName, lastName,
                dateOfBirth, gender, phoneNumber, userAddress, visitDate,
                reasonForVisit, medicalHistory, allergies, diagnosis, treatmentPlan,
                prescribedMedications, followUpInstructions, nextAppointmentDate,
                consultationNotes, medicalRecordStatus);
    }
}
