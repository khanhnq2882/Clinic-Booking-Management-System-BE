package khanhnq.project.clinicbookingmanagementsystem.schedule;

import khanhnq.project.clinicbookingmanagementsystem.repository.BookingRepository;
import lombok.AllArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class BookingCleanupJob {
    private final BookingRepository bookingRepository;

    @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Ho_Chi_Minh")
    public void deleteUnconfirmedBookingsAfter24Hours() {
        bookingRepository.deleteUnconfirmedBookingAfter24Hours(24);
    }

    @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Ho_Chi_Minh")
    public void deleteAllOldBookings() {
        bookingRepository.deleteOlderThanDays(14);
    }

}
