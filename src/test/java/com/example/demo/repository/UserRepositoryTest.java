package com.example.demo.repository;

import com.example.demo.model.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Optional;

@DataJpaTest
@Testcontainers
@Sql(scripts = "classpath:database/scripts/user/cleanup.sql",
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "classpath:database/scripts/user/insert-users.sql",
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "classpath:database/scripts/user/cleanup.sql",
        executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class UserRepositoryTest {
    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("Check if user exists by real email")
    void existsByEmail_RealEmail_ReturnUser() {
        String realEmail = "User@mail.com";
        boolean actual = userRepository.existsByEmail(realEmail);
        Assertions.assertTrue(actual);
    }

    @Test
    @DisplayName("Check if user's roles by real email")
    void findByEmailWithRoles_RealEmail_ReturnUser() {
        String realEmail = "User@mail.com";
        Optional<User> actual = userRepository.findByEmailWithRoles(realEmail);
        Assertions.assertEquals(1, actual.get().getAuthorities().size());
    }
}
