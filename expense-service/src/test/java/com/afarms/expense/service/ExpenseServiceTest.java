package com.afarms.expense.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.afarms.expense.repository.ExpenseRepository;
import com.afarms.expense.service.impl.ExpenseServiceImpl;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class ExpenseServiceTest {

    @Test
    void shouldCreateServiceImpl() {
        ExpenseRepository repository = Mockito.mock(ExpenseRepository.class);
        ExpenseServiceImpl service = new ExpenseServiceImpl(repository);
        assertNotNull(service);
    }
}
