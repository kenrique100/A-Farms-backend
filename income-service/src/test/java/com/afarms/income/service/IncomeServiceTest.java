package com.afarms.income.service;

import com.afarms.income.client.TransactionServiceClient;
import com.afarms.income.client.UserServiceClient;
import com.afarms.income.repository.IncomeRepository;
import com.afarms.income.service.impl.IncomeServiceImpl;
import com.afarms.income.utils.IncomeBuilderUtils;
import com.afarms.income.utils.IncomeValidationUtils;
import com.afarms.income.utils.ResponseBuilderUtils;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class IncomeServiceTest {

    @Test
    void shouldCreateServiceImpl() {
        IncomeRepository repository = Mockito.mock(IncomeRepository.class);
        UserServiceClient userServiceClient = Mockito.mock(UserServiceClient.class);
        TransactionServiceClient transactionServiceClient = Mockito.mock(TransactionServiceClient.class);
        IncomeValidationUtils validationUtils = Mockito.mock(IncomeValidationUtils.class);
        IncomeBuilderUtils builderUtils = Mockito.mock(IncomeBuilderUtils.class);
        ResponseBuilderUtils responseBuilder = Mockito.mock(ResponseBuilderUtils.class);

        IncomeServiceImpl service = new IncomeServiceImpl(
                repository,
                userServiceClient,
                transactionServiceClient,
                validationUtils,
                builderUtils,
                responseBuilder
        );
        assertNotNull(service);
    }
}