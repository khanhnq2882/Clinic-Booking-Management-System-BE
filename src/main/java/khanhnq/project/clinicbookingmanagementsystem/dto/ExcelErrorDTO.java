package khanhnq.project.clinicbookingmanagementsystem.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ExcelErrorDTO {
    private BookingExcelDTO bookingExcelDTO;
    private int indexRow;
    private String errorMessage;
}
