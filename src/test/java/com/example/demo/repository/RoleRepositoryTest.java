package com.example.demo.repository;

import com.example.demo.model.Role;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Set;

@DataJpaTest
@Testcontainers
@Sql(scripts = "classpath:database/scripts/user/cleanup.sql",
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "classpath:database/scripts/user/insert-users.sql",
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "classpath:database/scripts/user/cleanup.sql",
        executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class RoleRepositoryTest {
    @Autowired
    private RoleRepository roleRepository;

    @Test
    @DisplayName("Find real role by id")
    void findByName_RealName_ReturnRole() {
        Role.RoleName name = Role.RoleName.USER;
        Role actual = roleRepository.findByName(name);
        Assertions.assertNotNull(actual);
    }

    @Test
    @DisplayName("Check functionality of findRolesByIds method")
    void findRolesByIds_RealName_ReturnRole() {
        Set<Long> ids = Set.of(1L,2L);
        Set<Role> actual = roleRepository.findRolesByIds(ids);
        Assertions.assertEquals(2, actual.size());
    }

}
