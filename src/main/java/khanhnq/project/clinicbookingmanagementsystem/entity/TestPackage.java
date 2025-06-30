package khanhnq.project.clinicbookingmanagementsystem.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import khanhnq.project.clinicbookingmanagementsystem.entity.enums.ETestPackageStatus;
import lombok.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "test_package")
@Builder
public class TestPackage extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long testPackageId;

    @Column(nullable = false)
    private String testPackageCode;

    @Column(nullable = false)
    private String testPackageName;

    @Column(nullable = false)
    private BigDecimal testPackagePrice;

    @NotBlank
    @Size(max = 255)
    @Column(nullable = false)
    private String testPreparationRequirements;

    @NotBlank
    @Size(max = 255)
    @Column(nullable = false)
    private String testDescription;

    @ManyToOne
    @JoinColumn(name = "service_id")
    private Services service;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private ETestPackageStatus status;

    @OneToMany(mappedBy = "testPackage", cascade = CascadeType.ALL)
    private List<LabResult> labResults;

    @ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE})
    @JoinTable(
            name = "test_package_attribute_mapping",
            joinColumns = @JoinColumn(name = "test_package_id"),
            inverseJoinColumns = @JoinColumn(name = "test_package_attribute_id")
    )
    private List<TestPackageAttribute> testPackageAttributes = new ArrayList<>();
}
