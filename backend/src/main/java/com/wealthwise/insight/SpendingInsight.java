package com.wealthwise.insight;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "spending_insights")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpendingInsight {

    @Id
    private String id;

    private Long userId;
    private int month;
    private int year;
    private Map<String, BigDecimal> categoryTotals;
    private BigDecimal monthTotal;
    private LocalDateTime createdAt;
}
