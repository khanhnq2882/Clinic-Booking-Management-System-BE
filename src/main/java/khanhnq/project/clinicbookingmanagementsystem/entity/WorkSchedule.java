package khanhnq.project.clinicbookingmanagementsystem.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalTime;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "work_schedule")
@Builder
public class WorkSchedule extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long workScheduleId;

    @Column(name = "start_time", columnDefinition = "TIME")
    private LocalTime startTime;

    @Column(name = "end_time", columnDefinition = "TIME")
    private LocalTime endTime;

    @ManyToOne
    @JoinColumn(name = "day_of_week_id")
    private DaysOfWeek daysOfWeek;

    @OneToMany(mappedBy = "workSchedule", cascade = CascadeType.PERSIST)
    private Set<Booking> bookings;

}
