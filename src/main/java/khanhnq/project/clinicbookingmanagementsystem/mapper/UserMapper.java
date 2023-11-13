package khanhnq.project.clinicbookingmanagementsystem.mapper;

import khanhnq.project.clinicbookingmanagementsystem.entity.User;
import khanhnq.project.clinicbookingmanagementsystem.request.UserProfileRequest;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

@Mapper
public interface UserMapper {
    UserMapper USER_MAPPER = Mappers.getMapper(UserMapper.class);

    void mapToUser(@MappingTarget User currentUser, UserProfileRequest userProfileRequest);

}