//package khanhnq.project.clinicbookingmanagementsystem.entity;
//
//import com.fasterxml.jackson.annotation.JsonBackReference;
//import jakarta.persistence.*;
//import lombok.*;
//import java.time.LocalDateTime;
//
//@Entity
//@Getter
//@Setter
//@NoArgsConstructor
//@AllArgsConstructor
//@Table(name = "lab_result")
//@Builder
//public class LabResult extends BaseEntity{
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long labResultId;
//
//    private String labName;
//
//    private LocalDateTime labDate;
//
//    @OneToOne(cascade = CascadeType.PERSIST, fetch = FetchType.EAGER)
//    @JoinColumn(name = "service_id")
//    private Services service;
//
//    @JsonBackReference
//    @ManyToOne
//    @JoinColumn(name = "medical_record_id")
//    private MedicalRecord medicalRecord;
//}
