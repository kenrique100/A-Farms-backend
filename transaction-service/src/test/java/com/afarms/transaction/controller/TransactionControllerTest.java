package com.afarms.transaction.controller;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

import com.afarms.transaction.service.TransactionService;
import org.junit.jupiter.api.Test;

class TransactionControllerTest {

    @Test
    void shouldCreateController() {
        TransactionService service = mock(TransactionService.class);
        TransactionController controller = new TransactionController(service);
        assertNotNull(controller);
    }
}
