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
public class BudgetDTO {

    private Long id;
    private CategoryType category;
    private BigDecimal monthlyLimit;
    private int month;
    private int year;
}
