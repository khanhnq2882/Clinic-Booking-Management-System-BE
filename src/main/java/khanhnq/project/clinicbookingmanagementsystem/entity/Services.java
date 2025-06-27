package khanhnq.project.clinicbookingmanagementsystem.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import khanhnq.project.clinicbookingmanagementsystem.entity.enums.EServiceStatus;
import khanhnq.project.clinicbookingmanagementsystem.entity.enums.EServiceType;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "service")
@Builder
public class Services extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long serviceId;

    @Column(nullable = false)
    private String serviceName;

    @NotBlank
    @Size(max = 255)
    @Column(nullable = false)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private EServiceType serviceType;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private EServiceStatus status;

    @ManyToOne
    @JoinColumn(name = "specialization_id")
    private Specialization specialization;

    @OneToMany(mappedBy = "service", cascade = CascadeType.ALL)
    private List<TestPackage> testPackages = new ArrayList<>();

    @OneToMany(mappedBy = "service", cascade = CascadeType.ALL)
    private List<ImagingService> imagingServices = new ArrayList<>();

    @OneToMany(mappedBy = "service", cascade = CascadeType.ALL)
    private List<ConsultationService> consultationServices = new ArrayList<>();
}
