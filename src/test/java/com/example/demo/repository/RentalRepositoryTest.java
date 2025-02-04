package com.example.demo.repository;

import com.example.demo.model.Rental;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.jdbc.Sql;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@DataJpaTest
@Testcontainers
@Sql(scripts = "classpath:database/scripts/rental/cleanup.sql",
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "classpath:database/scripts/rental/insert-rentals-users-cars.sql",
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "classpath:database/scripts/rental/cleanup.sql",
        executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class RentalRepositoryTest {
    private static final Pageable TEST_PAGEABLE = PageRequest.of(0, 10);

    @Autowired
    private RentalRepository rentalRepository;

    @Test
    @DisplayName("Check result of findAll method")
    void findAll_UserId_ReturnPage() {
        Long userId = 4L;
        Page<Rental> actual = rentalRepository.findAll(TEST_PAGEABLE, userId);
        Assertions.assertEquals(2, actual.getTotalElements());
    }

    @Test
    @DisplayName("Check result of findAllActive method")
    void findAllActive_UserId_ReturnPage() {
        Long userId = 4L;
        Page<Rental> actual = rentalRepository.findAllActive(TEST_PAGEABLE, userId);
        Assertions.assertEquals(1, actual.getTotalElements());
    }

    @Test
    @DisplayName("Check result of findByIdAndUserId method")
    void findByIdAndUserId_UserId_ReturnRental() {
        Long userId = 4L;
        Long rentalId = 1L;
        Optional<Rental> actual = rentalRepository.findByIdAndUserId(userId, rentalId);
        Assertions.assertNotNull(actual);
    }

    @Test
    @DisplayName("Check result of findOverdueRentals method")
    void findOverdueRentals_TodayDate_ReturnListOfRental() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);

        List<Rental> actual = rentalRepository.findOverdueRentals(tomorrow);

        Assertions.assertNotNull(actual);
    }

    @Test
    @DisplayName("Check result of findFinishedById method")
    void findFinishedById_RentalId_ReturnRental() {
        Long rentalId = 1L;
        Optional<Rental> actual = rentalRepository.findFinishedById(rentalId);
        Assertions.assertNotNull(actual);
    }

}
