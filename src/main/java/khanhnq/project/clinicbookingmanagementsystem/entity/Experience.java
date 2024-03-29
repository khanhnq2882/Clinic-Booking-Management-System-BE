package khanhnq.project.clinicbookingmanagementsystem.entity;

import jakarta.persistence.*;
import khanhnq.project.clinicbookingmanagementsystem.entity.enums.ERoleDoctor;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "experience")
@Builder
public class Experience {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long experienceId;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private ERoleDoctor position;

    @Column(nullable = false)
    private String specialization;

    @Column(nullable = false)
    private String workPlace;

    @Column(nullable = false)
    private int startWork;

    @Column(nullable = false)
    private int endWork;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}
