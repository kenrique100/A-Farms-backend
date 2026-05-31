package com.afarms.expense.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

import com.afarms.expense.client.TransactionServiceClient;
import com.afarms.expense.repository.ExpenseRepository;
import com.afarms.expense.service.impl.ExpenseServiceImpl;
import com.afarms.expense.utils.ExpenseServiceUtils;
import com.afarms.expense.utils.ExpenseValidationUtils;
import org.junit.jupiter.api.Test;

class ExpenseServiceTest {

    @Test
    void shouldCreateServiceImpl() {
        ExpenseRepository repository = mock(ExpenseRepository.class);
        TransactionServiceClient client = mock(TransactionServiceClient.class);
        ExpenseValidationUtils validationUtils = mock(ExpenseValidationUtils.class);
        ExpenseServiceUtils serviceUtils = mock(ExpenseServiceUtils.class);

        ExpenseServiceImpl service = new ExpenseServiceImpl(repository, client, validationUtils, serviceUtils);
        assertNotNull(service);
    }
}
