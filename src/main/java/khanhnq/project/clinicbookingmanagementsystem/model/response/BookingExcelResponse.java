package khanhnq.project.clinicbookingmanagementsystem.model.response;

import khanhnq.project.clinicbookingmanagementsystem.model.dto.BookingExcelDTO;
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
