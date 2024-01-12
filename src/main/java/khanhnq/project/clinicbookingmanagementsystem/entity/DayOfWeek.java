package khanhnq.project.clinicbookingmanagementsystem.entity;

import jakarta.persistence.*;
import khanhnq.project.clinicbookingmanagementsystem.entity.enums.EDayOfWeek;
import lombok.*;
import java.util.Set;
import java.util.TreeSet;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "day_of_week")
@Builder
public class DayOfWeek {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long dayOfWeekId;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private EDayOfWeek dayOfWeek;

    @OneToMany(mappedBy = "dayOfWeek", cascade = CascadeType.ALL)
    private Set<WorkSchedule> workSchedules = new TreeSet<>();

    private int NumberOfShiftsPerDay;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}
