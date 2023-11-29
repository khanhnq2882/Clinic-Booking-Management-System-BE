package khanhnq.project.clinicbookingmanagementsystem.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import khanhnq.project.clinicbookingmanagementsystem.entity.enums.EBookingStatus;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.util.Date;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "booking")
@Builder
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long bookingId;

//    @Column(nullable = false)
//    private String bookingCode;

    @Column(length = 50, nullable = false)
    @Size(max = 50)
    private String firstName;

    @Column(length = 50, nullable = false)
    @Size(max = 50)
    private String lastName;

    @Column(nullable = false)
    @DateTimeFormat(pattern = "dd-MM-yyyy")
    @Temporal(TemporalType.DATE)
    private Date dateOfBirth;

    @Column(length = 1, nullable = false)
    private int gender;

    @Column(length = 10, nullable = false)
    @Size(max = 10)
    private String phoneNumber;

    @OneToOne(cascade = CascadeType.PERSIST, fetch = FetchType.EAGER)
    @JoinColumn(name = "address_id")
    private Address address;

    @Column(nullable = false)
    @DateTimeFormat(pattern = "dd-MM-yyyy")
    @Temporal(TemporalType.DATE)
    private Date appointmentDate;

    @OneToOne(cascade = CascadeType.PERSIST, fetch = FetchType.EAGER)
    @JoinColumn(name = "work_schedule_id")
    private WorkSchedule workSchedule;

    @Column(nullable = false)
    private String describeSymptoms;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private EBookingStatus status;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

}
