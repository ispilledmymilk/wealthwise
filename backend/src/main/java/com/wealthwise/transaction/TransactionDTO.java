package com.wealthwise.transaction;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionDTO {

    private Long id;
    private BigDecimal amount;
    private CategoryType category;
    private String description;
    private LocalDate transactionDate;
    private LocalDateTime createdAt;
}
