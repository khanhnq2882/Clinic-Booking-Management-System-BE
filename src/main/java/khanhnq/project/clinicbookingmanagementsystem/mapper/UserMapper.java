package khanhnq.project.clinicbookingmanagementsystem.mapper;

import khanhnq.project.clinicbookingmanagementsystem.model.dto.DoctorDTO;
import khanhnq.project.clinicbookingmanagementsystem.model.dto.UserDTO;
import khanhnq.project.clinicbookingmanagementsystem.entity.Role;
import khanhnq.project.clinicbookingmanagementsystem.entity.User;
import khanhnq.project.clinicbookingmanagementsystem.model.request.DoctorInformationRequest;
import khanhnq.project.clinicbookingmanagementsystem.model.request.UserProfileRequest;
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
    DoctorDTO mapToDoctorResponse(User user);
    default String mapRolesToString(Role role) {
        return role.getRoleName().name();
    }
    void mapToDoctor(@MappingTarget User currentUser, DoctorInformationRequest doctorInformationRequest);
}