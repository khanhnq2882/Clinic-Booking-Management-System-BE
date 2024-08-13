package khanhnq.project.clinicbookingmanagementsystem.entity.eav;

import jakarta.persistence.*;
import khanhnq.project.clinicbookingmanagementsystem.entity.BaseEntity;
import lombok.*;

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

    private String testPackageName;
}
