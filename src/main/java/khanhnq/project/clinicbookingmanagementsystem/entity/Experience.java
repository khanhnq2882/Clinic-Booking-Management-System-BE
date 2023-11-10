package khanhnq.project.clinicbookingmanagementsystem.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.Date;
import java.util.Set;

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

    @Column(nullable = false)
    private String position;

    @Column(nullable = false)
    private Date startWork;

    @Column(nullable = false)
    private Date endWork;

    @Column(nullable = false)
    private String detailedDescription;

    @OneToMany(mappedBy = "experience", cascade = CascadeType.ALL)
    private Set<Specialization> specializations;

    @OneToOne(cascade = CascadeType.PERSIST, fetch = FetchType.EAGER)
    @JoinColumn(name = "clinic_id")
    private Clinic clinic;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}
