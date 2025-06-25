package khanhnq.project.clinicbookingmanagementsystem.mapper;

import khanhnq.project.clinicbookingmanagementsystem.entity.ImagingService;
import khanhnq.project.clinicbookingmanagementsystem.model.request.ImagingServiceRequest;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

@Mapper
public interface ImagingServiceMapper {
    ImagingServiceMapper IMAGING_SERVICE_MAPPER = Mappers.getMapper(ImagingServiceMapper.class);
    ImagingService mapToImagingService (ImagingServiceRequest imagingServiceRequest);
    void updateImagingService (@MappingTarget ImagingService imagingService, ImagingServiceRequest imagingServiceRequest);
}