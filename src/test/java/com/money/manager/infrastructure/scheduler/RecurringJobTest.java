package com.money.manager.infrastructure.scheduler;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;

import org.mockito.InOrder;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.money.manager.application.ports.RecurringService;
import com.money.manager.application.ports.SavingsService;

@ExtendWith(MockitoExtension.class)
class RecurringJobTest {

    @Mock
    private RecurringService recurringService;

    @Mock
    private SavingsService savingsService;

    @Test
    void runMonthlyRecurrences_processesRecurringThenRecalculatesSavings() {
        RecurringJob job = new RecurringJob(recurringService, savingsService);

        job.runMonthlyRecurrences();

        InOrder inOrder = inOrder(recurringService, savingsService);
        inOrder.verify(recurringService).processFixedTransactions();
        inOrder.verify(recurringService).processAutomaticPayments();
        inOrder.verify(savingsService).recalculateAll();
        verify(savingsService).recalculateAll();
    }
}
