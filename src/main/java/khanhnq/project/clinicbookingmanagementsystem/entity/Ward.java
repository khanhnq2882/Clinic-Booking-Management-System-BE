package khanhnq.project.clinicbookingmanagementsystem.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "ward")
@Builder
public class Ward extends BaseEntity{
    @Id
    private Long wardId;

    @Column(nullable = false)
    private String wardName;

    @JsonBackReference
    @ManyToOne
    @JoinColumn(name = "district_id")
    private District district;

    @OneToMany(mappedBy = "ward", cascade = CascadeType.ALL)
    private Set<Address> addresses;

}