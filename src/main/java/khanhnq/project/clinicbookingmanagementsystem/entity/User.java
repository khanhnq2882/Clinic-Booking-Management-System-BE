package khanhnq.project.clinicbookingmanagementsystem.entity;

import java.util.*;
import java.util.stream.Collectors;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
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
@Table(name = "users",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "username"),
                @UniqueConstraint(columnNames = "email")
        })
public class User {
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
    @Size(max = 50)
    @Email
    @Column(nullable = false)
    private String email;

    @NotBlank
    @Size(max = 120)
    @Column(nullable = false)
    private String password;

    @Column(length = 50)
    @Size(max = 50)
    private String firstName;

    @Column(length = 50)
    @Size(max = 50)
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
    @Column(length = 20)
    private EUserStatus status;

    @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.PERSIST)
    @JoinTable(name = "users_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles = new HashSet<>();

    @Column(length = 50)
    @Size(max = 50)
    private String universityName;

    //    medicalLicense, avatar, medicalDegree;
    @OneToMany(mappedBy = "user", cascade = CascadeType.PERSIST)
    private Set<File> files = new HashSet<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private Set<Experience> experiences;

    @ManyToOne
    @JoinColumn(name = "specialization_id")
    private Specialization specialization;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private Set<WorkSchedule> workSchedules;

    @ManyToOne
    @JoinColumn(name = "clinic_id")
    private Clinic clinic;

    public Set<String> roleNames() {
        return roles.stream().map(role -> role.getRoleName().name()).collect(Collectors.toSet());
    }

    public String specializationName() {
        return specialization.getSpecializationName();
    }

}