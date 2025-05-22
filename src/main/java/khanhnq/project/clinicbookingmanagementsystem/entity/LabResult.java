package khanhnq.project.clinicbookingmanagementsystem.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import khanhnq.project.clinicbookingmanagementsystem.entity.enums.ELabResultStatus;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "lab_result")
@Builder
public class LabResult extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long labResultId;

    @Column(nullable = false)
    private LocalDateTime sampleCollectionTime;

    @Column(nullable = false)
    private LocalDateTime sampleReceptionTime;

    @Column(nullable = false)
    private LocalDateTime testDate;

    @Column(nullable = false)
    private LocalDateTime resultDeliveryDate;

    @Column(nullable = false)
    private String note;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private ELabResultStatus status;

    @JsonBackReference
    @ManyToOne
    @JoinColumn(name = "test_package_id")
    private TestPackage testPackage;

    @JsonBackReference
    @ManyToOne
    @JoinColumn(name = "medical_record_id")
    private MedicalRecord medicalRecord;

    @ManyToOne
    @JoinColumn(name = "doctor_id")
    private Doctor doctorPrescribed;
}
