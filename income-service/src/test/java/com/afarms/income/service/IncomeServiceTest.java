package com.afarms.income.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.afarms.income.client.TransactionServiceClient;
import com.afarms.income.client.UserServiceClient;
import com.afarms.income.repository.IncomeRepository;
import com.afarms.income.service.impl.IncomeServiceImpl;
import com.afarms.income.utils.IncomeValidationUtils;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class IncomeServiceTest {

    @Test
    void shouldCreateServiceImpl() {
        IncomeRepository repository = Mockito.mock(IncomeRepository.class);
        UserServiceClient userServiceClient = Mockito.mock(UserServiceClient.class);
        TransactionServiceClient transactionServiceClient = Mockito.mock(TransactionServiceClient.class);
        IncomeValidationUtils incomeValidationUtils = Mockito.mock(IncomeValidationUtils.class);

        IncomeServiceImpl service = new IncomeServiceImpl(
                repository,
                userServiceClient,
                transactionServiceClient,
                incomeValidationUtils
        );
        assertNotNull(service);
    }
}
