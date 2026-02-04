package khanhnq.project.clinicbookingmanagementsystem.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "doctor_time_off")
@Builder
public class DoctorTimeOff extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long doctorTimeOffId;

    @Column(name = "time_off_start", nullable = false)
    private LocalDateTime timeOffStart;

    @Column(name = "time_off_end", nullable = false)
    private LocalDateTime timeOffend;

    @Column(nullable = false)
    private String reason;

    @Column(nullable = false)
    private boolean active = true;

    @ManyToOne
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;
}