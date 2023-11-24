package khanhnq.project.clinicbookingmanagementsystem.mapper;

import khanhnq.project.clinicbookingmanagementsystem.entity.Services;
import khanhnq.project.clinicbookingmanagementsystem.request.ServiceRequest;
import khanhnq.project.clinicbookingmanagementsystem.response.ServicesResponse;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface ServicesMapper {
    ServicesMapper SERVICES_MAPPER = Mappers.getMapper(ServicesMapper.class);
    Services mapToServices(ServiceRequest serviceRequest);

    ServicesResponse mapToServicesResponse(Services services);
}
