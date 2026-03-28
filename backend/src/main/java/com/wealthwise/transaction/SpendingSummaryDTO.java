package com.wealthwise.transaction;

import java.math.BigDecimal;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpendingSummaryDTO {

    private BigDecimal totalSpent;
    private Map<String, BigDecimal> byCategory;
}
