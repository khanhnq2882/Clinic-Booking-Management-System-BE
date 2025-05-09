package khanhnq.project.clinicbookingmanagementsystem.model.response;

import khanhnq.project.clinicbookingmanagementsystem.model.projection.BookingDetailsInfoProjection;
import lombok.*;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BookingResponse {
    private long totalItems;
    private List<BookingDetailsInfoProjection> bookings;
    private long totalPages;
    private long currentPage;
}
