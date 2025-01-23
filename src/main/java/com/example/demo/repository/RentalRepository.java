package com.example.demo.repository;

import com.example.demo.model.Rental;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import jakarta.validation.constraints.Positive;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RentalRepository extends JpaRepository<Rental, Long> {
    @Query("SELECT r FROM Rental r "
            + "JOIN FETCH r.car c "
            + "JOIN FETCH r.user u "
            + "WHERE u.id = :userId")
    Page<Rental> findAll(Pageable pageable, @Param("userId") Long userId);

    @Query("SELECT r FROM Rental r "
            + "JOIN FETCH r.car c "
            + "JOIN FETCH r.user u "
            + "WHERE u.id = :userId "
            + "AND r.actualReturnDate IS NULL")
    Page<Rental> findAllActive(Pageable pageable, @Param("userId") Long userId);

    @Query("SELECT r FROM Rental r "
            + "JOIN FETCH r.car c "
            + "JOIN FETCH r.user u "
            + "WHERE u.id = :userId "
            + "AND r.id = :rentId")
    Optional<Rental> findByIdAndUserId(@Param("rentId")Long rentId, @Param("userId")Long userId);

    @Query("SELECT r FROM Rental r WHERE r.returnDate < :tomorrow AND r.actualReturnDate IS NULL")
    List<Rental> findOverdueRentals(@Param("tomorrow") LocalDate tomorrow);

    @Query("SELECT r FROM Rental r "
            + "WHERE r.id = :rentalId "
            + "AND r.actualReturnDate IS NOT NULL")
    Optional<Rental> findFinishedById(@Param("rentalId") Long rentalId);
}
