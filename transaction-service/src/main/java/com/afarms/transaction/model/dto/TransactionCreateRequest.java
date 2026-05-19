// TransactionCreateRequest.java (used by internal income endpoint)
package com.afarms.transaction.model.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionCreateRequest {

    private Long incomeId;

    // private Long expenseId;
    // private Long investmentId;

    @NotNull
    private UUID farmId;

    @NotNull
    private UUID userId;

    @NotNull
    @Size(min = 1, max = 255)
    private String description;

    @NotNull
    @Positive
    private BigDecimal amount;

    @NotNull
    private LocalDate occurredAt;

    private String type;
    private String authToken;
}