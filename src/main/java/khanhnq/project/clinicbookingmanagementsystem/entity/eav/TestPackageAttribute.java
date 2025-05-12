package khanhnq.project.clinicbookingmanagementsystem.entity.eav;

import jakarta.persistence.*;
import khanhnq.project.clinicbookingmanagementsystem.entity.BaseEntity;
import khanhnq.project.clinicbookingmanagementsystem.entity.TestPackage;
import lombok.*;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "test_package_attribute")
@Builder
public class TestPackageAttribute extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long testPackageAttributeId;

    private String testName;
    private String unit;
    private String normalRange;
    private String method;
    private String deviceType;

    @ManyToMany(mappedBy = "attributes")
    private List<TestPackage> testPackages;
}
