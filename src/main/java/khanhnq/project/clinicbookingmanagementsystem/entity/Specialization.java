package khanhnq.project.clinicbookingmanagementsystem.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "specialization")
@Builder
public class Specialization extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long specializationId;

    @Column(nullable = false)
    private String specializationName;

    @OneToMany(mappedBy = "specialization", cascade = CascadeType.ALL)
    private Set<Services> services;

    @OneToMany(mappedBy = "specialization", cascade = CascadeType.PERSIST)
    private Set<Doctor> doctors = new HashSet<>();
}
