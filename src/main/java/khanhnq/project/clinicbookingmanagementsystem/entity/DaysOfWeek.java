package khanhnq.project.clinicbookingmanagementsystem.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
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

    @Column(nullable = false)
    private Date workingDay;

    @Column(nullable = false)
    private Double examinationFee;

    @OneToMany(mappedBy = "daysOfWeek", cascade = CascadeType.ALL)
    private List<WorkSchedule> workSchedules = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "doctor_id")
    private Doctor doctor;
}
