package khanhnq.project.clinicbookingmanagementsystem.entity;

import jakarta.persistence.*;
import khanhnq.project.clinicbookingmanagementsystem.common.MapToJsonConverter;
import lombok.*;
import java.util.*;

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

    @Convert(converter = MapToJsonConverter.class)
    @Column(columnDefinition = "json")
    private Map<String, String> attributeMetadata;

    @ManyToMany(mappedBy = "testPackageAttributes")
    private List<TestPackage> testPackages = new ArrayList<>();
}
