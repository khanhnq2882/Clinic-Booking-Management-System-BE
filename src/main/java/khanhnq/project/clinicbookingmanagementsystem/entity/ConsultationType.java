package khanhnq.project.clinicbookingmanagementsystem.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "consultation_type")
@Builder
public class ConsultationType extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long consultationTypeId;

    @Column(nullable = false)
    private String consultationTypeCode;

    @Column(nullable = false)
    private String consultationTypeName;

    @Column(nullable = false)
    private String description;

    @OneToMany(mappedBy = "consultationType", cascade = CascadeType.ALL)
    private List<ConsultationService> consultationServices = new ArrayList<>();
}
