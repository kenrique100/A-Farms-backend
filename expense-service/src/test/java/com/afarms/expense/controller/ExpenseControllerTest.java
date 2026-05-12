package com.afarms.expense.controller;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

import com.afarms.expense.service.ExpenseService;
import org.junit.jupiter.api.Test;

class ExpenseControllerTest {

    @Test
    void shouldCreateController() {
        ExpenseService service = mock(ExpenseService.class);
        ExpenseController controller = new ExpenseController(service);
        assertNotNull(controller);
    }
}
