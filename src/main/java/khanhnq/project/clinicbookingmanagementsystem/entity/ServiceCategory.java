package khanhnq.project.clinicbookingmanagementsystem.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "service_category")
@Builder
public class ServiceCategory extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long serviceCategoryId;

    @Column(nullable = false)
    private String serviceCategoryName;

    @NotBlank
    @Size(max = 255)
    @Column(nullable = false)
    private String description;

    @ManyToOne
    @JoinColumn(name = "specialization_id")
    private Specialization specialization;

    @OneToMany(mappedBy = "serviceCategory", cascade = CascadeType.ALL)
    private Set<Services> services;

}
