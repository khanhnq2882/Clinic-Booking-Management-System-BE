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

    @Column(nullable = false)
    private String medicalHistory;

    @Column(nullable = false)
    private String allergies;

    @Column(nullable = false)
    private String diagnosis;

    @Column(nullable = false)
    private String treatmentPlan;

    @Column(nullable = false)
    private String prescribedMedications;

    @Column(nullable = false)
    private String followUpInstructions;

    @Column(nullable = false)
    private LocalDateTime nextAppointmentDate;

    @Column(nullable = false)
    private String consultationNotes;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private EMedicalRecordStatus status;

    @OneToMany(mappedBy = "medicalRecord", cascade = CascadeType.ALL)
    private List<LabResult> labResults = new ArrayList<>();

    @OneToOne(cascade = CascadeType.PERSIST, fetch = FetchType.EAGER)
    @JoinColumn(name = "booking_id")
    private Booking booking;
}
