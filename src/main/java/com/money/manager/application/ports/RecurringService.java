package com.money.manager.application.ports;

public interface RecurringService {
    void processFixedTransactions();

    void processAutomaticPayments();
}