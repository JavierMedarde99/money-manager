package com.money.manager.infrastructure.persistance.adapter;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.money.manager.domain.User;
import com.money.manager.infrastructure.persistance.PostgresCategoryRepository;
import com.money.manager.infrastructure.persistance.PostgresDebtRepository;
import com.money.manager.infrastructure.persistance.PostgresPaymentRepository;
import com.money.manager.infrastructure.persistance.PostgresTransactionRepository;
import com.money.manager.infrastructure.persistance.PostgresUserRepository;

@ExtendWith(MockitoExtension.class)
class UserRepositoryAdapterTest {

    @Mock
    private PostgresUserRepository jpa;

    @Mock
    private PostgresCategoryRepository jpaCategory;

    @Mock
    private PostgresTransactionRepository jpaTransaction;

    @Mock
    private PostgresDebtRepository jpaDebt;

    @Mock
    private PostgresPaymentRepository jpaPayment;

    private UserRepositoryAdapter adapter;

    private User user;

    @BeforeEach
    void setUp() {
        adapter = new UserRepositoryAdapter(jpa, jpaCategory, jpaTransaction, jpaDebt, jpaPayment);
        user = User.builder().id(7L).username("javi").build();
    }

    @Test
    void delete_deletesChildrenBeforeUserInCascadeOrder() {
        adapter.delete(user);

        InOrder inOrder = inOrder(jpaPayment, jpaDebt, jpaTransaction, jpaCategory, jpa);
        inOrder.verify(jpaPayment).deleteByDebt_User_Id(7L);
        inOrder.verify(jpaDebt).deleteByUser_Id(7L);
        inOrder.verify(jpaTransaction).deleteByUser_Id(7L);
        inOrder.verify(jpaCategory).deleteByUser_Id(7L);
        inOrder.verify(jpa).deleteById(7L);
    }

    @Test
    void delete_doesNotTouchAnythingElse() {
        adapter.delete(user);

        verify(jpaPayment).deleteByDebt_User_Id(7L);
        verify(jpaDebt).deleteByUser_Id(7L);
        verify(jpaTransaction).deleteByUser_Id(7L);
        verify(jpaCategory).deleteByUser_Id(7L);
        verify(jpa).deleteById(7L);
        verifyNoMoreInteractions(jpaPayment, jpaDebt, jpaTransaction, jpaCategory, jpa);
    }
}