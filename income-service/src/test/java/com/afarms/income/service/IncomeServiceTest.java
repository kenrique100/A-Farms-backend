package com.afarms.income.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.afarms.income.repository.IncomeRepository;
import com.afarms.income.service.impl.IncomeServiceImpl;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class IncomeServiceTest {

    @Test
    void shouldCreateServiceImpl() {
        IncomeRepository repository = Mockito.mock(IncomeRepository.class);
        IncomeServiceImpl service = new IncomeServiceImpl(repository);
        assertNotNull(service);
    }
}
