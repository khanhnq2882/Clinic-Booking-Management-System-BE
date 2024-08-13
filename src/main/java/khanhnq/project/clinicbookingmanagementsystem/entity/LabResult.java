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

    private LocalDateTime testDate;

    private LocalDateTime resultDeliveryDate;

    private String note;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private ELabResultStatus eLabResultStatus;

    @JsonBackReference
    @ManyToOne
    @JoinColumn(name = "test_package_id")
    private TestPackage testPackage;

    @JsonBackReference
    @ManyToOne
    @JoinColumn(name = "medical_record_id")
    private MedicalRecord medicalRecord;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User doctorPrescribed;
}
