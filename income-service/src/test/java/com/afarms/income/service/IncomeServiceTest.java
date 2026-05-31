package com.afarms.income.service;

import com.afarms.income.repository.IncomeRepository;
import com.afarms.income.service.impl.IncomeServiceImpl;
import com.afarms.income.utils.IncomeBuilderUtils;
import com.afarms.income.utils.IncomeServiceUtils;
import com.afarms.income.utils.IncomeValidationUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
class IncomeServiceTest {

    @Mock
    private IncomeRepository incomeRepository;

    @Mock
    private IncomeBuilderUtils builderUtils;

    @Mock
    private IncomeValidationUtils validationUtils;

    @Mock
    private IncomeServiceUtils serviceUtils;

    @InjectMocks
    private IncomeServiceImpl incomeService;

    @Test
    void shouldCreateServiceImpl() {
        assertNotNull(incomeService);
    }
}