package khanhnq.project.clinicbookingmanagementsystem.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "work_experience")
@Builder
public class WorkExperience extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long workExperienceId;

    @Column(nullable = false)
    private String position;

    @Column(nullable = false)
    private String workSpecializationName;

    @Column(nullable = false)
    private String workPlace;

    @Column(nullable = false)
    private Integer yearOfStartWork;

    private Integer yearOfEndWork;

    private String description;

    @ManyToOne
    @JoinColumn(name = "doctor_id")
    private Doctor doctor;
}
