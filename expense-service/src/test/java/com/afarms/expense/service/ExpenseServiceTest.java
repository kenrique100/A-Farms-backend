package com.afarms.expense.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.afarms.expense.client.UserServiceClient;
import com.afarms.expense.exception.AccessDeniedException;
import com.afarms.expense.exception.BusinessException;
import com.afarms.expense.exception.ResourceNotFoundException;
import com.afarms.expense.model.dto.*;
import com.afarms.expense.model.entity.Expense;
import com.afarms.expense.repository.ExpenseRepository;
import com.afarms.expense.service.impl.ExpenseServiceImpl;
import com.afarms.expense.utils.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@ExtendWith(MockitoExtension.class)
class ExpenseServiceTest {

    @Mock
    private ExpenseRepository expenseRepository;

    @Mock
    private UserServiceClient userServiceClient;

    @Mock
    private ExpenseBuilderUtils builderUtils;

    @Mock
    private ExpenseValidationUtils validationUtils;

    @Mock
    private ExpenseServiceUtils serviceUtils;

    @Mock
    private ExpenseResponseBuilderUtils responseBuilderUtils;

    @InjectMocks
    private ExpenseServiceImpl expenseService;

    private ExpenseRequestDTO requestDTO;
    private Expense expense;
    private ExpenseResponseDTO responseDTO;
    private TokenValidationResponse tokenInfo;
    private UUID farmId;
    private UUID userId;
    private Long expenseId;

    @BeforeEach
    void setUp() {
        farmId = UUID.randomUUID();
        userId = UUID.randomUUID();
        expenseId = 1L;

        requestDTO = ExpenseRequestDTO.builder()
                .description("Test Expense")
                .amount(new BigDecimal("150.50"))
                .occurredAt(LocalDate.now())
                .build();

        expense = Expense.builder()
                .id(expenseId)
                .description("Test Expense")
                .amount(new BigDecimal("150.50"))
                .occurredAt(LocalDate.now())
                .farmId(farmId)
                .userId(userId)
                .build();

        responseDTO = ExpenseResponseDTO.builder()
                .id(expenseId)
                .description("Test Expense")
                .amount(new BigDecimal("150.50"))
                .occurredAt(LocalDate.now())
                .farmId(farmId)
                .userId(userId)
                .userName("testuser")
                .build();

        tokenInfo = new TokenValidationResponse(
                userId,
                "testuser",
                "test@example.com",
                true,
                "MASTER",
                farmId
        );
    }

    @Test
    void shouldCreateServiceImplWithAllDependencies() {
        assertNotNull(expenseService);
    }

    @Test
    void createExpense_ShouldSuccess() {
        String authHeader = "Bearer test-token";

        Map<UUID, String> userMap = Map.of(userId, "testuser");

        when(serviceUtils.validateAndGetTokenInfoWithWriteAccess(authHeader)).thenReturn(tokenInfo);
        when(builderUtils.buildExpenseFromRequest(requestDTO, tokenInfo)).thenReturn(expense);
        when(expenseRepository.save(any(Expense.class))).thenReturn(expense);
        when(serviceUtils.toResponseDTOWithFetch(any(Expense.class), eq(tokenInfo), eq(userMap)))
                .thenReturn(responseDTO);

        ExpenseResponseDTO result = expenseService.create(authHeader, requestDTO);

        assertNotNull(result);
        assertEquals(expenseId, result.getId());
        verify(expenseRepository, times(1)).save(any(Expense.class));
        verify(serviceUtils, times(1)).toResponseDTOWithFetch(any(Expense.class), eq(tokenInfo), eq(userMap));
    }

    @Test
    void createExpense_WhenValidationFails_ShouldThrowException() {
        String authHeader = "Bearer test-token";

        doThrow(new BusinessException("Invalid request"))
                .when(validationUtils).validateCreateRequest(requestDTO);

        assertThrows(BusinessException.class,
                () -> expenseService.create(authHeader, requestDTO));

        verify(expenseRepository, never()).save(any(Expense.class));
    }

    @Test
    void createExpense_WhenExpenseSaveFails_ShouldThrowException() {
        String authHeader = "Bearer test-token";

        when(serviceUtils.validateAndGetTokenInfoWithWriteAccess(authHeader)).thenReturn(tokenInfo);
        when(builderUtils.buildExpenseFromRequest(requestDTO, tokenInfo)).thenReturn(expense);
        when(expenseRepository.save(any(Expense.class)))
                .thenThrow(new RuntimeException("Database error"));

        assertThrows(RuntimeException.class,
                () -> expenseService.create(authHeader, requestDTO));

        verify(expenseRepository, times(1)).save(any(Expense.class));
        verify(serviceUtils, never()).toResponseDTOWithFetch(any(), any(), any());
    }

    @Test
    void createExpense_WhenTokenHasNoWriteAccess_ShouldThrowException() {
        String authHeader = "Bearer test-token";

        when(serviceUtils.validateAndGetTokenInfoWithWriteAccess(authHeader))
                .thenThrow(new AccessDeniedException("User role 'VIEWER' is not allowed to write expenses"));

        assertThrows(AccessDeniedException.class,
                () -> expenseService.create(authHeader, requestDTO));

        verify(expenseRepository, never()).save(any(Expense.class));
    }

    @Test
    void findAllExpenses_ShouldReturnPage() {
        String authHeader = "Bearer test-token";
        Pageable pageable = PageRequest.of(0, 20);
        Page<Expense> expensePage = new PageImpl<>(Collections.singletonList(expense));
        Map<UUID, String> userMap = Map.of(userId, "testuser");

        when(serviceUtils.validateAndGetTokenInfo(authHeader)).thenReturn(tokenInfo);
        when(expenseRepository.findByFarmId(farmId, pageable)).thenReturn(expensePage);
        when(serviceUtils.getUsernameMapForFarm(authHeader, farmId)).thenReturn(userMap);
        when(serviceUtils.toResponseDTOPage(expensePage, tokenInfo, userMap))
                .thenReturn(new PageImpl<>(Collections.singletonList(responseDTO)));

        Page<ExpenseResponseDTO> result = expenseService.findAll(authHeader, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(expenseId, result.getContent().getFirst().getId());
    }

    @Test
    void findAllListExpenses_ShouldReturnList() {
        String authHeader = "Bearer test-token";
        List<Expense> expenses = Collections.singletonList(expense);
        Map<UUID, String> userMap = Map.of(userId, "testuser");

        when(serviceUtils.validateAndGetTokenInfo(authHeader)).thenReturn(tokenInfo);
        when(expenseRepository.findByFarmId(farmId)).thenReturn(expenses);
        when(serviceUtils.getUsernameMapForFarm(authHeader, farmId)).thenReturn(userMap);
        when(serviceUtils.toResponseDTOList(expenses, tokenInfo, userMap))
                .thenReturn(Collections.singletonList(responseDTO));

        List<ExpenseResponseDTO> result = expenseService.findAllList(authHeader);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(expenseId, result.getFirst().getId());
    }

    @Test
    void findExpenseById_ShouldReturnExpense() {
        String authHeader = "Bearer test-token";
        Map<UUID, String> userMap = Map.of(userId, "testuser");

        when(serviceUtils.validateAndGetTokenInfo(authHeader)).thenReturn(tokenInfo);
        when(serviceUtils.findExpenseByIdAndFarmId(expenseId, farmId)).thenReturn(expense);
        when(serviceUtils.getUsernameMapForFarm(authHeader, farmId)).thenReturn(userMap);
        when(serviceUtils.toResponseDTOWithFetch(expense, tokenInfo, userMap)).thenReturn(responseDTO);

        ExpenseResponseDTO result = expenseService.findById(authHeader, expenseId);

        assertNotNull(result);
        assertEquals(expenseId, result.getId());
    }

    @Test
    void findExpenseById_WhenNotFound_ShouldThrowException() {
        String authHeader = "Bearer test-token";

        when(serviceUtils.validateAndGetTokenInfo(authHeader)).thenReturn(tokenInfo);
        when(serviceUtils.findExpenseByIdAndFarmId(expenseId, farmId))
                .thenThrow(new ResourceNotFoundException("Expense not found"));

        assertThrows(ResourceNotFoundException.class,
                () -> expenseService.findById(authHeader, expenseId));
    }

    @Test
    void updateExpense_ShouldSuccess() {
        String authHeader = "Bearer test-token";
        Expense updatedExpense = expense;
        updatedExpense.setDescription("Updated Description");

        ExpenseResponseDTO updatedResponse = ExpenseResponseDTO.builder()
                .id(expenseId)
                .description("Updated Description")
                .amount(new BigDecimal("150.50"))
                .occurredAt(LocalDate.now())
                .farmId(farmId)
                .userId(userId)
                .userName("testuser")
                .build();

        Map<UUID, String> userMap = Map.of(userId, "testuser");

        when(serviceUtils.validateAndGetTokenInfoWithWriteAccess(authHeader)).thenReturn(tokenInfo);
        when(serviceUtils.findExpenseByIdAndFarmId(expenseId, farmId)).thenReturn(expense);
        doNothing().when(serviceUtils).checkOwnershipOrMaster(expense, userId, tokenInfo.getRole());
        doNothing().when(builderUtils).updateExpenseFromRequest(expense, requestDTO);
        when(expenseRepository.save(expense)).thenReturn(updatedExpense);
        when(serviceUtils.getUsernameMapForFarm(authHeader, farmId)).thenReturn(userMap);
        when(serviceUtils.toResponseDTOWithFetch(updatedExpense, tokenInfo, userMap)).thenReturn(updatedResponse);

        ExpenseResponseDTO result = expenseService.update(authHeader, expenseId, requestDTO);

        assertNotNull(result);
        assertEquals("Updated Description", result.getDescription());
    }

    @Test
    void updateExpense_WhenExpenseNotFound_ShouldThrowException() {
        String authHeader = "Bearer test-token";

        when(serviceUtils.validateAndGetTokenInfoWithWriteAccess(authHeader)).thenReturn(tokenInfo);
        when(serviceUtils.findExpenseByIdAndFarmId(expenseId, farmId))
                .thenThrow(new ResourceNotFoundException("Expense not found"));

        assertThrows(ResourceNotFoundException.class,
                () -> expenseService.update(authHeader, expenseId, requestDTO));

        verify(expenseRepository, never()).save(any(Expense.class));
    }

    @Test
    void deleteExpense_ShouldSuccess() {
        String authHeader = "Bearer test-token";

        when(serviceUtils.validateAndGetTokenInfo(authHeader)).thenReturn(tokenInfo);
        when(serviceUtils.findExpenseByIdAndFarmId(expenseId, farmId)).thenReturn(expense);
        doNothing().when(serviceUtils).checkOwnershipOrMaster(expense, userId, tokenInfo.getRole());

        assertDoesNotThrow(() -> expenseService.delete(authHeader, expenseId));
        verify(expenseRepository).delete(expense);
    }

    @Test
    void deleteExpense_WhenNotOwner_ShouldThrowException() {
        String authHeader = "Bearer test-token";
        UUID otherUserId = UUID.randomUUID();
        expense.setUserId(otherUserId);

        TokenValidationResponse otherTokenInfo = new TokenValidationResponse(
                otherUserId,
                "otheruser",
                "other@example.com",
                true,
                "SUB_USER",
                farmId
        );

        when(serviceUtils.validateAndGetTokenInfo(authHeader)).thenReturn(otherTokenInfo);
        when(serviceUtils.findExpenseByIdAndFarmId(expenseId, farmId)).thenReturn(expense);
        doThrow(new AccessDeniedException("You can only modify your own expenses"))
                .when(serviceUtils).checkOwnershipOrMaster(expense, otherUserId, otherTokenInfo.getRole());

        assertThrows(AccessDeniedException.class,
                () -> expenseService.delete(authHeader, expenseId));
        verify(expenseRepository, never()).delete(any());
    }

    @Test
    void findByFarmId_ShouldReturnPage() {
        String authHeader = "Bearer test-token";
        UUID requestedFarmId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 20);
        Page<Expense> expensePage = new PageImpl<>(Collections.singletonList(expense));
        Map<UUID, String> userMap = Map.of(userId, "testuser");

        when(serviceUtils.validateAndGetTokenInfo(authHeader)).thenReturn(tokenInfo);
        doNothing().when(serviceUtils).checkFarmAccess(requestedFarmId, farmId, tokenInfo.getRole());
        when(expenseRepository.findByFarmId(requestedFarmId, pageable)).thenReturn(expensePage);
        when(serviceUtils.getUsernameMapForFarm(authHeader, requestedFarmId)).thenReturn(userMap);
        when(serviceUtils.toResponseDTOPage(expensePage, tokenInfo, userMap))
                .thenReturn(new PageImpl<>(Collections.singletonList(responseDTO)));

        Page<ExpenseResponseDTO> result = expenseService.findByFarmId(authHeader, requestedFarmId, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void findByDateRange_ShouldReturnPage() {
        String authHeader = "Bearer test-token";
        LocalDate start = LocalDate.now().minusDays(30);
        LocalDate end = LocalDate.now();
        Pageable pageable = PageRequest.of(0, 20);
        Page<Expense> expensePage = new PageImpl<>(Collections.singletonList(expense));
        Map<UUID, String> userMap = Map.of(userId, "testuser");

        when(serviceUtils.validateAndGetTokenInfo(authHeader)).thenReturn(tokenInfo);
        when(expenseRepository.findByFarmIdAndOccurredAtBetween(farmId, start, end, pageable))
                .thenReturn(expensePage);
        when(serviceUtils.getUsernameMapForFarm(authHeader, farmId)).thenReturn(userMap);
        when(serviceUtils.toResponseDTOPage(expensePage, tokenInfo, userMap))
                .thenReturn(new PageImpl<>(Collections.singletonList(responseDTO)));

        Page<ExpenseResponseDTO> result = expenseService.findByDateRange(authHeader, start, end, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void findByDateRange_WhenStartAfterEnd_ShouldThrowException() {
        LocalDate start = LocalDate.now();
        LocalDate end = LocalDate.now().minusDays(30);

        assertThrows(BusinessException.class,
                () -> expenseService.findByDateRange("Bearer token", start, end, PageRequest.of(0, 20)));
    }

    @Test
    void findByDateRange_WhenNullDates_ShouldThrowException() {
        assertThrows(BusinessException.class,
                () -> expenseService.findByDateRange("Bearer token", null, null, PageRequest.of(0, 20)));
    }
}