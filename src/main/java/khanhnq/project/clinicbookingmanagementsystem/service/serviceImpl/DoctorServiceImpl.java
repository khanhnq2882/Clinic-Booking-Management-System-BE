package khanhnq.project.clinicbookingmanagementsystem.service.serviceImpl;

import jakarta.transaction.Transactional;
import khanhnq.project.clinicbookingmanagementsystem.common.MessageConstants;
import khanhnq.project.clinicbookingmanagementsystem.entity.enums.*;
import khanhnq.project.clinicbookingmanagementsystem.exception.ResourceNotFoundException;
import khanhnq.project.clinicbookingmanagementsystem.mapper.LabResultMapper;
import khanhnq.project.clinicbookingmanagementsystem.mapper.MedicalRecordMapper;
import khanhnq.project.clinicbookingmanagementsystem.model.projection.BookingDetailsInfoProjection;
import khanhnq.project.clinicbookingmanagementsystem.model.dto.SpecializationDTO;
import khanhnq.project.clinicbookingmanagementsystem.model.dto.WorkScheduleDTO;
import khanhnq.project.clinicbookingmanagementsystem.exception.SystemException;
import khanhnq.project.clinicbookingmanagementsystem.exception.ForbiddenException;
import khanhnq.project.clinicbookingmanagementsystem.mapper.WorkExperienceMapper;
import khanhnq.project.clinicbookingmanagementsystem.mapper.WorkScheduleMapper;
import khanhnq.project.clinicbookingmanagementsystem.model.projection.BookingTimeInfoProjection;
import khanhnq.project.clinicbookingmanagementsystem.model.request.*;
import khanhnq.project.clinicbookingmanagementsystem.entity.*;
import khanhnq.project.clinicbookingmanagementsystem.model.response.BookingResponse;
import khanhnq.project.clinicbookingmanagementsystem.model.response.ResponseEntityBase;
import khanhnq.project.clinicbookingmanagementsystem.repository.*;
import khanhnq.project.clinicbookingmanagementsystem.service.AuthService;
import khanhnq.project.clinicbookingmanagementsystem.service.DoctorService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class DoctorServiceImpl implements DoctorService {
    private final AuthService authService;
    private final SpecializationRepository specializationRepository;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final DoctorRepository doctorRepository;
    private final WorkScheduleRepository workScheduleRepository;
    private final DayOfWeekRepository dayOfWeekRepository;
    private final WorkExperienceRepository workExperienceRepository;
    private final MedicalRecordRepository medicalRecordRepository;
    private final LabResultRepository labResultRepository;
    private final TestPackageRepository testPackageRepository;
    private final TestPackageAttributeRepository testPackageAttributeRepository;
    private final TestResultRepository testResultRepository;
    private final NormalRangeRepository normalRangeRepository;
    private final CommonServiceImpl commonServiceImpl;

    @Override
    public ResponseEntityBase updateProfile(UserProfileRequest userProfileRequest, MultipartFile avatar) {
        ResponseEntityBase response = new ResponseEntityBase(HttpStatus.OK.value(), null, null);
        User currentUser = authService.getCurrentUser();
        commonServiceImpl.updateProfile(userProfileRequest, currentUser, avatar);
        userRepository.save(currentUser);
        response.setData(MessageConstants.UPDATE_PROFILE_SUCCESS);
        return response;
    }

    @Override
    public ResponseEntityBase updateDoctorInformation(DoctorInformationRequest doctorInformationRequest, MultipartFile specialtyDegree) {
        ResponseEntityBase response = new ResponseEntityBase(HttpStatus.OK.value(), null, null);
        User currentUser = authService.getCurrentUser();
        Optional<Specialization> specializationOptional =
                specializationRepository.findById(doctorInformationRequest.getSpecializationId());
        if (specializationOptional.isEmpty()) {
            throw new ResourceNotFoundException("Specialization id", doctorInformationRequest.getSpecializationId().toString());
        }
        Doctor doctor = doctorRepository.findDoctorByUserId(currentUser.getUserId());
        if (Objects.nonNull(doctor)) {
            List<WorkExperience> workExperiences =
                    workExperienceRepository.findWorkExperienceByDoctorId(doctor.getDoctorId());
            workExperienceRepository.deleteAll(workExperiences);
        }
        Doctor savedDoctor = Objects.nonNull(doctor) ? doctor : new Doctor();
        Set<WorkExperience> workExperiences = doctorInformationRequest.getWorkExperiences().stream()
                .map(workExperienceDTO -> {
                    WorkExperience workExperience =
                            WorkExperienceMapper.WORK_EXPERIENCE_MAPPER.mapToExperience(workExperienceDTO);
                    workExperience.setCreatedBy(currentUser.getUsername());
                    workExperience.setCreatedAt(LocalDateTime.now());
                    workExperience.setDoctor(savedDoctor);
                    return workExperience;
                }).collect(Collectors.toSet());
        savedDoctor.setSpecialization(specializationOptional.get());
        savedDoctor.setWorkExperiences(workExperiences);
        savedDoctor.setBiography(doctorInformationRequest.getBiography());
        savedDoctor.setCareerDescription(doctorInformationRequest.getCareerDescription());
        savedDoctor.setEducationLevel(validateEducationLevel(doctorInformationRequest.getEducationLevel()));
        savedDoctor.setUser(currentUser);
        savedDoctor.setUpdatedBy(currentUser.getUsername());
        savedDoctor.setUpdatedAt(LocalDateTime.now());
        doctorRepository.save(savedDoctor);
        if (Objects.nonNull(specialtyDegree)) {
            commonServiceImpl.uploadFile(specialtyDegree, "specialty-degree", currentUser);
        }
        response.setData(MessageConstants.UPDATE_DOCTOR_INFORMATION_SUCCESS);
        return response;
    }

    @Override
    @Transactional
    public ResponseEntityBase registerWorkSchedules(RegisterWorkScheduleRequest registerWorkSchedule) {
        ResponseEntityBase response = new ResponseEntityBase(HttpStatus.OK.value(), null, null);
        User currentUser = authService.getCurrentUser();
        Doctor doctor = doctorRepository.findDoctorByUserId(currentUser.getUserId());
        if (Objects.isNull(doctor.getSpecialization())) {
            throw new SystemException(MessageConstants.SPECIALIZATION_NOT_FOUND);
        }
        Date workingDay = registerWorkSchedule.getWorkingDay();
        LocalDate workingDayLocalDate = workingDay.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate nowLocalDate = new Date().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate startOfWeek = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate endOfWeek = LocalDate.now().with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
        if (workingDayLocalDate.isBefore(nowLocalDate) ||
                workingDayLocalDate.isBefore(startOfWeek) ||
                workingDayLocalDate.isAfter(endOfWeek)) {
            throw new SystemException(MessageConstants.INVALID_WORKING_DAY);
        }
        List<WorkScheduleDTO> newWorkSchedules = registerWorkSchedule.getWorkSchedules();
        DaysOfWeek existDayOfWeek = dayOfWeekRepository.getDayOfWeekByWorkingDay(doctor.getDoctorId(), registerWorkSchedule.getWorkingDay());
        DaysOfWeek newDayOfWeek = Objects.nonNull(existDayOfWeek) ? existDayOfWeek : new DaysOfWeek();
        Long specializationId = doctor.getSpecialization().getSpecializationId();
        List<Doctor> doctors = doctorRepository.getDoctorsBySpecializationId(specializationId).stream().filter(dt -> !dt.equals(doctor)).toList();
        List<WorkScheduleDTO> invalidWorkSchedules = invalidWorkSchedules(doctors, registerWorkSchedule).stream().toList();
        List<WorkScheduleDTO> filterValidWorkSchedules = newWorkSchedules
                .stream()
                .filter(workScheduleDTO -> !invalidWorkSchedules.contains(workScheduleDTO))
                .toList();
        List<WorkSchedule> workSchedulesNotInBooking = workScheduleRepository.getWorkSchedulesNotInBooking(
                doctor.getDoctorId(), registerWorkSchedule.getWorkingDay());
        if (workSchedulesNotInBooking.size() > 0) {
            workScheduleRepository.deleteAll(workSchedulesNotInBooking);
            List<WorkSchedule> workSchedulesInBooking = workScheduleRepository.getWorkSchedulesInBooking(
                    doctor.getDoctorId(), registerWorkSchedule.getWorkingDay());
            filterValidWorkSchedules = filterValidWorkSchedules.stream()
                    .filter(workScheduleDTO -> workSchedulesInBooking.stream()
                            .noneMatch(workSchedule ->
                                    workScheduleDTO.getStartTime().equals(workSchedule.getStartTime()) &&
                                            workScheduleDTO.getEndTime().equals(workSchedule.getEndTime())
                            )
                    ).toList();
        }
        List<WorkSchedule> validWorkSchedules = filterValidWorkSchedules
                .stream()
                .map(workScheduleDTO -> {
                        WorkSchedule workSchedule = WorkScheduleMapper.WORK_SCHEDULE_MAPPER.mapToWorkSchedule(workScheduleDTO);
                        workSchedule.setDaysOfWeek(newDayOfWeek);
                        workSchedule.setCreatedBy(currentUser.getUsername());
                        workSchedule.setCreatedAt(LocalDateTime.now());
                        return workSchedule;
                    })
                .toList();
        if (validWorkSchedules.size() == 0) {
            throw new SystemException(MessageConstants.ALL_WORK_SCHEDULES_INVALID);
        } else {
            newDayOfWeek.setWorkingDay(workingDay);
            newDayOfWeek.setDoctor(doctor);
            newDayOfWeek.getWorkSchedules().addAll(validWorkSchedules);
            newDayOfWeek.setWorkSchedules(newDayOfWeek.getWorkSchedules());
            newDayOfWeek.setCreatedBy(currentUser.getUsername());
            newDayOfWeek.setCreatedAt(LocalDateTime.now());
            doctor.getDaysOfWeeks().add(newDayOfWeek);
            doctor.setDaysOfWeeks(doctor.getDaysOfWeeks());
            doctorRepository.save(doctor);
            response.setData(workSchedulesMessage(invalidWorkSchedules, workingDay));
        }
        return response;
    }

    @Override
    public ResponseEntityBase confirmedBooking(Long bookingId) {
        ResponseEntityBase response = new ResponseEntityBase(HttpStatus.OK.value(), null, null);
        User currentUser = authService.getCurrentUser();
        Booking booking = validateChangeBookingStatus(bookingId);
        String bookingStatus = booking.getStatus().toString();
        if (!bookingStatus.equals("PENDING")) {
            throw new SystemException("Your appointment has been " + bookingStatus + " and cannot be confirmed.");
        }
        booking.setStatus(EBookingStatus.CONFIRMED);
        booking.setUpdatedBy(currentUser.getUsername());
        bookingRepository.save(booking);
        response.setData(MessageConstants.CONFIRM_BOOKING_SUCCESS);
        return response;
    }

    @Override
    public ResponseEntityBase cancelledBooking(Long bookingId) {
        ResponseEntityBase response = new ResponseEntityBase(HttpStatus.OK.value(), null, null);
        User currentUser = authService.getCurrentUser();
        Booking booking = validateChangeBookingStatus(bookingId);
        String bookingStatus = booking.getStatus().toString();
        if (!bookingStatus.equals("PENDING") && !bookingStatus.equals("CONFIRMED")) {
            throw new SystemException("Your appointment has been " + bookingStatus + " and cannot be cancelled.");
        }
        booking.setStatus(EBookingStatus.CANCELLED);
        booking.setUpdatedBy(currentUser.getUsername());
        bookingRepository.save(booking);
        response.setData(MessageConstants.CANCELED_BOOKING_SUCCESS);
        return response;
    }

    @Override
    public ResponseEntityBase completedBooking(Long bookingId) {
        ResponseEntityBase response = new ResponseEntityBase(HttpStatus.OK.value(), null, null);
        User currentUser = authService.getCurrentUser();
        Booking booking = validateChangeBookingStatus(bookingId);
        String bookingStatus = booking.getStatus().toString();
        if (!bookingStatus.equals("CONFIRMED") && !bookingStatus.equals("CANCELLED")) {
            throw new SystemException("Your appointment has been " + bookingStatus + " and cannot be cancelled.");
        }
        booking.setStatus(EBookingStatus.COMPLETED);
        booking.setUpdatedBy(currentUser.getUsername());
        bookingRepository.save(booking);
        response.setData(MessageConstants.COMPLETED_BOOKING_SUCCESS);
        return response;
    }

    @Override
    public ResponseEntityBase getAllBookings(int page, int size, String[] sorts) {
        ResponseEntityBase response = new ResponseEntityBase(HttpStatus.OK.value(), null, null);
        User currentUser = authService.getCurrentUser();
        Doctor doctor = doctorRepository.findDoctorByUserId(currentUser.getUserId());
        if (Objects.nonNull(doctor)) {
            Pageable pageable = commonServiceImpl.pagingSort(page, size, sorts);
            Page<BookingDetailsInfoProjection> bookingDetailsPage =
                    bookingRepository.getBookingDetailsByDoctorId(doctor.getDoctorId(), pageable);
            BookingResponse bookingResponse = BookingResponse.builder()
                    .totalItems(bookingDetailsPage.getTotalElements())
                    .totalPages(bookingDetailsPage.getTotalPages())
                    .currentPage(bookingDetailsPage.getNumber())
                    .bookings(bookingDetailsPage.getContent())
                    .build();
            response.setData(bookingResponse);
        }
        return response;
    }

    @Override
    public ResponseEntityBase addMedicalRecord(MedicalRecordRequest medicalRecordRequest) {
        ResponseEntityBase response = new ResponseEntityBase(HttpStatus.OK.value(), null, null);
        User currentUser = authService.getCurrentUser();
        boolean checkAdmin = currentUser.getRoles().stream().anyMatch(role -> role.getRoleName().equals(ERole.ROLE_ADMIN));
        Doctor doctor = doctorRepository.findDoctorByUserId(currentUser.getUserId());
        Long bookingId = medicalRecordRequest.getBookingId();
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(()
                -> new ResourceNotFoundException("Booking id", bookingId.toString()));
        if (medicalRecordRepository.findMedicalRecordByBooking(bookingId) == null) {
            if (!booking.getStatus().equals(EBookingStatus.CONFIRMED)) {
                throw new SystemException("Booking status is "+booking.getStatus().name()+". Booking status must be confirmed to be allowed to create medical records.");
            }
            long doctorId = bookingRepository.getDoctorIdByBookingId(bookingId);
            if (!checkAdmin && doctor != null && !doctor.getDoctorId().equals(doctorId)) {
                throw new ForbiddenException(MessageConstants.FORBIDDEN_ADD_MEDICAL_RECORD);
            }
            BookingTimeInfoProjection bookingTime = bookingRepository.getBookingInfoByBookingId(bookingId);
            LocalDateTime visitDate = medicalRecordRequest.getVisitDate();
            LocalDate workingDay = bookingTime.getWorkingDay().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            boolean checkVisitDate = isVisitDateValid(visitDate, workingDay, bookingTime.getStartTime(), bookingTime.getEndTime());
            if (!checkVisitDate) {
                String formatWorkingDay = new SimpleDateFormat("dd-MM-yyyy").format(bookingTime.getWorkingDay());
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
                throw new SystemException("The visit date must be within day " + formatWorkingDay + " , booking time from "
                        + bookingTime.getStartTime().format(formatter) + " to "+ bookingTime.getEndTime().format(formatter) +".");
            }
            if (!isValidNextAppointmentDate(medicalRecordRequest.getNextAppointmentDate())) {
                throw new SystemException(MessageConstants.ERROR_NEXT_APPOINTMENT_DATE);
            }
            MedicalRecord medicalRecord = MedicalRecordMapper.MEDICAL_RECORD_MAPPER.mapToMedicalRecord(medicalRecordRequest);
            medicalRecord.setBooking(booking);
            medicalRecord.setStatus(EMedicalRecordStatus.CREATED);
            medicalRecord.setCreatedAt(LocalDateTime.now());
            medicalRecord.setCreatedBy(currentUser.getUsername());
            medicalRecordRepository.save(medicalRecord);
            response.setData(MessageConstants.ADD_MEDICAL_RECORD_SUCCESS);
            return response;
        } else {
            throw new SystemException("A medical record for booking " + booking.getBookingCode() + " has been created. Please update it.");
        }

    }

    @Override
    public ResponseEntityBase addLabResultToMedicalRecord(LabResultRequest labResultRequest) {
        ResponseEntityBase response = new ResponseEntityBase(HttpStatus.OK.value(), null, null);
        User currentUser = authService.getCurrentUser();
        Long medicalRecordId = labResultRequest.getMedicalRecordId();
        Long testPackageId = labResultRequest.getTestPackageId();
        Long doctorId = labResultRequest.getDoctorPrescribedId();
        MedicalRecord medicalRecord = medicalRecordRepository.findById(medicalRecordId).orElseThrow(
                () -> new ResourceNotFoundException("Medical Record ID", medicalRecordId.toString()));
        TestPackage testPackage = testPackageRepository.findById(testPackageId).orElseThrow(
                () -> new ResourceNotFoundException("Test Package ID", testPackageId.toString()));
        Doctor doctorPrescribed = doctorRepository.findById(doctorId).orElseThrow(
                () -> new ResourceNotFoundException("Doctor ID", doctorId.toString()));
        LabResult labResult = LabResultMapper.LAB_RESULT_MAPPER.mapToLabResult(labResultRequest);
        List<TestResult> testResults = new ArrayList<>();

        labResultRequest.getTestResults().forEach(testResultDTO -> {
            Long testPackageAttributeId = testResultDTO.getTestPackageAttributeId();
            TestPackageAttribute testPackageAttribute = testPackageAttributeRepository.findById(testPackageAttributeId).orElseThrow(
                    () -> new ResourceNotFoundException("Test Package Attribute ID", testPackageAttributeId.toString()));
            TestResult testResult = new TestResult();
            String result = testResultDTO.getResult();
            Booking booking = medicalRecord.getBooking();
            LocalDate dateOfBirth = booking.getDateOfBirth().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            LocalDate dateNow = LocalDate.now();
            int yearOld = Period.between(dateOfBirth, dateNow).getYears();
            String gender = booking.getGender() == 1 ? "MALE" : "FEMALE";
            List<NormalRange> normalRanges =
                    normalRangeRepository.getNormalRangesByTestPackageAttributeId(testPackageAttribute.getTestPackageAttributeId());
            normalRanges.forEach(normalRange -> {
                ENormalRangeType normalRangeType = normalRange.getNormalRangeType();
                Integer ageMin = normalRange.getAgeMin();
                Integer ageMax = normalRange.getAgeMax();
                Double minValue = normalRange.getMinValue();
                Double maxValue = normalRange.getMaxValue();
                Double equalValue = normalRange.getEqualValue();
                String expectedValue = normalRange.getExpectedValue();
                if (result != null && !result.equals("") && normalRange.getGender().name().equals(gender)) {
                    setStatusForTestResult(testResult, result, yearOld, ageMin,
                            ageMax, minValue, maxValue, equalValue,
                            expectedValue, normalRangeType);

                }
            });
        });

        labResult.setMedicalRecord(medicalRecord);
        labResult.setTestPackage(testPackage);
        labResult.setDoctorPrescribed(doctorPrescribed);
        labResult.setCreatedBy(currentUser.getUsername());
        labResult.setCreatedAt(LocalDateTime.now());
        labResultRepository.save(labResult);
        testResultRepository.saveAll(testResults);
        response.setData(MessageConstants.ADD_LAB_RESULT_TO_MEDICAL_RECORD_SUCCESS);
        return response;
    }

    public Booking validateChangeBookingStatus(Long bookingId) {
        User currentUser = authService.getCurrentUser();
        boolean checkAdmin = currentUser.getRoles().stream().anyMatch(role -> role.getRoleName().equals(ERole.ROLE_ADMIN));
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(() -> new ResourceNotFoundException("Booking id", bookingId.toString()));
        long doctorId = bookingRepository.getDoctorIdByBookingId(bookingId);
        Doctor doctor = doctorRepository.findDoctorByUserId(currentUser.getUserId());
        if (!checkAdmin && doctor != null && !doctor.getDoctorId().equals(doctorId)) {
            throw new ForbiddenException(MessageConstants.FORBIDDEN_CHANGE_BOOKING_STATUS);
        }
        return booking;
    }

    public boolean checkSpecializationExist(List<SpecializationDTO> specializations, SpecializationDTO specializationDTO) {
        boolean isExist = false;
        for (SpecializationDTO specialization : specializations) {
            if (specialization.equals(specializationDTO)) {
                isExist = true;
                break;
            }
        }
        return isExist;
    }

    public Set<WorkScheduleDTO> invalidWorkSchedules(List<Doctor> doctors, RegisterWorkScheduleRequest registerWorkSchedule) {
        Set<WorkScheduleDTO> invalidWorkSchedules = new HashSet<>();
        if (doctors.size() == 0) {
            List<WorkScheduleDTO> workSchedules = registerWorkSchedule.getWorkSchedules().stream()
                    .filter(newSchedule -> !isValidDuration(newSchedule.getStartTime(), newSchedule.getEndTime()))
                    .toList();
            invalidWorkSchedules.addAll(workSchedules);
        } else {
            doctors.forEach(doctor -> {
                List<WorkSchedule> existWorkSchedules =
                        workScheduleRepository.getWorkSchedulesByWorkingDay(doctor.getDoctorId(), registerWorkSchedule.getWorkingDay());
                List<WorkScheduleDTO> workScheduleDTOS = existWorkSchedules.stream().map(WorkScheduleMapper.WORK_SCHEDULE_MAPPER::mapToWorkScheduleDTO).toList();
                List<WorkScheduleDTO> filterWorkSchedules = registerWorkSchedule.getWorkSchedules().stream()
                        .filter(newSchedule -> !isValidDuration(newSchedule.getStartTime(), newSchedule.getEndTime()) ||
                                workScheduleDTOS.stream().anyMatch(existSchedule ->
                                        isTimeOverlap(newSchedule.getStartTime(), newSchedule.getEndTime(),
                                                existSchedule.getStartTime(), existSchedule.getEndTime())
                                )
                        ).toList();
                invalidWorkSchedules.addAll(filterWorkSchedules);
            });
        }
        return invalidWorkSchedules;
    }

    private boolean isTimeOverlap(LocalTime start1, LocalTime end1, LocalTime start2, LocalTime end2) {
        return start1.isBefore(end2) && start2.isBefore(end1);
    }

    private boolean isValidDuration(LocalTime start, LocalTime end) {
        return Duration.between(start, end).toMinutes() == 30;
    }

    public String workSchedulesMessage(List<WorkScheduleDTO> invalidWorkSchedules, Date day) {
        String workSchedulesMessage;
        String workingDay = new SimpleDateFormat("dd-MM-yyyy").format(day);
        if (invalidWorkSchedules.size() == 0) {
            workSchedulesMessage = "Successfully registered work schedules for " + workingDay + ".";
        } else {
            StringBuilder invalidMessage = new StringBuilder();
            int size = invalidWorkSchedules.size();
            for (int i=0; i<size; i++) {
                invalidMessage.append(invalidWorkSchedules.get(i).getStartTime()).append("-")
                        .append(invalidWorkSchedules.get(i).getEndTime()).append((i != size-1) ? ", " : "");
            }
            workSchedulesMessage = "Successfully registered for work schedules for "+ workingDay
                    + ".However, the following time slots could not be registered because they are already taken: " + invalidMessage
                    + ". Note: Each work schedule time should only be 30 minutes.";
        }
        return workSchedulesMessage;
    }

    private EEducationLevel validateEducationLevel(String educationLevel) {
        String enumValue = "";
        if (educationLevel.equalsIgnoreCase("BACHELOR")) {
            enumValue = "BACHELOR";
        } else if (educationLevel.equalsIgnoreCase("MASTER")) {
            enumValue = "MASTER";
        } else if (educationLevel.equalsIgnoreCase("DOCTOR")) {
            enumValue = "DOCTOR";
        } else if (educationLevel.equalsIgnoreCase("PROFESSOR")) {
            enumValue = "PROFESSOR";
        } else if (educationLevel.equalsIgnoreCase("ASSOCIATE_PROFESSOR")) {
            enumValue = "ASSOCIATE_PROFESSOR";
        } else {
            throw new ResourceNotFoundException("Education level", educationLevel);
        }
        return EEducationLevel.valueOf(enumValue);
    }

    public boolean isVisitDateValid(LocalDateTime visitDate, LocalDate workingDayDate,
                                    LocalTime startTime, LocalTime endTime) {
        LocalDate visitDateOnly = visitDate.toLocalDate();
        LocalTime visitTimeOnly = visitDate.toLocalTime();
        boolean sameDay = visitDateOnly.equals(workingDayDate);
        boolean withinTime = !visitTimeOnly.isBefore(startTime) && !visitTimeOnly.isAfter(endTime);
        return sameDay && withinTime;
    }

    public boolean isValidNextAppointmentDate(LocalDateTime nextAppointmentDate) {
        LocalDateTime currentDate = LocalDateTime.now();
        if (nextAppointmentDate.isBefore(currentDate)) {
            return false;
        }
        if (nextAppointmentDate.isBefore(currentDate.plusDays(7))) {
            return false;
        }
        return true;
    }

    private void setStatusForTestResult(TestResult testResult, String result, Integer yearOld,
                                        Integer ageMin, Integer ageMax, Double minValue,
                                        Double maxValue, Double equalValue,String expectedValue,
                                        ENormalRangeType normalRangeType) {
        Pattern doublePattern = Pattern.compile("^[+-]?(\\d+(\\.\\d{0,3})?|\\.\\d{1,3})$");
        Matcher rangeMatcher = doublePattern.matcher(result);
        if (!rangeMatcher.find()) return;
        Double doubleResult = Double.valueOf(result);
        boolean matchAge = (ageMin == null || yearOld >= ageMin) &&
                (ageMax == null || yearOld <= ageMax);
        if (!matchAge) return;
        switch (normalRangeType) {
            case RANGE:
                if (doubleResult >= minValue && doubleResult <= maxValue) {
                    testResult.setStatus(ETestResultStatus.NORMAL);
                } else if (doubleResult < minValue) {
                    testResult.setStatus(ETestResultStatus.LOW);
                } else {
                    testResult.setStatus(ETestResultStatus.HIGH);
                }
                break;
            case LESS_THAN:
                testResult.setStatus(doubleResult < maxValue ?
                        ETestResultStatus.NORMAL : ETestResultStatus.HIGH);
                break;
            case LESS_THAN_EQUAL:
                testResult.setStatus(doubleResult <= maxValue ?
                        ETestResultStatus.NORMAL : ETestResultStatus.HIGH);
                break;
            case GREATER_THAN:
                testResult.setStatus(doubleResult > minValue ?
                        ETestResultStatus.NORMAL : ETestResultStatus.LOW);
                break;
            case GREATER_THAN_EQUAL:
                testResult.setStatus(doubleResult >= minValue ?
                        ETestResultStatus.NORMAL : ETestResultStatus.LOW);
                break;
            case EQUAL:
                if (doubleResult.equals(minValue)) {
                    testResult.setStatus(ETestResultStatus.NORMAL);
                } else if (doubleResult < equalValue) {
                    testResult.setStatus(ETestResultStatus.LOW);
                } else {
                    testResult.setStatus(ETestResultStatus.HIGH);
                }
                break;
            case QUALITATIVE, SEMI_QUALITATIVE:
                if (expectedValue != null && expectedValue.equalsIgnoreCase(result)) {
                    testResult.setStatus(ETestResultStatus.NORMAL);
                } else {
                    testResult.setStatus(ETestResultStatus.ABNORMAL);
                }
                break;
            case TEXT:
                testResult.setStatus(ETestResultStatus.INCONCLUSIVE);
                break;
        }
    }

}
