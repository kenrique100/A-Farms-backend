package com.afarms.investment.service;

import com.afarms.investment.model.dto.InvestmentRequestDTO;
import com.afarms.investment.model.dto.InvestmentResponseDTO;
import com.afarms.investment.model.dto.TokenValidationResponse;
import com.afarms.investment.model.entity.Investment;
import com.afarms.investment.repository.InvestmentRepository;
import com.afarms.investment.service.impl.InvestmentServiceImpl;
import com.afarms.investment.utils.InvestmentBuilderUtils;
import com.afarms.investment.utils.InvestmentServiceUtils;
import com.afarms.investment.utils.InvestmentValidationUtils;
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
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InvestmentServiceTest {

    @Mock
    private InvestmentRepository investmentRepository;

    @Mock
    private InvestmentBuilderUtils builderUtils;

    @Mock
    private InvestmentValidationUtils validationUtils;

    @Mock
    private InvestmentServiceUtils serviceUtils;

    @InjectMocks
    private InvestmentServiceImpl investmentService;

    private UUID farmId;
    private UUID userId;
    private TokenValidationResponse tokenInfo;
    private InvestmentRequestDTO requestDTO;
    private Investment investment;
    private InvestmentResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        farmId = UUID.randomUUID();
        userId = UUID.randomUUID();

        tokenInfo = new TokenValidationResponse(
                userId,
                "testuser@example.com",
                "testuser@example.com",
                true,
                "MASTER",
                farmId
        );

        requestDTO = InvestmentRequestDTO.builder()
                .initialAmount(new BigDecimal("1000.00"))
                .currentBalance(new BigDecimal("1000.00"))
                .build();

        // Remove .roi() from Investment builder - ROI is not stored in entity
        investment = Investment.builder()
                .id(1L)
                .initialAmount(new BigDecimal("1000.00"))
                .currentBalance(new BigDecimal("1000.00"))
                .farmId(farmId)
                .userId(userId)
                .createdAt(LocalDate.now())
                .updatedAt(LocalDate.now())
                .build();

        responseDTO = InvestmentResponseDTO.builder()
                .id(1L)
                .initialAmount(new BigDecimal("1000.00"))
                .currentBalance(new BigDecimal("1000.00"))
                .roi(BigDecimal.ZERO)  // ROI is in DTO, not entity
                .farmId(farmId)
                .userId(userId)
                .userName("testuser@example.com")
                .createdAt(LocalDate.now())
                .updatedAt(LocalDate.now())
                .build();
    }

    @Test
    void create_shouldSaveAndReturnResponse() {
        // Given
        when(serviceUtils.validateAndGetTokenInfoWithWriteAccess(anyString())).thenReturn(tokenInfo);
        when(builderUtils.buildFromRequest(any(InvestmentRequestDTO.class), any(TokenValidationResponse.class)))
                .thenReturn(investment);
        when(investmentRepository.save(any(Investment.class))).thenReturn(investment);
        when(serviceUtils.toResponseDTO(anyString(), any(Investment.class), any(TokenValidationResponse.class), anyMap()))
                .thenReturn(responseDTO);

        // When
        InvestmentResponseDTO result = investmentService.create("Bearer token", requestDTO);

        // Then
        assertNotNull(result);
        assertEquals(new BigDecimal("1000.00"), result.getInitialAmount());
        assertEquals(new BigDecimal("1000.00"), result.getCurrentBalance());
        verify(validationUtils).validateCreateRequest(requestDTO);
        verify(serviceUtils).validateAndGetTokenInfoWithWriteAccess("Bearer token");
        verify(builderUtils).buildFromRequest(requestDTO, tokenInfo);
        verify(investmentRepository).save(investment);
    }

    @Test
    void findAll_shouldReturnPageOfInvestments() {
        // Given
        Pageable pageable = PageRequest.of(0, 20);
        Page<Investment> investmentPage = new PageImpl<>(List.of(investment), pageable, 1);
        Map<UUID, String> usernameMap = Map.of(userId, "testuser@example.com");

        when(serviceUtils.validateAndGetTokenInfo(anyString())).thenReturn(tokenInfo);
        when(investmentRepository.findByFarmId(eq(farmId), eq(pageable))).thenReturn(investmentPage);
        when(serviceUtils.getUsernameMapForFarm(anyString(), eq(farmId))).thenReturn(usernameMap);
        when(serviceUtils.toResponseDTOPage(anyString(), eq(investmentPage), eq(tokenInfo), eq(usernameMap)))
                .thenReturn(new PageImpl<>(List.of(responseDTO), pageable, 1));

        // When
        Page<InvestmentResponseDTO> result = investmentService.findAll("Bearer token", pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(serviceUtils).validateAndGetTokenInfo("Bearer token");
        verify(investmentRepository).findByFarmId(farmId, pageable);
    }

    @Test
    void findById_shouldReturnInvestment() {
        // Given
        Map<UUID, String> usernameMap = Map.of(userId, "testuser@example.com");

        when(serviceUtils.validateAndGetTokenInfo(anyString())).thenReturn(tokenInfo);
        when(serviceUtils.findByIdAndFarmId(1L, farmId)).thenReturn(investment);
        when(serviceUtils.getUsernameMapForFarm(anyString(), eq(farmId))).thenReturn(usernameMap);
        when(serviceUtils.toResponseDTO(anyString(), eq(investment), eq(tokenInfo), eq(usernameMap)))
                .thenReturn(responseDTO);

        // When
        InvestmentResponseDTO result = investmentService.findById("Bearer token", 1L);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(serviceUtils).validateAndGetTokenInfo("Bearer token");
        verify(serviceUtils).findByIdAndFarmId(1L, farmId);
    }

    @Test
    void update_shouldUpdateAndReturnInvestment() {
        // Given
        InvestmentRequestDTO updateRequest = InvestmentRequestDTO.builder()
                .initialAmount(new BigDecimal("1000.00"))
                .currentBalance(new BigDecimal("1200.00"))
                .build();

        Investment updatedInvestment = Investment.builder()
                .id(1L)
                .initialAmount(new BigDecimal("1000.00"))
                .currentBalance(new BigDecimal("1200.00"))
                .farmId(farmId)
                .userId(userId)
                .createdAt(LocalDate.now())
                .updatedAt(LocalDate.now())
                .build();

        InvestmentResponseDTO updatedResponse = InvestmentResponseDTO.builder()
                .id(1L)
                .initialAmount(new BigDecimal("1000.00"))
                .currentBalance(new BigDecimal("1200.00"))
                .roi(new BigDecimal("20.00"))
                .farmId(farmId)
                .userId(userId)
                .userName("testuser@example.com")
                .createdAt(LocalDate.now())
                .updatedAt(LocalDate.now())
                .build();

        Map<UUID, String> usernameMap = Map.of(userId, "testuser@example.com");

        when(serviceUtils.validateAndGetTokenInfoWithWriteAccess(anyString())).thenReturn(tokenInfo);
        when(serviceUtils.findByIdAndFarmId(1L, farmId)).thenReturn(investment);
        doNothing().when(serviceUtils).checkOwnershipOrMaster(investment, userId, "MASTER");
        doNothing().when(builderUtils).updateFromRequest(investment, updateRequest);
        when(investmentRepository.save(investment)).thenReturn(updatedInvestment);
        when(serviceUtils.getUsernameMapForFarm(anyString(), eq(farmId))).thenReturn(usernameMap);
        when(serviceUtils.toResponseDTO(anyString(), eq(updatedInvestment), eq(tokenInfo), eq(usernameMap)))
                .thenReturn(updatedResponse);

        // When
        InvestmentResponseDTO result = investmentService.update("Bearer token", 1L, updateRequest);

        // Then
        assertNotNull(result);
        assertEquals(new BigDecimal("1200.00"), result.getCurrentBalance());
        assertEquals(new BigDecimal("20.00"), result.getRoi());
        verify(validationUtils).validateCreateRequest(updateRequest);
        verify(builderUtils).updateFromRequest(investment, updateRequest);
        verify(investmentRepository).save(investment);
    }

    @Test
    void updateBalance_shouldUpdateBalanceAndReturnInvestment() {
        // Given
        BigDecimal newBalance = new BigDecimal("1500.00");

        Investment updatedInvestment = Investment.builder()
                .id(1L)
                .initialAmount(new BigDecimal("1000.00"))
                .currentBalance(new BigDecimal("1500.00"))
                .farmId(farmId)
                .userId(userId)
                .createdAt(LocalDate.now())
                .updatedAt(LocalDate.now())
                .build();

        InvestmentResponseDTO updatedResponse = InvestmentResponseDTO.builder()
                .id(1L)
                .initialAmount(new BigDecimal("1000.00"))
                .currentBalance(new BigDecimal("1500.00"))
                .roi(new BigDecimal("50.00"))
                .farmId(farmId)
                .userId(userId)
                .userName("testuser@example.com")
                .createdAt(LocalDate.now())
                .updatedAt(LocalDate.now())
                .build();

        Map<UUID, String> usernameMap = Map.of(userId, "testuser@example.com");

        when(serviceUtils.validateAndGetTokenInfoWithWriteAccess(anyString())).thenReturn(tokenInfo);
        when(serviceUtils.findByIdAndFarmId(1L, farmId)).thenReturn(investment);
        doNothing().when(serviceUtils).checkOwnershipOrMaster(investment, userId, "MASTER");
        doNothing().when(builderUtils).updateBalance(investment, newBalance);
        when(investmentRepository.save(investment)).thenReturn(updatedInvestment);
        when(serviceUtils.getUsernameMapForFarm(anyString(), eq(farmId))).thenReturn(usernameMap);
        when(serviceUtils.toResponseDTO(anyString(), eq(updatedInvestment), eq(tokenInfo), eq(usernameMap)))
                .thenReturn(updatedResponse);

        // When
        InvestmentResponseDTO result = investmentService.updateBalance("Bearer token", 1L, newBalance);

        // Then
        assertNotNull(result);
        assertEquals(new BigDecimal("1500.00"), result.getCurrentBalance());
        assertEquals(new BigDecimal("50.00"), result.getRoi());
        verify(validationUtils).validateUpdateBalance(newBalance);
        verify(builderUtils).updateBalance(investment, newBalance);
        verify(investmentRepository).save(investment);
    }

    @Test
    void delete_shouldDeleteInvestment() {
        // Given
        when(serviceUtils.validateAndGetTokenInfo(anyString())).thenReturn(tokenInfo);
        when(serviceUtils.findByIdAndFarmId(5L, farmId)).thenReturn(investment);
        doNothing().when(serviceUtils).checkOwnershipOrMaster(investment, userId, "MASTER");
        doNothing().when(investmentRepository).delete(investment);

        // When
        investmentService.delete("Bearer token", 5L);

        // Then
        verify(serviceUtils).validateAndGetTokenInfo("Bearer token");
        verify(serviceUtils).findByIdAndFarmId(5L, farmId);
        verify(serviceUtils).checkOwnershipOrMaster(investment, userId, "MASTER");
        verify(investmentRepository).delete(investment);
    }

    @Test
    void getTotalPortfolioBalance_shouldReturnSum() {
        // Given
        BigDecimal totalBalance = new BigDecimal("2500.00");

        when(serviceUtils.validateAndGetTokenInfo(anyString())).thenReturn(tokenInfo);
        when(investmentRepository.sumCurrentBalanceByFarmId(farmId)).thenReturn(totalBalance);

        // When
        BigDecimal result = investmentService.getTotalPortfolioBalance("Bearer token");

        // Then
        assertNotNull(result);
        assertEquals(new BigDecimal("2500.00"), result);
        verify(investmentRepository).sumCurrentBalanceByFarmId(farmId);
    }

    @Test
    void getTotalPortfolioBalance_whenNull_shouldReturnZero() {
        // Given
        when(serviceUtils.validateAndGetTokenInfo(anyString())).thenReturn(tokenInfo);
        when(investmentRepository.sumCurrentBalanceByFarmId(farmId)).thenReturn(null);

        // When
        BigDecimal result = investmentService.getTotalPortfolioBalance("Bearer token");

        // Then
        assertNotNull(result);
        assertEquals(BigDecimal.ZERO, result);
    }
}