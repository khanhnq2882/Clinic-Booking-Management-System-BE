package khanhnq.project.clinicbookingmanagementsystem.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import khanhnq.project.clinicbookingmanagementsystem.entity.enums.EGender;
import khanhnq.project.clinicbookingmanagementsystem.entity.enums.ENormalRangeType;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "normal_range")
@Builder
public class NormalRange extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long normalRangeId;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private EGender gender;

    @Column(name = "age_min")
    private Integer ageMin;

    @Column(name = "age_max")
    private Integer ageMax;

    @Enumerated(EnumType.STRING)
    @Column(name = "normal_range_type", length = 20)
    private ENormalRangeType normalRangeType;

    @Column(name = "min_value")
    private Double minValue;

    @Column(name = "max_value")
    private Double maxValue;

    @Column(name = "expected_value")
    private String expectedValue;

    @Column(name = "normal_text")
    private String normalText;

    @JsonBackReference
    @ManyToOne
    @JoinColumn(name = "test_package_attribute_id")
    private TestPackageAttribute testPackageAttribute;
}
