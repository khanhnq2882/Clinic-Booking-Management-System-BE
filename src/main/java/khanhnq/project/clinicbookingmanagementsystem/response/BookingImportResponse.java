package khanhnq.project.clinicbookingmanagementsystem.response;

import khanhnq.project.clinicbookingmanagementsystem.entity.Booking;
import lombok.*;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BookingImportResponse {
    private List<Booking> validBookings;
    private List<BookingExcelResponse> invalidBookings;
}
