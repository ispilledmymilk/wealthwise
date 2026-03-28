package com.wealthwise.budget;

import com.wealthwise.transaction.CategoryType;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BudgetStatusDTO {

    private CategoryType category;
    private BigDecimal limit;
    private BigDecimal spent;
    private BigDecimal remaining;
    private double percentageUsed;
    private BudgetStatusLevel status;
}
