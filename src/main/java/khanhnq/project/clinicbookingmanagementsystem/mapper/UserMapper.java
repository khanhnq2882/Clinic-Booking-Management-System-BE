package khanhnq.project.clinicbookingmanagementsystem.mapper;

import khanhnq.project.clinicbookingmanagementsystem.entity.User;
import khanhnq.project.clinicbookingmanagementsystem.request.UserProfileRequest;
import khanhnq.project.clinicbookingmanagementsystem.response.UserResponse;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

@Mapper
public interface UserMapper {
    UserMapper USER_MAPPER = Mappers.getMapper(UserMapper.class);

    void mapToUser(@MappingTarget User currentUser, UserProfileRequest userProfileRequest);
    UserResponse mapToUserResponse(User user);

}