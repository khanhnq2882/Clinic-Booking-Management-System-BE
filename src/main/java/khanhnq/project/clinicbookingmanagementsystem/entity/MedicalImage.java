package khanhnq.project.clinicbookingmanagementsystem.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import khanhnq.project.clinicbookingmanagementsystem.entity.enums.EResultStatus;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "medical_image")
@Builder
public class MedicalImage extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long medicalImageId;

    @Lob
    @Column(nullable = false)
    private String conclusion;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private LocalDateTime performedAt;

    @ManyToOne
    @JoinColumn(name = "doctor_id")
    private Doctor doctorPrescribed;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private EResultStatus status;

    @JsonBackReference
    @ManyToOne
    @JoinColumn(name = "imaging_service_id")
    private ImagingService imagingService;

    @JsonBackReference
    @ManyToOne
    @JoinColumn(name = "medical_record_id")
    private MedicalRecord medicalRecord;

    @OneToMany(mappedBy = "medicalImage", cascade = CascadeType.ALL)
    private List<File> medicalImageFiles = new ArrayList<>();
}
