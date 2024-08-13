package khanhnq.project.clinicbookingmanagementsystem.entity.eav;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import khanhnq.project.clinicbookingmanagementsystem.entity.TestPackage;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "test_package_attribute_value")
@Builder
public class TestPackageAttributeValue {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long testPackageAttributeValueId;

    private String testName;

    private Float result;

    private String normalRange;

    private String unit;

    private String deviceName;

    @JsonBackReference
    @ManyToOne
    @JoinColumn(name = "test_package_attribute_id")
    private TestPackageAttribute testPackageAttribute;

    @JsonBackReference
    @ManyToOne
    @JoinColumn(name = "test_package_id")
    private TestPackage testPackage;
}
