package khanhnq.project.clinicbookingmanagementsystem.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "address")
@Builder
public class Address extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long addressId;

    @Column(nullable = false)
    @Size(max = 255)
    private String specificAddress;

    @JsonBackReference
    @ManyToOne
    @JoinColumn(name = "ward_id")
    private Ward ward;

    @JsonBackReference
    @ManyToOne
    @JoinColumn(name = "clinic_id")
    private Clinic clinic;
}