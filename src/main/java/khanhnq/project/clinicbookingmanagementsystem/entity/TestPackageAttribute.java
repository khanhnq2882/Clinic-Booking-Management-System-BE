package khanhnq.project.clinicbookingmanagementsystem.entity;

import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
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

    @Column(nullable = false)
    private String name;

    private String unit;

    @Valid
    @OneToMany(mappedBy = "testPackageAttribute", cascade = CascadeType.ALL, orphanRemoval = true)
    @NotEmpty(message = "Test package must have at least one normal range.")
    private List<NormalRange> normalRanges = new ArrayList<>();

    @Convert(converter = MapToJsonConverter.class)
    @Column(columnDefinition = "json")
    private Map<String, String> attributeMetadata;

    @ManyToMany(mappedBy = "testPackageAttributes")
    private List<TestPackage> testPackages = new ArrayList<>();
}
