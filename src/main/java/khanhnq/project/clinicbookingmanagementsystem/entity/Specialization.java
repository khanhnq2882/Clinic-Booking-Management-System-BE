package khanhnq.project.clinicbookingmanagementsystem.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "specialization")
@Builder
public class Specialization {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long specializationId;

    @Column(nullable = false)
    private String specializationName;

    @OneToMany(mappedBy = "specialization", cascade = CascadeType.ALL)
    private Set<ServiceCategory> serviceCategories;

//    @ManyToOne
//    @JoinColumn(name = "clinic_id")
//    private Clinic clinic;
}
