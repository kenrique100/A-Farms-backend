package com.afarms.transaction.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionCreateRequestDTO {
    @NotBlank
    private String type;
    @NotNull
    private Long referenceId;
    @NotNull
    private LocalDate date;
    @NotNull
    @Positive
    private BigDecimal amount;
    private String createdBy;
    @NotNull
    private UUID farmId;
    @NotNull
    private UUID userId;
}
