package com.example.demo.service;

import com.example.demo.model.Payment;
import com.example.demo.model.Rental;

public interface NotificationService {
    void sendRentalNotification(Rental rental);

    void sendPaymentSuccessNotification(Payment payment);
}
