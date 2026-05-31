package com.afarms.expense.controller;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.afarms.expense.model.dto.ExpenseRequestDTO;
import com.afarms.expense.model.dto.ExpenseResponseDTO;
import com.afarms.expense.service.ExpenseService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.UUID;

@WebMvcTest(ExpenseController.class)
class ExpenseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ExpenseService expenseService;

    @Autowired
    private ObjectMapper objectMapper;

    private ExpenseRequestDTO requestDTO;
    private ExpenseResponseDTO responseDTO;
    private UUID farmId;
    private Long expenseId;

    @BeforeEach
    void setUp() {
        farmId = UUID.randomUUID();
        expenseId = 1L;

        requestDTO = ExpenseRequestDTO.builder()
                .description("Test Expense")
                .amount(new BigDecimal("150.50"))
                .occurredAt(LocalDate.now())
                .build();

        responseDTO = ExpenseResponseDTO.builder()
                .id(expenseId)
                .description("Test Expense")
                .amount(new BigDecimal("150.50"))
                .occurredAt(LocalDate.now())
                .farmId(farmId)
                .userId(UUID.randomUUID())
                .userName("testuser")
                .transactionId(100L)
                .build();
    }

    @Test
    void shouldCreateController() {
        ExpenseService service = mock(ExpenseService.class);
        ExpenseController controller = new ExpenseController(service);
        assertNotNull(controller);
    }

    @Test
    void createExpense_ShouldReturnCreated() throws Exception {
        when(expenseService.create(any(String.class), any(ExpenseRequestDTO.class)))
                .thenReturn(responseDTO);

        mockMvc.perform(post("/api/v1/expenses")
                        .header("Authorization", "Bearer test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(expenseId))
                .andExpect(jsonPath("$.description").value("Test Expense"))
                .andExpect(jsonPath("$.amount").value(150.50));
    }

    @Test
    void findAllExpenses_ShouldReturnPage() throws Exception {
        Page<ExpenseResponseDTO> page = new PageImpl<>(
                Collections.singletonList(responseDTO),
                PageRequest.of(0, 20),
                1
        );

        when(expenseService.findAll(any(String.class), any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(get("/api/v1/expenses")
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(expenseId))
                .andExpect(jsonPath("$.content[0].description").value("Test Expense"));
    }

    @Test
    void findAllListExpenses_ShouldReturnList() throws Exception {
        when(expenseService.findAllList(any(String.class)))
                .thenReturn(Collections.singletonList(responseDTO));

        mockMvc.perform(get("/api/v1/expenses/all")
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(expenseId));
    }

    @Test
    void findExpenseById_ShouldReturnExpense() throws Exception {
        when(expenseService.findById(any(String.class), eq(expenseId)))
                .thenReturn(responseDTO);

        mockMvc.perform(get("/api/v1/expenses/{id}", expenseId)
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(expenseId))
                .andExpect(jsonPath("$.description").value("Test Expense"));
    }

    @Test
    void updateExpense_ShouldReturnUpdatedExpense() throws Exception {
        when(expenseService.update(any(String.class), eq(expenseId), any(ExpenseRequestDTO.class)))
                .thenReturn(responseDTO);

        mockMvc.perform(put("/api/v1/expenses/{id}", expenseId)
                        .header("Authorization", "Bearer test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(expenseId));
    }

    @Test
    void deleteExpense_ShouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/api/v1/expenses/{id}", expenseId)
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isNoContent());
    }

    @Test
    void findByFarmId_ShouldReturnPage() throws Exception {
        Page<ExpenseResponseDTO> page = new PageImpl<>(
                Collections.singletonList(responseDTO),
                PageRequest.of(0, 20),
                1
        );

        when(expenseService.findByFarmId(any(String.class), eq(farmId), any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(get("/api/v1/expenses/farm/{farmId}", farmId)
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].farmId").value(farmId.toString()));
    }

    @Test
    void findByDateRange_ShouldReturnPage() throws Exception {
        LocalDate start = LocalDate.now().minusDays(30);
        LocalDate end = LocalDate.now();

        Page<ExpenseResponseDTO> page = new PageImpl<>(
                Collections.singletonList(responseDTO),
                PageRequest.of(0, 20),
                1
        );

        when(expenseService.findByDateRange(any(String.class), eq(start), eq(end), any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(get("/api/v1/expenses/range")
                        .header("Authorization", "Bearer test-token")
                        .param("start", start.toString())
                        .param("end", end.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(expenseId));
    }
}