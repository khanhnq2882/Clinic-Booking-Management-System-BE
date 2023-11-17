package khanhnq.project.clinicbookingmanagementsystem.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "district")
@Builder
public class District {
    @Id
    private Long districtId;

    @Column(nullable = false)
    private String districtName;

    @JsonBackReference
    @ManyToOne
    @JoinColumn(name = "city_id")
    private City city;

    @OneToMany(mappedBy = "district", cascade = CascadeType.ALL)
    private List<Ward> wards;
}