package com.afarms.transaction.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.afarms.transaction.repository.TransactionRepository;
import com.afarms.transaction.service.impl.TransactionServiceImpl;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class TransactionServiceTest {

    @Test
    void shouldCreateServiceImpl() {
        TransactionRepository repository = Mockito.mock(TransactionRepository.class);
        TransactionServiceImpl service = new TransactionServiceImpl(repository);
        assertNotNull(service);
    }
}
