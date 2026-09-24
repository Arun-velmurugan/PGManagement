package com.project.pghostel.app.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class PaymentScheduler {

    private final PaymentService paymentService;

    public PaymentScheduler(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @Scheduled(cron = "0 0 0 * * *")
    public void createMonthlyPayments() {

        paymentService.createMonthlyPayments();
    }
}