package khanhnq.project.clinicbookingmanagementsystem.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import khanhnq.project.clinicbookingmanagementsystem.entity.enums.EImagingServiceStatus;
import khanhnq.project.clinicbookingmanagementsystem.entity.enums.EImagingServiceType;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "imaging_service")
@Builder
public class ImagingService extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long imagingServiceId;

    @Column(nullable = false, unique = true)
    private String imagingServiceCode;

    @Column(nullable = false)
    private String imagingServiceName;

    @Column(nullable = false)
    private Double imagingServicePrice;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private Integer estimatedMinutes;

    @NotBlank
    @Size(max = 255)
    @Column(nullable = false)
    private String preparationRequirements;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private EImagingServiceType imagingServiceType;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private EImagingServiceStatus status;

    @OneToMany(mappedBy = "imagingService")
    private List<MedicalImage> medicalImages = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "service_id")
    private Services service;
}