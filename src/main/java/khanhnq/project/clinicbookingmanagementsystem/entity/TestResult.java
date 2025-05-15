package khanhnq.project.clinicbookingmanagementsystem.entity;

import jakarta.persistence.*;
import khanhnq.project.clinicbookingmanagementsystem.entity.enums.ETestResultStatus;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "test_result")
@Builder
public class TestResult extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long testResultId;

    @Column(nullable = false)
    private String result;

    private String note;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private ETestResultStatus status;

    @ManyToOne
    @JoinColumn(name = "lab_result_id")
    private LabResult labResult;

    @ManyToOne
    @JoinColumn(name = "test_package_attribute_id")
    private TestPackageAttribute testPackageAttribute;
}
