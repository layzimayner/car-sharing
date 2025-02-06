package com.example.demo.service.implementation;

import com.example.demo.model.Payment;
import com.example.demo.model.Rental;
import com.example.demo.model.User;
import com.example.demo.repository.RentalRepository;
import com.example.demo.service.NotificationService;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class TelegramNotificationService implements NotificationService {
    private final RestTemplate restTemplate;

    @Autowired
    private RentalRepository rentalRepository;

    @Value("${telegram.chat.id.member}")
    private String telegramChatIdMember;

    @Value("${telegram.bot.token}")
    private String botToken;

    public TelegramNotificationService() {
        this.restTemplate = new RestTemplate();
    }

    @Async
    @Override
    public void sendPaymentSuccessNotification(Payment payment) {
        String message = createPaymentSuccessMessage(payment);
        sendNotification(message, Long.parseLong(telegramChatIdMember));
    }

    @Async
    @Override
    public void sendRentalNotification(Rental rental) {
        String message = createRentalNotification(rental);
        sendNotification(message, Long.parseLong(telegramChatIdMember));
    }

    @Scheduled(cron = "0 0 8 * * ?")
    public void checkOverdueRentals() {
        List<Rental> overdueRentals = rentalRepository
                .findOverdueRentals(LocalDate.now().plusDays(1));

        if (overdueRentals.isEmpty()) {
            String noOverdueMessage = "📅 No rentals overdue today!";
            this.sendNotification(noOverdueMessage,
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
                this.sendNotification(overdueMessage,
                        Long.parseLong(telegramChatIdMember));
            });
        }
    }

    private String createPaymentSuccessMessage(Payment payment) {
        Rental rental = payment.getRental();
        User user = rental.getUser();

        return String.format("""
                ✅ *Payment Successful*
                
                *Car*: %s
                *User Name*: %s
                *User ID*: %d
                *Rental Start Date*: %s
                *Rental Return Date*: %s
                *Total Payment*: %s %s
                """,
                rental.getCar().getModel(),
                user.getFullName(),
                user.getId(),
                rental.getRentalDate(),
                rental.getActualReturnDate(),
                payment.getTotal(),
                "USD"
        );
    }

    private String createRentalNotification(Rental rental) {
        return String.format("""
                        🚗 *New Rental Created*
                        
                        *Car*: %s
                        *User ID*: %d
                        *Rental Date*: %s
                        *Return Date*: %s""",
                rental.getCar().getId(), rental.getUser().getId(), rental.getRentalDate(),
                rental.getReturnDate());
    }

    private void sendNotification(String message, Long chatId) {
        final String url = String.format("https://api.telegram.org/bot%s/sendMessage", botToken);

        Map<String, Object> request = new HashMap<>();
        request.put("chat_id", chatId);
        request.put("text", message);
        request.put("parse_mode", "Markdown");
        restTemplate.postForObject(url, request, String.class);
    }
}
