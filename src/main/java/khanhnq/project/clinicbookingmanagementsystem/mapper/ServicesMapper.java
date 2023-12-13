package khanhnq.project.clinicbookingmanagementsystem.mapper;

import khanhnq.project.clinicbookingmanagementsystem.dto.ServicesDTO;
import khanhnq.project.clinicbookingmanagementsystem.entity.Services;
import khanhnq.project.clinicbookingmanagementsystem.request.ServiceRequest;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface ServicesMapper {
    ServicesMapper SERVICES_MAPPER = Mappers.getMapper(ServicesMapper.class);
    Services mapToServices(ServiceRequest serviceRequest);
    ServicesDTO mapToServicesResponse(Services services);
}
