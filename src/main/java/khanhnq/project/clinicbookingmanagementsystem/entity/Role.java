package khanhnq.project.clinicbookingmanagementsystem.entity;

import jakarta.persistence.*;
import khanhnq.project.clinicbookingmanagementsystem.entity.enums.ERole;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "role")
@Builder
public class Role extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer roleId;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private ERole roleName;
}