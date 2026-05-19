package com.afarms.transaction.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.afarms.transaction.client.UserServiceClient;
import com.afarms.transaction.repository.TransactionRepository;
import com.afarms.transaction.service.impl.TransactionServiceImpl;
import com.afarms.transaction.utils.TransactionValidationUtils;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class TransactionServiceTest {

    @Test
    void shouldCreateServiceImpl() {
        TransactionRepository repository = Mockito.mock(TransactionRepository.class);
        UserServiceClient userServiceClient = Mockito.mock(UserServiceClient.class);
        TransactionValidationUtils validationUtils = Mockito.mock(TransactionValidationUtils.class);

        TransactionServiceImpl service = new TransactionServiceImpl(repository, userServiceClient, validationUtils);
        assertNotNull(service);
    }
}
