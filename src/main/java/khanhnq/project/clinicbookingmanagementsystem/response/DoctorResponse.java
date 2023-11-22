package khanhnq.project.clinicbookingmanagementsystem.response;

import lombok.*;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DoctorResponse {
    private Long userId;
    private String userCode;
    private String email;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private AddressResponse doctorAddress;
    private String specializationName;
    private Set<WorkScheduleResponse> workSchedules;
    private String status;
}
