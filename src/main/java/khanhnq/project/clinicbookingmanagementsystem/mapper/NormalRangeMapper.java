package khanhnq.project.clinicbookingmanagementsystem.mapper;

import khanhnq.project.clinicbookingmanagementsystem.entity.NormalRange;
import khanhnq.project.clinicbookingmanagementsystem.model.dto.NormalRangeDTO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface NormalRangeMapper {
    NormalRangeMapper NORMAL_RANGE_MAPPER = Mappers.getMapper(NormalRangeMapper.class);
    NormalRange mapToNormalRange (NormalRangeDTO normalRangeDTO);
}
