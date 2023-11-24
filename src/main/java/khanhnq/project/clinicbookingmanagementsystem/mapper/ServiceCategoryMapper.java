package khanhnq.project.clinicbookingmanagementsystem.mapper;

import khanhnq.project.clinicbookingmanagementsystem.entity.ServiceCategory;
import khanhnq.project.clinicbookingmanagementsystem.request.ServiceCategoryRequest;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface ServiceCategoryMapper {
    ServiceCategoryMapper SERVICE_CATEGORY_MAPPER = Mappers.getMapper(ServiceCategoryMapper.class);
    ServiceCategory mapToServiceCategory(ServiceCategoryRequest serviceCategoryRequest);
}
