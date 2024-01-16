package khanhnq.project.clinicbookingmanagementsystem.response;

import khanhnq.project.clinicbookingmanagementsystem.dto.BookingExcelDTO;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BookingExcelResponse {
    private int rowIndex;
    private BookingExcelDTO bookingExcelDTO = new BookingExcelDTO();
}
