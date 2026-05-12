package com.afarms.income.controller;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

import com.afarms.income.service.IncomeService;
import org.junit.jupiter.api.Test;

class IncomeControllerTest {

    @Test
    void shouldCreateController() {
        IncomeService service = mock(IncomeService.class);
        IncomeController controller = new IncomeController(service);
        assertNotNull(controller);
    }
}
