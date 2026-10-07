package com.money.manager.application.ports;

import com.money.manager.domain.User;

public interface SavingsService {

    void recalculate(User user, int year, int month);

    void recalculateAll();
}
