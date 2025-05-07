package khanhnq.project.clinicbookingmanagementsystem.service;

import khanhnq.project.clinicbookingmanagementsystem.model.request.BookingAppointmentRequest;
import khanhnq.project.clinicbookingmanagementsystem.model.request.UserProfileRequest;
import khanhnq.project.clinicbookingmanagementsystem.model.response.ResponseEntityBase;
import org.springframework.web.multipart.MultipartFile;

public interface UserService {
    ResponseEntityBase updateProfile(UserProfileRequest userProfileRequest, MultipartFile multipartFile);
    ResponseEntityBase getDoctorsBySpecialization(Long specializationId);
    ResponseEntityBase getDoctorDetails (Long doctorId);
    ResponseEntityBase bookingAppointment(BookingAppointmentRequest bookingAppointmentRequest);
    ResponseEntityBase updateBookedAppointment (Long bookingId, BookingAppointmentRequest bookingAppointmentRequest);
    ResponseEntityBase cancelAppointment(Long bookingId);
    ResponseEntityBase getAllBookings(int page, int size, String[] sorts);
}