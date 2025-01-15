package com.example.demo.mapper;

import com.example.demo.config.MapperConfig;
import com.example.demo.dto.user.UserRegistrationDto;
import com.example.demo.dto.user.UserRegistrationRequestDto;
import com.example.demo.dto.user.UserWithRoleDto;
import com.example.demo.model.Role;
import com.example.demo.model.User;
import java.util.List;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(config = MapperConfig.class, componentModel = "spring")
public interface UserMapper {
    User toModel(UserRegistrationRequestDto requestDto);

    UserRegistrationDto toDto(User user);

    UserWithRoleDto toModelWithRoles(User user);

    void update(@MappingTarget User user, UserRegistrationRequestDto requestDto);

    @AfterMapping
    default void setRolesIds(@MappingTarget UserWithRoleDto userDto, User user) {
        List<Long> rolesIds = user.getRoles().stream()
                .map(Role::getId)
                .toList();
        userDto.setRolesId(rolesIds);
    }
}
