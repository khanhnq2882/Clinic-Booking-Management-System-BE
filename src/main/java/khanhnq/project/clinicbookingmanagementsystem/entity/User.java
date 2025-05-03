package khanhnq.project.clinicbookingmanagementsystem.entity;

import java.util.*;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import khanhnq.project.clinicbookingmanagementsystem.entity.enums.EUserStatus;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "user",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "username"),
                @UniqueConstraint(columnNames = "email")
        })
public class User extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @NotBlank
    @Size(max = 10)
    @Column(nullable = false)
    private String userCode;

    @NotBlank
    @Size(max = 20)
    @Column(nullable = false)
    private String username;

    @NotBlank
    @Size(max = 30)
    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(length = 20)
    @Size(max = 20)
    private String firstName;

    @Column(length = 20)
    @Size(max = 20)
    private String lastName;

    @DateTimeFormat(pattern = "dd-MM-yyyy")
    @Temporal(TemporalType.DATE)
    private Date dateOfBirth;

    @Column(length = 1)
    private int gender;

    @Column(length = 10)
    @Size(max = 10)
    private String phoneNumber;

    @OneToOne(cascade = CascadeType.PERSIST, fetch = FetchType.EAGER)
    @JoinColumn(name = "address_id")
    private Address address;

    @Enumerated(EnumType.STRING)
    private EUserStatus status;

    @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.PERSIST)
    @JoinTable(name = "users_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles = new HashSet<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.PERSIST)
    private Set<File> files = new HashSet<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private Set<Booking> bookings = new HashSet<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<MedicalRecord> medicalRecords;

    @OneToMany(mappedBy = "doctorPrescribed", cascade = CascadeType.ALL)
    private List<LabResult> labResults;

    @Override
    public boolean equals(Object o) {
        if (Objects.isNull(o)) return false;
        User user = (User) o;
        return Objects.equals(this.getUserId(), user.getUserId());
    }
}