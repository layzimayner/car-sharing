package com.example.demo.service;

import com.example.demo.dto.role.RoleRequestDto;
import com.example.demo.dto.user.UserRegistrationDto;
import com.example.demo.dto.user.UserRegistrationRequestDto;
import com.example.demo.dto.user.UserWithRoleDto;
import com.example.demo.exception.RegistrationException;
import com.example.demo.model.User;

public interface UserService {
    UserRegistrationDto save(UserRegistrationRequestDto requestDto) throws RegistrationException;

    UserWithRoleDto updateRoles(RoleRequestDto requestDto, Long id);

    UserRegistrationDto findInfo(User user);

    UserRegistrationDto updateProfile(User user, UserRegistrationRequestDto requestDto);
}
