package com.example.demo.controller;

import com.example.demo.dto.role.RoleRequestDto;
import com.example.demo.dto.user.UserRegistrationDto;
import com.example.demo.dto.user.UserRegistrationRequestDto;
import com.example.demo.dto.user.UserWithRoleDto;
import com.example.demo.model.Role;
import com.example.demo.model.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.shaded.org.apache.commons.lang3.builder.EqualsBuilder;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UserControllerTest {
    private static final Long TEST_USER_ID = 4L;
    private static final String TEST_USER_EMAIL = "email@mail.com";
    private static final String TEST_USER_PASSWORD = "1qw2";
    private static final String TEST_USER_FIRST_NAME = "Electronic";
    private static final String TEST_USER_LAST_NAME = "Mail";
    private static final Role TEST_USER_ROLE = new Role(2L, Role.RoleName.USER);
    private static final Set<Role> TEST_USER_ROLES = Set.of(TEST_USER_ROLE);
    private static final List<Long> TEST_USER_ALTER_ROLES = List.of(2L,1L);

    protected static MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeAll
    static void setup(@Autowired WebApplicationContext webApplicationContext,
                      @Autowired DataSource dataSource) throws SQLException {
        cleanUpDb(dataSource);
        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();
    }

    @BeforeEach
    void beforeEach(@Autowired DataSource dataSource) throws SQLException{
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(true);
            ScriptUtils.executeSqlScript(
                    connection,
                    new ClassPathResource("database/scripts/user/insert-users.sql"));
        }
    }

    @AfterEach
    void afterEach(@Autowired DataSource dataSource) {
        cleanUpDb(dataSource);
    }

    @SneakyThrows
    static void cleanUpDb(DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(true);
            ScriptUtils.executeSqlScript(
                    connection,
                    new ClassPathResource("database/scripts/user/cleanup.sql")
            );
        }
    }

    @WithMockUser(username = "user", authorities = {"USER"})
    @Test
    @Sql(scripts = "classpath:database/scripts/user/insert-alter-user.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @DisplayName("Check functionality of ProfileInfo method")
    void updateProfileInfo_ValidData_UserRegistrationDto() throws Exception {
        //Given
        User user = createUser();

        UserRegistrationRequestDto requestDto = createRequest();

        UserRegistrationDto expected = createDto();

        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        //When
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.put("/users")
                        .with(authentication(new UsernamePasswordAuthenticationToken(
                                user, null, user.getAuthorities())))
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String jsonResponse = result.getResponse().getContentAsString();
        UserRegistrationDto actualResponse = objectMapper.readValue(jsonResponse, UserRegistrationDto.class);

        //Then
        Assertions.assertNotNull(actualResponse);
        Assertions.assertEquals(expected, actualResponse);
    }

    @WithMockUser(username = "user", authorities = {"USER"})
    @Test
    @Sql(scripts = "classpath:database/scripts/user/insert-user.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @DisplayName("Check functionality of ProfileInfo method")
    void getProfileInfo_ValidData_UserRegistrationDto() throws Exception {
        //Given
        User user = createUser();

        UserRegistrationDto expected = createDto();

        //When
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/users")
                        .with(authentication(new UsernamePasswordAuthenticationToken(
                                user, null, user.getAuthorities())))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String jsonResponse = result.getResponse().getContentAsString();
        UserRegistrationDto actualResponse = objectMapper.readValue(jsonResponse, UserRegistrationDto.class);

        //Then
        Assertions.assertNotNull(actualResponse);
        Assertions.assertEquals(expected, actualResponse);
    }

    @WithMockUser(username = "user", authorities = {"ADMIN"})
    @Test
    @Sql(scripts = "classpath:database/scripts/user/insert-user.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @DisplayName("Check functionality of updateProfileInfo method")
    void updateRoles_ValidData_UserWithRoleDto() throws Exception {
        //Given
        RoleRequestDto requestDto = new RoleRequestDto(Set.of(1L, 2L));

        UserWithRoleDto expected = new UserWithRoleDto();
        expected.setId(TEST_USER_ID);
        expected.setEmail(TEST_USER_EMAIL);
        expected.setFirstName(TEST_USER_FIRST_NAME);
        expected.setLastName(TEST_USER_LAST_NAME);
        expected.setRolesIds(TEST_USER_ALTER_ROLES);

        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        //When
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.put("/users/{id}", TEST_USER_ID)
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String jsonResponse = result.getResponse().getContentAsString();
        UserWithRoleDto actualResponse = objectMapper.readValue(jsonResponse, UserWithRoleDto.class);

        //Then
        Assertions.assertNotNull(actualResponse);
        Assertions.assertTrue(EqualsBuilder.reflectionEquals(expected,
                actualResponse, "rolesIds"));
    }

    private UserRegistrationDto createDto() {
        UserRegistrationDto dto = new UserRegistrationDto();
        dto.setId(TEST_USER_ID);
        dto.setEmail(TEST_USER_EMAIL);
        dto.setFirstName(TEST_USER_FIRST_NAME);
        dto.setLastName(TEST_USER_LAST_NAME);
        return dto;
    }

    private UserRegistrationRequestDto createRequest() {
        UserRegistrationRequestDto dto = new UserRegistrationRequestDto();
        dto.setEmail(TEST_USER_EMAIL);
        dto.setPassword(TEST_USER_PASSWORD);
        dto.setRepeatPassword(TEST_USER_PASSWORD);
        dto.setFirstName(TEST_USER_FIRST_NAME);
        dto.setLastName(TEST_USER_LAST_NAME);
        return dto;
    }

    private User createUser() {
        User user = new User();
        user.setId(TEST_USER_ID);
        user.setEmail(TEST_USER_EMAIL);
        user.setPassword(TEST_USER_PASSWORD);
        user.setFirstName(TEST_USER_FIRST_NAME);
        user.setLastName(TEST_USER_LAST_NAME);
        user.setRoles(TEST_USER_ROLES);
        return user;
    }
}
