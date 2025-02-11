package khanhnq.project.clinicbookingmanagementsystem.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.DayOfWeek;
import java.util.Date;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "day_of_week")
@Builder
public class DaysOfWeek extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long dayOfWeekId;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private DayOfWeek dayOfWeek;

    @Column(nullable = false)
    private Date workingDay;

    @Column(nullable = false)
    private int numberOfShiftsPerDay;

    @OneToMany(mappedBy = "daysOfWeek", cascade = CascadeType.ALL)
    private List<WorkSchedule> workSchedules;

    @ManyToOne
    @JoinColumn(name = "doctor_id")
    private Doctor doctor;
}
