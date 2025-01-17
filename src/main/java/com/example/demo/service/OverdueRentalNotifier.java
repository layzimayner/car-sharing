package com.example.demo.service;

import com.example.demo.model.Rental;
import com.example.demo.repository.RentalRepository;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OverdueRentalNotifier {
    private final RentalRepository rentalRepository;
    private final NotificationService notificationService;
    @Value("${telegram.chat.id.member}")
    private String telegramChatIdMember;

    @Scheduled(cron = "0 0 8 * * ?")
    public void checkOverdueRentals() {
        List<Rental> overdueRentals = rentalRepository
                .findOverdueRentals(LocalDate.now().plusDays(1));

        if (overdueRentals.isEmpty()) {
            String noOverdueMessage = "📅 No rentals overdue today!";
            notificationService.sendNotification(noOverdueMessage,
                    Long.parseLong(telegramChatIdMember));
        } else {
            overdueRentals.forEach(rental -> {
                String overdueMessage = String.format("""
                        ⚠️ *Overdue Rental Alert!*
                        
                        *Car*: %s
                        *User ID*: %d
                        *Expected Return Date*: %s
                        """,
                        rental.getCar().getModel(),
                        rental.getUser().getId(),
                        rental.getReturnDate());
                notificationService.sendNotification(overdueMessage,
                        Long.parseLong(telegramChatIdMember));
            });
        }
    }
}

