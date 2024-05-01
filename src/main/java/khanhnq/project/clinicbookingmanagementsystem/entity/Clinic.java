package khanhnq.project.clinicbookingmanagementsystem.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "clinic")
@Builder
public class Clinic extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long clinicId;

    @OneToMany(mappedBy = "clinic", cascade = CascadeType.PERSIST)
    private Set<Address> addresses = new HashSet<>();

    @Column(nullable = false)
    private String phoneNumber;

    @Column(nullable = false)
    private LocalTime startWorkingHours;

    @Column(nullable = false)
    private LocalTime endWorkingHours;

    @OneToMany(mappedBy = "clinic", cascade = CascadeType.PERSIST)
    private Set<File> files = new HashSet<>();

    @OneToMany(mappedBy = "clinic", cascade = CascadeType.ALL)
    private Set<User> users = new HashSet<>();

}
