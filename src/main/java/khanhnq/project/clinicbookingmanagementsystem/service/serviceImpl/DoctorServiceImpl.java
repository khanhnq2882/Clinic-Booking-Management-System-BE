package khanhnq.project.clinicbookingmanagementsystem.service.serviceImpl;

import khanhnq.project.clinicbookingmanagementsystem.entity.*;
import khanhnq.project.clinicbookingmanagementsystem.mapper.UserMapper;
import khanhnq.project.clinicbookingmanagementsystem.repository.*;
import khanhnq.project.clinicbookingmanagementsystem.request.DoctorInformationRequest;
import khanhnq.project.clinicbookingmanagementsystem.service.AuthService;
import khanhnq.project.clinicbookingmanagementsystem.service.DoctorService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class DoctorServiceImpl implements DoctorService {
    private final AuthService authService;
    private final WardRepository wardRepository;
    private final SkillRepository skillRepository;
    private final SpecializationRepository specializationRepository;
    private final WorkScheduleRepository workScheduleRepository;
    private final UserRepository userRepository;

    @Override
    public String addDoctorInformation(DoctorInformationRequest doctorInformationRequest) {
        User currentUser = authService.getCurrentUser();
        Set<WorkSchedule> workSchedules = doctorInformationRequest.getWorkSchedules().stream()
                .map(workScheduleDTO -> WorkSchedule.builder()
                        .startTime(workScheduleDTO.getStartTime())
                        .endTime(workScheduleDTO.getEndTime())
                        .user(currentUser)
                        .build())
                .collect(Collectors.toSet());
        if (currentUser.getRoles().stream().anyMatch(role -> role.getRoleName().name().equals("ROLE_DOCTOR"))) {
            UserMapper.USER_MAPPER.mapToDoctor(currentUser, doctorInformationRequest);
            currentUser.setAddress(Address.builder()
                    .specificAddress(doctorInformationRequest.getSpecificAddress())
                    .ward(wardRepository.findById(doctorInformationRequest.getWardId()).orElse(null))
                    .build());
            Set<WorkSchedule> sortWorkSchedules = new LinkedHashSet<>(addWorkSchedules(workSchedules, currentUser.getSpecialization().getSpecializationId()));
            currentUser.setWorkSchedules(sortWorkSchedules);
            Set<Skill> skills = doctorInformationRequest.getSkillIds()
                    .stream()
                    .map(id -> skillRepository.findById(id).orElse(null))
                    .collect(Collectors.toSet());
            currentUser.setSkills(skills);
            userRepository.save(currentUser);
        }
        return "Update information successfully.";
    }

    public Map<Long, List<User>> groupDoctorsBySpecialization() {
        Map<Long, List<User>> map = new HashMap<>();
        for (Specialization specialization : specializationRepository.findAll()) {
            if (!map.containsKey(specialization.getSpecializationId())) {
                List<User> users = userRepository.getDoctorsBySpecializationId(specialization.getSpecializationId());
                map.put(specialization.getSpecializationId(), users);
            }
        }
        return map;
    }

    public Map<Long, Set<WorkSchedule>> groupWorkScheduleByDoctor() {
        Map<Long, Set<WorkSchedule>> map = new HashMap<>();
        for (User user : userRepository.getDoctors()) {
            if (!map.containsKey(user.getUserId())) {
                Set<WorkSchedule> workSchedules = new HashSet<>(workScheduleRepository.getWorkSchedulesByUserId(user.getUserId()));
                map.put(user.getUserId(), workSchedules);
            }
        }
        return map;
    }

    // phai check them khoang thoi gian lich lam viec khong duoc nam trong lich lam viec cua nguoi khac
    // phai sort cac phan tu cua set theo dung thu tu
    public Set<WorkSchedule> addWorkSchedules(Set<WorkSchedule> workSchedulesRequest, Long specializationId) {
        Set<WorkSchedule> similarWorkSchedule = new HashSet<>();
        Map<Long, List<User>> specializationMap = groupDoctorsBySpecialization();
        Map<Long, Set<WorkSchedule>> userMap = groupWorkScheduleByDoctor();
        for (User doctor : specializationMap.get(specializationId)) {
            for (WorkSchedule workSchedule : userMap.get(doctor.getUserId())) {
                for (WorkSchedule workScheduleRequest : workSchedulesRequest) {
                    if (workScheduleRequest.getStartTime().equals(workSchedule.getStartTime())
                            && workScheduleRequest.getEndTime().equals(workSchedule.getEndTime())) {
                        similarWorkSchedule.add(workScheduleRequest);
                    }
                }
            }
        }
        workSchedulesRequest.removeAll(similarWorkSchedule);
        return workSchedulesRequest;
    }



}
