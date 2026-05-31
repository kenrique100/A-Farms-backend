package com.afarms.investment.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

import com.afarms.investment.client.TransactionServiceClient;
import com.afarms.investment.repository.InvestmentRepository;
import com.afarms.investment.service.impl.InvestmentServiceImpl;
import com.afarms.investment.utils.InvestmentServiceUtils;
import com.afarms.investment.utils.InvestmentValidationUtils;
import org.junit.jupiter.api.Test;

class InvestmentServiceTest {

    @Test
    void shouldCreateServiceImpl() {
        InvestmentRepository repository = mock(InvestmentRepository.class);
        TransactionServiceClient client = mock(TransactionServiceClient.class);
        InvestmentValidationUtils validationUtils = mock(InvestmentValidationUtils.class);
        InvestmentServiceUtils serviceUtils = mock(InvestmentServiceUtils.class);

        InvestmentServiceImpl service = new InvestmentServiceImpl(repository, client, validationUtils, serviceUtils);
        assertNotNull(service);
    }
}
