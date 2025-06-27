package khanhnq.project.clinicbookingmanagementsystem.entity;

import jakarta.persistence.*;
import khanhnq.project.clinicbookingmanagementsystem.entity.enums.EConsultationServiceStatus;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "consultation_service")
@Builder
public class ConsultationService extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long consultationServiceId;

    @Column(nullable = false)
    private String consultationServiceName;

    @Column(nullable = false)
    private Double consultationServicePrice;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private Integer durationMinutes;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private EConsultationServiceStatus status;

    @ManyToOne
    @JoinColumn(name = "consultation_type_id")
    private ConsultationType consultationType;

    @ManyToOne
    @JoinColumn(name = "service_id")
    private Services service;
}
