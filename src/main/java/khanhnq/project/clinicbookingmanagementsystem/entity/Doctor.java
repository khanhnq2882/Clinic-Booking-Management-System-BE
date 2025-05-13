package khanhnq.project.clinicbookingmanagementsystem.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import khanhnq.project.clinicbookingmanagementsystem.entity.enums.EEducationLevel;
import lombok.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "doctor")
@Builder
public class Doctor extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long doctorId;

    @Size(max = 255)
    private String biography;

    @Size(max = 255)
    private String careerDescription;

    @Enumerated(EnumType.STRING)
    private EEducationLevel educationLevel;

    @ManyToOne
    @JoinColumn(name = "specialization_id")
    private Specialization specialization;

    @OneToMany(mappedBy = "doctor", cascade = CascadeType.ALL)
    private Set<WorkExperience> workExperiences = new HashSet<>();

    @OneToMany(mappedBy = "doctor", cascade = CascadeType.ALL)
    private List<DaysOfWeek> daysOfWeeks = new ArrayList<>();

    @OneToMany(mappedBy = "doctorPrescribed", cascade = CascadeType.ALL)
    private List<LabResult> labResults;

    @OneToOne(cascade = CascadeType.PERSIST, fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    private User user;
}
