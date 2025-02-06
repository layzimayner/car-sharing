package com.example.demo.service;

import com.example.demo.dto.role.RoleRequestDto;
import com.example.demo.dto.user.UserRegistrationDto;
import com.example.demo.dto.user.UserRegistrationRequestDto;
import com.example.demo.dto.user.UserWithRoleDto;
import com.example.demo.exception.RegistrationException;
import com.example.demo.mapper.UserMapper;
import com.example.demo.model.Role;
import com.example.demo.model.User;
import com.example.demo.repository.RoleRepository;
import com.example.demo.repository.UserRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.example.demo.service.implementation.UserServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    private static final Long TEST_USER_ID = 1L;
    private static final String TEST_USER_EMAIL = "email@mail.com";
    private static final String TEST_USER_PASSWORD = "1234";
    private static final String TEST_USER_FIRST_NAME = "Test";
    private static final String TEST_USER_LAST_NAME = "Test";

    @InjectMocks
    private UserServiceImpl userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("Check save service with valid request")
    void save_ValidRequest_returnUserDto() throws RegistrationException {
        //Given
        User model = createModel();
        UserRegistrationRequestDto request = createRequest();
        UserRegistrationDto expect = createRegistrationDto();

        when(userRepository.existsByEmail(any())).thenReturn(false);
        when(userMapper.toModel(any())).thenReturn(model);
        when(roleRepository.findByName(any())).thenReturn(crateUserRole());
        when(userMapper.toDto(any())).thenReturn(createRegistrationDto());
        when(passwordEncoder.encode(any())).thenReturn(TEST_USER_PASSWORD);

        //When
        UserRegistrationDto actual = userService.save(request);

        //Then
        Assertions.assertEquals(actual, expect);
    }

    @Test
    @DisplayName("Check updateRoles service with valid request")
    void updateRoles_ValidRequest_returnUserDto() throws RegistrationException {
        //Given
        User model = createModel();
        RoleRequestDto request = new RoleRequestDto(Set.of(1L, 2L));
        UserWithRoleDto expect = createDtoWithRoles();

        when(userRepository.findById(any())).thenReturn(Optional.of(model));
        when(roleRepository.findRolesByIds(any())).thenReturn(
                Set.of(crateUserRole(),createAdminRole()));
        when(userRepository.save(any())).thenReturn(model);
        when(userMapper.toModelWithRoles(any())).thenReturn(expect);

        //When
        UserWithRoleDto actual = userService.updateRoles(request, TEST_USER_ID);

        //Then
        Assertions.assertEquals(actual, expect);
    }

    @Test
    @DisplayName("Check findInfo service with valid request")
    void findInfo_ValidRequest_returnUserDto() throws RegistrationException {
        //Given
        User model = createModel();
        UserRegistrationDto expect = createRegistrationDto();

        when(userMapper.toDto(any())).thenReturn(expect);

        //When
        UserRegistrationDto actual = userService.findInfo(model);

        //Then
        Assertions.assertEquals(actual, expect);
    }

    @Test
    @DisplayName("Check updateProfile service with valid request")
    void updateProfile_ValidRequest_returnUserDto() throws RegistrationException {
        //Given
        User model = createModel();
        UserRegistrationRequestDto request = createRequest();
        UserRegistrationDto expect = createRegistrationDto();

        when(passwordEncoder.encode(any())).thenReturn(TEST_USER_PASSWORD);
        doNothing().when(userMapper).update(any(), any());
        when(userRepository.save(any())).thenReturn(model);
        when(userMapper.toDto(any())).thenReturn(expect);

        //When
        UserRegistrationDto actual = userService.updateProfile(model, request);

        //Then
        Assertions.assertEquals(actual, expect);
    }

    private UserWithRoleDto createDtoWithRoles() {
        UserWithRoleDto dto = new UserWithRoleDto();
        dto.setRolesIds(List.of(1L, 2L));
        dto.setId(TEST_USER_ID);
        dto.setFirstName(TEST_USER_FIRST_NAME);
        dto.setLastName(TEST_USER_LAST_NAME);
        dto.setEmail(TEST_USER_EMAIL);
        return dto;
    }

    private UserRegistrationRequestDto createRequest() {
        UserRegistrationRequestDto request = new UserRegistrationRequestDto();
        request.setRepeatPassword(TEST_USER_PASSWORD);
        request.setPassword(TEST_USER_PASSWORD);
        request.setEmail(TEST_USER_EMAIL);
        request.setFirstName(TEST_USER_FIRST_NAME);
        request.setLastName(TEST_USER_LAST_NAME);
        return request;
    }

    private User createModel() {
        User user = new User();
        user.setId(TEST_USER_ID);
        user.setFirstName(TEST_USER_FIRST_NAME);
        user.setLastName(TEST_USER_LAST_NAME);
        user.setPassword(TEST_USER_PASSWORD);
        user.setRoles(Set.of(crateUserRole()));
        user.setEmail(TEST_USER_EMAIL);
        return user;
    }

    private Role crateUserRole() {
        Role role = new Role();
        role.setId(2L);
        role.setName(Role.RoleName.USER);
        return role;
    }

    private UserRegistrationDto createRegistrationDto() {
        UserRegistrationDto dto = new UserRegistrationDto();
        dto.setId(TEST_USER_ID);
        dto.setFirstName(TEST_USER_FIRST_NAME);
        dto.setLastName(TEST_USER_LAST_NAME);
        dto.setEmail(TEST_USER_EMAIL);
        return dto;
    }

    private Role createAdminRole() {
        Role role = new Role();
        role.setId(1L);
        role.setName(Role.RoleName.ADMIN);
        return role;
    }
}
