package khanhnq.project.clinicbookingmanagementsystem.mapper;

import khanhnq.project.clinicbookingmanagementsystem.dto.DoctorDTO;
import khanhnq.project.clinicbookingmanagementsystem.dto.UserDTO;
import khanhnq.project.clinicbookingmanagementsystem.entity.Role;
import khanhnq.project.clinicbookingmanagementsystem.entity.User;
import khanhnq.project.clinicbookingmanagementsystem.request.DoctorInformationRequest;
import khanhnq.project.clinicbookingmanagementsystem.request.UserProfileRequest;
import khanhnq.project.clinicbookingmanagementsystem.response.RequestDoctorResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

@Mapper
public interface UserMapper {
    UserMapper USER_MAPPER = Mappers.getMapper(UserMapper.class);
    void mapToUser(@MappingTarget User currentUser, UserProfileRequest userProfileRequest);
    @Mapping(target = "roleNames", source = "roles")
    UserDTO mapToUserDTO(User user);
    void mapToRequestDoctorResponse(@MappingTarget RequestDoctorResponse requestDoctorResponse, User user);
    DoctorDTO mapToDoctorResponse(User user);
    default String mapRolesToString(Role role) {
        return role.getRoleName().name();
    }
    void mapToDoctor(@MappingTarget User currentUser, DoctorInformationRequest doctorInformationRequest);
}