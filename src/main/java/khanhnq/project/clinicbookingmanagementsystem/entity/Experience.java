package khanhnq.project.clinicbookingmanagementsystem.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "experience")
@Builder
public class Experience extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long experienceId;

    @Column(nullable = false)
    private String position;

    @Column(nullable = false)
    private String specialization;

    @Column(nullable = false)
    private String workPlace;

    @Column(nullable = false)
    private int yearOfStartWork;

    @Column(nullable = false)
    private int yearOfEndWork;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}
