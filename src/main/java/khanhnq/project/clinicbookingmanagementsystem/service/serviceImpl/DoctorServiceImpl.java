package khanhnq.project.clinicbookingmanagementsystem.service.serviceImpl;

import jakarta.transaction.Transactional;
import khanhnq.project.clinicbookingmanagementsystem.common.MessageConstants;
import khanhnq.project.clinicbookingmanagementsystem.entity.enums.*;
import khanhnq.project.clinicbookingmanagementsystem.exception.ResourceNotFoundException;
import khanhnq.project.clinicbookingmanagementsystem.mapper.*;
import khanhnq.project.clinicbookingmanagementsystem.model.dto.*;
import khanhnq.project.clinicbookingmanagementsystem.model.projection.BookingDetailsInfoProjection;
import khanhnq.project.clinicbookingmanagementsystem.exception.SystemException;
import khanhnq.project.clinicbookingmanagementsystem.exception.ForbiddenException;
import khanhnq.project.clinicbookingmanagementsystem.model.projection.BookingTimeInfoProjection;
import khanhnq.project.clinicbookingmanagementsystem.model.projection.MedicalRecordDetailsProjection;
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
    public ResponseEntityBase addMedicalRecord(Long bookingId, MedicalRecordRequest medicalRecordRequest) {
        ResponseEntityBase response = new ResponseEntityBase(HttpStatus.OK.value(), null, null);
        User currentUser = authService.getCurrentUser();
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(()
                -> new ResourceNotFoundException("Booking id", bookingId.toString()));
        if (medicalRecordRepository.findMedicalRecordByBooking(bookingId) == null)
            throw new SystemException("A medical record for booking " + booking.getBookingCode() + " has been created. Please update it.");
        validateMedicalRecord(currentUser, booking, medicalRecordRequest);
        MedicalRecord medicalRecord = MedicalRecordMapper.MEDICAL_RECORD_MAPPER.mapToMedicalRecord(medicalRecordRequest);
        medicalRecord.setBooking(booking);
        medicalRecord.setStatus(EMedicalRecordStatus.CREATED);
        medicalRecord.setCreatedAt(LocalDateTime.now());
        medicalRecord.setCreatedBy(currentUser.getUsername());
        medicalRecordRepository.save(medicalRecord);
        response.setData(MessageConstants.ADD_MEDICAL_RECORD_SUCCESS);
        return response;
    }

    @Override
    public ResponseEntityBase updateMedicalRecord(Long medicalRecordId, MedicalRecordRequest medicalRecordRequest) {
        ResponseEntityBase response = new ResponseEntityBase(HttpStatus.OK.value(), null, null);
        User currentUser = authService.getCurrentUser();
        MedicalRecord medicalRecord = medicalRecordRepository.findById(medicalRecordId).orElseThrow(()
                -> new ResourceNotFoundException("Medical record id", medicalRecordId.toString()));
        Booking booking = medicalRecord.getBooking();
        validateMedicalRecord(currentUser, booking, medicalRecordRequest);
        MedicalRecordMapper.MEDICAL_RECORD_MAPPER.mapToMedicalRecord(medicalRecord, medicalRecordRequest);
        medicalRecord.setStatus(EMedicalRecordStatus.CREATED);
        medicalRecord.setUpdatedAt(LocalDateTime.now());
        medicalRecord.setUpdatedBy(currentUser.getUsername());
        medicalRecordRepository.save(medicalRecord);
        response.setData(MessageConstants.UPDATE_MEDICAL_RECORD_SUCCESS);
        return response;
    }

    @Override
    public ResponseEntityBase getAllMedicalRecord() {
        return null;
    }

    @Override
    public ResponseEntityBase addLabResultsToMedicalRecord(List<LabResultRequest> labResultRequests) {
        ResponseEntityBase response = new ResponseEntityBase(HttpStatus.OK.value(), null, null);
        User currentUser = authService.getCurrentUser();
        labResultRequests.forEach(labResultRequest -> {
            Long medicalRecordId = labResultRequest.getMedicalRecordId();
            Long testPackageId = labResultRequest.getTestPackageId();
            Long doctorId = labResultRequest.getDoctorPrescribedId();
            MedicalRecord medicalRecord = medicalRecordRepository.findById(medicalRecordId).orElseThrow(
                    () -> new ResourceNotFoundException("Medical Record ID", medicalRecordId.toString()));
            TestPackage testPackage = testPackageRepository.findById(testPackageId).orElseThrow(
                    () -> new ResourceNotFoundException("Test Package ID", testPackageId.toString()));
            if (!testPackage.getStatus().name().equals("ACTIVE")) {
                throw new SystemException(MessageConstants.ERROR_TEST_PACKAGE_NOT_ACTIVE);
            }
            Doctor doctorPrescribed = doctorRepository.findById(doctorId).orElseThrow(
                    () -> new ResourceNotFoundException("Doctor ID", doctorId.toString()));
            LocalDateTime sampleCollectionTime = labResultRequest.getSampleCollectionTime();
            LocalDateTime sampleReceptionTime = labResultRequest.getSampleReceptionTime();
            LocalDateTime testDate = labResultRequest.getTestDate();
            LocalDateTime resultDeliveryDate = labResultRequest.getResultDeliveryDate();
            if (sampleCollectionTime.isAfter(sampleReceptionTime)
                    || sampleReceptionTime.isAfter(testDate)
                    || testDate.isAfter(resultDeliveryDate)) {
                throw new SystemException(MessageConstants.ERROR_TEST_TIME_ORDER);
            }
            LabResult labResult = LabResultMapper.LAB_RESULT_MAPPER.mapToLabResult(labResultRequest);
            List<TestResult> testResults = new ArrayList<>();
            List<TestResultDTO> testResultDTOS = labResultRequest.getTestResults();
            Set<Long> attributesId = testResultDTOS.stream().map(TestResultDTO::getTestPackageAttributeId).collect(Collectors.toSet());
            if (attributesId.size() != testResultDTOS.size()) {
                throw new SystemException(MessageConstants.ERROR_ADD_RESULT_FOR_SAME_ATTRIBUTE);
            }
            testResultDTOS.forEach(testResultDTO -> {
                Long testPackageAttributeId = testResultDTO.getTestPackageAttributeId();
                TestPackageAttribute testPackageAttribute = testPackageAttributeRepository.findById(testPackageAttributeId).orElseThrow(
                        () -> new ResourceNotFoundException("Test Package Attribute ID", testPackageAttributeId.toString()));
                List<Long> testPackageAttributesId = testPackageAttributeRepository.getTestPackageAttributeIdsByTestPackageId(testPackageId);
                if (testPackageAttributesId.stream().noneMatch(id -> id.equals(testPackageAttributeId))) {
                    throw new SystemException(MessageConstants.ERROR_ATTRIBUTE_NOT_IN_TEST_PACKAGE);
                }
                TestResult testResult = new TestResult();
                Booking booking = medicalRecord.getBooking();
                Date dob = booking.getDateOfBirth();
                LocalDate dateOfBirth = dob instanceof java.sql.Date ? ((java.sql.Date) dob).toLocalDate()
                        : dob.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                int yearOld = Period.between(dateOfBirth, LocalDate.now()).getYears();
                String gender = booking.getGender() == 1 ? "MALE" : "FEMALE";
                String result = testResultDTO.getResult();
                if (result != null && !result.equals("")) {
                    testResult.setResult(result);
                    testResult.setNote(testResultDTO.getNote());
                    testPackageAttribute.getNormalRanges().forEach(normalRange -> {
                        EGender normalGender = normalRange.getGender();
                        ResultDTO resultDTO = new ResultDTO(result, yearOld, normalRange.getNormalRangeType(), normalRange.getAgeMin(),
                                normalRange.getAgeMax(), normalRange.getMinValue(), normalRange.getMaxValue(),
                                normalRange.getEqualValue(), normalRange.getExpectedValue(), normalRange.getGender());
                        if (normalGender != null && (normalGender.name().equals(gender) || normalGender.name().equals("ALL"))) {
                            setStatusForTestResult(testResult, resultDTO);
                        }
                        if (normalGender == null) {
                            setStatusForTestResult(testResult, resultDTO);
                        }
                    });
                    testResult.setLabResult(labResult);
                    testResult.setCreatedBy(doctorPrescribed.getUser().getUsername());
                    testResult.setTestPackageAttribute(testPackageAttribute);
                    testResults.add(testResult);
                } else {
                    throw new SystemException(MessageConstants.ERROR_TEST_RESULT);
                }
            });
            labResult.setMedicalRecord(medicalRecord);
            labResult.setTestPackage(testPackage);
            labResult.setDoctorPrescribed(doctorPrescribed);
            labResult.setStatus(ELabResultStatus.CREATED);
            labResult.setCreatedBy(currentUser.getUsername());
            labResult.setCreatedAt(LocalDateTime.now());
            labResultRepository.save(labResult);
            testResultRepository.saveAll(testResults);
        });
        response.setData(MessageConstants.ADD_LAB_RESULT_TO_MEDICAL_RECORD_SUCCESS);
        return response;
    }

    @Override
    public ResponseEntityBase getAllMedicalRecords() {
        ResponseEntityBase response = new ResponseEntityBase(HttpStatus.OK.value(), null, null);
        Map<Long, List<MedicalRecordDetailsProjection>> groupedByMedicalRecordId = medicalRecordRepository.getAllMedicalRecordsDetails().stream()
                .collect(Collectors.groupingBy(MedicalRecordDetailsProjection::getMedicalRecordId));
        List<MedicalRecordDetailsDTO> medicalRecordDetailsDTOList = new ArrayList<>();
        for (Map.Entry<Long, List<MedicalRecordDetailsProjection>> entry : groupedByMedicalRecordId.entrySet()) {
            List<MedicalRecordDetailsProjection> medicalRecordDetailsProjectionList = entry.getValue();
            MedicalRecordDetailsProjection medicalRecordDetailsProjection = medicalRecordDetailsProjectionList.get(0);
            MedicalRecordInfoDTO medicalRecordInfoDTO =
                    MedicalRecordMapper.MEDICAL_RECORD_MAPPER.mapToMedicalRecordInfoDTO(medicalRecordDetailsProjection);
            String userAddress = medicalRecordDetailsProjection.getSpecificAddress() + ", " +medicalRecordDetailsProjection.getWardName()
                    + ", " +medicalRecordDetailsProjection.getDistrictName() + ", " + medicalRecordDetailsProjection.getCityName() + ".";
            medicalRecordInfoDTO.setUserAddress(userAddress);
            Map<Long, LabResultDetailsDTO> labResultDetailsMap = mapLabResultDetails(medicalRecordDetailsProjectionList);
            MedicalRecordDetailsDTO medicalRecordDetailsDTO = new MedicalRecordDetailsDTO();
            medicalRecordDetailsDTO.setMedicalRecordInfo(medicalRecordInfoDTO);
            medicalRecordDetailsDTO.setLabResultsDetails(new ArrayList<>(labResultDetailsMap.values()));
            medicalRecordDetailsDTOList.add(medicalRecordDetailsDTO);
        }
        response.setData(medicalRecordDetailsDTOList);
        return response;
    }

    @Override
    public ResponseEntityBase getMedicalRecordByBookingId(Long bookingId) {
        ResponseEntityBase response = new ResponseEntityBase(HttpStatus.OK.value(), null, null);
        if (bookingRepository.findById(bookingId).isEmpty()) {
            throw new ResourceNotFoundException("Booking id", bookingId.toString());
        }
        List<MedicalRecordDetailsProjection> medicalRecordDetailsByBookingId = medicalRecordRepository.getAllMedicalRecordsDetails()
                .stream()
                .filter(medicalRecordDetails -> medicalRecordDetails.getBookingId().equals(bookingId))
                .toList();
        Set<MedicalRecordInfoDTO> medicalRecordsInfo = new HashSet<>();
        medicalRecordDetailsByBookingId.forEach(medicalRecordDetails -> {
            MedicalRecordInfoDTO medicalRecordInfoDTO =
                    MedicalRecordMapper.MEDICAL_RECORD_MAPPER.mapToMedicalRecordInfoDTO(medicalRecordDetails);
            medicalRecordsInfo.add(medicalRecordInfoDTO);
        });
        Map<Long, LabResultDetailsDTO> labResultDetailsMap = mapLabResultDetails(medicalRecordDetailsByBookingId);
        MedicalRecordDetailsDTO medicalRecordDetailsDTO = new MedicalRecordDetailsDTO();
        medicalRecordDetailsDTO.setMedicalRecordInfo(medicalRecordsInfo.stream().findFirst().get());
        medicalRecordDetailsDTO.setLabResultsDetails(new ArrayList<>(labResultDetailsMap.values()));
        response.setData(medicalRecordDetailsDTO);
        return response;
    }

    private void validateMedicalRecord(User currentUser, Booking booking, MedicalRecordRequest medicalRecordRequest) {
        long doctorId = bookingRepository.getDoctorIdByBookingId(booking.getBookingId());
        boolean checkAdmin = currentUser.getRoles().stream().anyMatch(role -> role.getRoleName().equals(ERole.ROLE_ADMIN));
        Doctor doctor = doctorRepository.findDoctorByUserId(currentUser.getUserId());
        if (!checkAdmin && doctor != null && !doctor.getDoctorId().equals(doctorId))
            throw new ForbiddenException(MessageConstants.FORBIDDEN_ADD_MEDICAL_RECORD);
        if (!booking.getStatus().equals(EBookingStatus.COMPLETED))
             throw new SystemException("Booking status is "+booking.getStatus().name()+". Booking status must be completed to be allowed to create medical records.");
        BookingTimeInfoProjection bookingTime = bookingRepository.getBookingInfoByBookingId(booking.getBookingId());
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
    }

    private Map<Long, LabResultDetailsDTO> mapLabResultDetails(List<MedicalRecordDetailsProjection> medicalRecordDetailsProjections) {
        Map<Long, LabResultDetailsDTO> labResultDetailsMap = new HashMap<>();
        for (MedicalRecordDetailsProjection medicalRecordDetailsProjection : medicalRecordDetailsProjections) {
            Long labResultId = medicalRecordDetailsProjection.getLabResultId();
            LabResultDetailsDTO labResultDetailsDTO = labResultDetailsMap.get(labResultId);
            if (labResultDetailsDTO == null) {
                labResultDetailsDTO = new LabResultDetailsDTO();
                LabResultInfoDTO labResultInfoDTO = new LabResultInfoDTO();
                LabResultMapper.LAB_RESULT_MAPPER.mapToLabResultInfoDTO(labResultInfoDTO, medicalRecordDetailsProjection);
                labResultDetailsDTO.setLabResultInfo(labResultInfoDTO);
                labResultDetailsDTO.setTestResultsDetails(new HashSet<>());
                labResultDetailsMap.put(labResultId, labResultDetailsDTO);
            }
            TestResultDetailsDTO testResultDetailsDTO =
                    TestResultMapper.TEST_RESULT_MAPPER.mapToTestResultDetailsDTO(medicalRecordDetailsProjection);
            labResultDetailsDTO.getTestResultsDetails().add(testResultDetailsDTO);
        }
        return labResultDetailsMap;
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
        String enumValue;
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
        return nextAppointmentDate.isAfter(currentDate.plusDays(7));
    }

    private void setStatusForTestResult(TestResult testResult, ResultDTO resultDTO) {
        boolean matchAge = (resultDTO.getAgeMin() == null || resultDTO.getYearOld() >= resultDTO.getAgeMin()) &&
                (resultDTO.getAgeMax() == null || resultDTO.getYearOld() <= resultDTO.getAgeMax());
        if (!matchAge) return;
        Double minValue = resultDTO.getMinValue();
        Double maxValue = resultDTO.getMaxValue();
        String result = resultDTO.getResult();
        Pattern doublePattern = Pattern.compile("^[+-]?(\\d+(\\.\\d{0,3})?|\\.\\d{1,3})$");
        Matcher rangeMatcher = doublePattern.matcher(result);
        switch (resultDTO.getNormalRangeType()) {
            case RANGE -> {
                if (!rangeMatcher.find()) return;
                if (Double.parseDouble(result) >= minValue && Double.parseDouble(result) <= maxValue) {
                    testResult.setStatus(ETestResultStatus.NORMAL);
                } else if (Double.parseDouble(result) < minValue) {
                    testResult.setStatus(ETestResultStatus.LOW);
                } else {
                    testResult.setStatus(ETestResultStatus.HIGH);
                }
            }
            case LESS_THAN -> {
                if (!rangeMatcher.find()) return;
                testResult.setStatus(Double.parseDouble(result) < maxValue ? ETestResultStatus.NORMAL : ETestResultStatus.HIGH);
            }
            case LESS_THAN_EQUAL -> {
                if (!rangeMatcher.find()) return;
                testResult.setStatus(Double.parseDouble(result) <= maxValue ? ETestResultStatus.NORMAL : ETestResultStatus.HIGH);
            }
            case GREATER_THAN -> {
                if (!rangeMatcher.find()) return;
                testResult.setStatus(Double.parseDouble(result) > minValue ? ETestResultStatus.NORMAL : ETestResultStatus.LOW);
            }
            case GREATER_THAN_EQUAL -> {
                if (!rangeMatcher.find()) return;
                testResult.setStatus(Double.parseDouble(result) >= minValue ? ETestResultStatus.NORMAL : ETestResultStatus.LOW);
            }
            case EQUAL -> {
                if (!rangeMatcher.find()) return;
                Double equalValue = resultDTO.getEqualValue();
                if (Double.parseDouble(result) == equalValue) {
                    testResult.setStatus(ETestResultStatus.NORMAL);
                } else if (Double.parseDouble(result) < equalValue) {
                    testResult.setStatus(ETestResultStatus.LOW);
                } else {
                    testResult.setStatus(ETestResultStatus.HIGH);
                }
            }
            case QUALITATIVE, SEMI_QUALITATIVE, TEXT -> {
                String expectedValue = resultDTO.getExpectedValue();
                if (expectedValue != null) {
                    List<String> values = Arrays.stream(expectedValue.split(",")).map(String::trim).toList();
                    if (values.stream().anyMatch(s -> s.equalsIgnoreCase(result.trim()))) {
                        testResult.setStatus(ETestResultStatus.NORMAL);
                    } else {
                        testResult.setStatus(ETestResultStatus.ABNORMAL);
                    }
                } else {
                    testResult.setStatus(ETestResultStatus.INCONCLUSIVE);
                }
            }
        }
    }

}
