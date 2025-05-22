package khanhnq.project.clinicbookingmanagementsystem.entity;

import jakarta.persistence.*;
import khanhnq.project.clinicbookingmanagementsystem.entity.enums.EMedicalRecordStatus;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "medical_record")
@Builder
public class MedicalRecord extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long medicalRecordId;

    @Column(nullable = false)
    private LocalDateTime visitDate;

    @Column(nullable = false)
    private String reasonForVisit;

    private String medicalHistory;

    private String allergies;

    private String diagnosis;

    private String treatmentPlan;

    private String prescribedMedications;

    private String followUpInstructions;

    private LocalDateTime nextAppointmentDate;

    private String consultationNotes;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private EMedicalRecordStatus status;

    @OneToMany(mappedBy = "medicalRecord", cascade = CascadeType.ALL)
    private List<LabResult> labResults = new ArrayList<>();

    @OneToOne(cascade = CascadeType.PERSIST, fetch = FetchType.EAGER)
    @JoinColumn(name = "booking_id")
    private Booking booking;
}
