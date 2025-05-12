package khanhnq.project.clinicbookingmanagementsystem.entity.eav;

import jakarta.persistence.*;
import khanhnq.project.clinicbookingmanagementsystem.entity.LabResult;
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
