package khanhnq.project.clinicbookingmanagementsystem.mapper;

import khanhnq.project.clinicbookingmanagementsystem.entity.Booking;
import khanhnq.project.clinicbookingmanagementsystem.request.BookingAppointmentRequest;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface BookingMapper {
    BookingMapper BOOKING_MAPPER = Mappers.getMapper(BookingMapper.class);

    Booking mapToBooking(BookingAppointmentRequest bookingAppointmentRequest);
}
