package khanhnq.project.clinicbookingmanagementsystem.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "test_result")
@Builder
public class TestResult {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long testResultId;

    private Float result;

    private String note;

    @ManyToOne
    @JoinColumn(name = "lab_result_id")
    private LabResult labResult;

    @ManyToOne
    @JoinColumn(name = "test_package_attribute_id")
    private TestPackageAttribute testPackageAttribute;
}
