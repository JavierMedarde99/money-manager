package com.money.manager.infrastructure.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.money.manager.application.ports.RecurringService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RecurringJob {

    private final RecurringService recurringService;

    @Scheduled(cron = "0 5 0 1 * *")
    public void runMonthlyRecurrences() {
        recurringService.processFixedTransactions();
        recurringService.processAutomaticPayments();
    }
}