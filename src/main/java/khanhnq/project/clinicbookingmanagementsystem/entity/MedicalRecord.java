package khanhnq.project.clinicbookingmanagementsystem.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import khanhnq.project.clinicbookingmanagementsystem.entity.enums.EMedicalRecordStatus;
import lombok.*;
import java.time.LocalDateTime;
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
    private LocalDateTime admitDate;

    @Column(nullable = false)
    private LocalDateTime dischargeDate;

    @Column(nullable = false)
    private String bloodType;

    @Column(nullable = false)
    private String medicalHistory;

    @Column(nullable = false)
    private String medications;

    @Column(nullable = false)
    private String allergies;

    @Column(nullable = false)
    private String diagnosis;

    @Column(nullable = false)
    private String treatment;

    @Column(nullable = false)
    private String note;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private EMedicalRecordStatus status;

    @JsonBackReference
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(mappedBy = "medicalRecord", cascade = CascadeType.ALL)
    private List<LabResult> labResults;
}
