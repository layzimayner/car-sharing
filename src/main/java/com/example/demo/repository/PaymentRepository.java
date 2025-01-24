package com.example.demo.repository;

import com.example.demo.model.Payment;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    @Query("SELECT p FROM Payment p "
            + "JOIN p.rental r "
            + "JOIN r.user u "
            + "WHERE u.id = :userId")
    Page<Payment> findAll(@Param("userId")Pageable pageable, Long userId);

    @Query("SELECT p FROM Payment p "
            + "JOIN FETCH p.rental r "
            + "JOIN FETCH r.user u "
            + "JOIN FETCH r.car c "
            + "WHERE p.sessionId = :sessionId")
    Optional<Payment> findBySessionId(@Param("sessionId")String sessionId);

    List<Payment> findByStatus(Payment.Status status);
}
