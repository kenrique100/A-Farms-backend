package com.afarms.investment.controller;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

import com.afarms.investment.service.InvestmentService;
import org.junit.jupiter.api.Test;

class InvestmentControllerTest {

    @Test
    void shouldCreateController() {
        InvestmentService service = mock(InvestmentService.class);
        InvestmentController controller = new InvestmentController(service);
        assertNotNull(controller);
    }
}
