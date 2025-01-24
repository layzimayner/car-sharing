package com.example.demo.repository;

import com.example.demo.model.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    @Query("SELECT p FROM Payment p "
            + "JOIN p.rental r "
            + "JOIN r.user u "
            + "WHERE u.id = :userId")
    Page<Payment> findAll(Pageable pageable, Long userId);

    Optional<Payment> getPaymentByRentalId(Long rentalId);
}
