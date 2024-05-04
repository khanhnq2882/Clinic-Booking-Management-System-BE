package khanhnq.project.clinicbookingmanagementsystem.mapper;

import khanhnq.project.clinicbookingmanagementsystem.dto.WorkScheduleDTO;
import khanhnq.project.clinicbookingmanagementsystem.entity.WorkSchedule;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface WorkScheduleMapper {
    WorkScheduleMapper WORK_SCHEDULE_MAPPER = Mappers.getMapper(WorkScheduleMapper.class);
    WorkSchedule mapToWorkSchedule (WorkScheduleDTO workScheduleDTO);
    WorkScheduleDTO mapToWorkScheduleDTO (WorkSchedule workSchedule);
}
