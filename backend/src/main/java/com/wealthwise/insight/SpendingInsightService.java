package com.wealthwise.insight;

import com.wealthwise.transaction.Transaction;
import com.wealthwise.transaction.TransactionRepository;
import com.wealthwise.user.User;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SpendingInsightService {

    private final TransactionRepository transactionRepository;
    private final SpendingInsightRepository spendingInsightRepository;

    public void generateSnapshot(User user) {
        int month = LocalDate.now().getMonthValue();
        int year = LocalDate.now().getYear();
        List<Transaction> txs = transactionRepository.findByUserIdAndMonthYear(user.getId(), month, year);
        Map<String, BigDecimal> byCategory = new HashMap<>();
        BigDecimal total = BigDecimal.ZERO;
        for (Transaction t : txs) {
            total = total.add(t.getAmount());
            byCategory.merge(t.getCategory().name(), t.getAmount(), BigDecimal::add);
        }
        spendingInsightRepository.save(SpendingInsight.builder()
                .userId(user.getId())
                .month(month)
                .year(year)
                .categoryTotals(byCategory)
                .monthTotal(total)
                .createdAt(LocalDateTime.now())
                .build());
    }
}
