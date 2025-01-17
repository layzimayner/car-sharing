package com.example.demo.service;

import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class TelegramNotificationService implements NotificationService {

    private final RestTemplate restTemplate;
    private final String botToken;

    public TelegramNotificationService(@Value("${telegram.bot.token}") String botToken) {
        this.restTemplate = new RestTemplate();
        this.botToken = botToken;
    }

    @Override
    public void sendNotification(String message, Long chatId) {
        final String url = String.format("https://api.telegram.org/bot%s/sendMessage", botToken);

        Map<String, Object> request = new HashMap<>();
        request.put("chat_id", chatId);
        request.put("text", message);
        request.put("parse_mode", "Markdown");
        restTemplate.postForObject(url, request, String.class);
    }
}
