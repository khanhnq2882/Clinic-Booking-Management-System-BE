//package khanhnq.project.clinicbookingmanagementsystem.entity;
//
//import com.fasterxml.jackson.annotation.JsonBackReference;
//import jakarta.persistence.*;
//import lombok.*;
//
//import java.util.List;
//
//@Entity
//@Getter
//@Setter
//@NoArgsConstructor
//@AllArgsConstructor
//@Table(name = "medical_record")
//@Builder
//public class MedicalRecord extends BaseEntity{
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long medicalRecordId;
//
//    @JsonBackReference
//    @ManyToOne
//    @JoinColumn(name = "user_id")
//    private User user;
//
//    @OneToMany(mappedBy = "medicalRecord", cascade = CascadeType.ALL)
//    private List<LabResult> labResults;
//
//}
