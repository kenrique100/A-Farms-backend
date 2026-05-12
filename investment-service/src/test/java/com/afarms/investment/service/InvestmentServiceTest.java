package com.afarms.investment.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.afarms.investment.repository.InvestmentRepository;
import com.afarms.investment.service.impl.InvestmentServiceImpl;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class InvestmentServiceTest {

    @Test
    void shouldCreateServiceImpl() {
        InvestmentRepository repository = Mockito.mock(InvestmentRepository.class);
        InvestmentServiceImpl service = new InvestmentServiceImpl(repository);
        assertNotNull(service);
    }
}
