package com.wealthwise.alert;

import com.wealthwise.budget.Budget;
import com.wealthwise.budget.BudgetRepository;
import com.wealthwise.transaction.CategoryType;
import com.wealthwise.transaction.TransactionRepository;
import com.wealthwise.user.User;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AlertEngine {

    private final TransactionRepository transactionRepository;
    private final BudgetRepository budgetRepository;
    private final AlertLogRepository alertLogRepository;

    public void runForUser(User user) {
        int currentMonth = LocalDate.now().getMonthValue();
        int currentYear = LocalDate.now().getYear();

        List<Budget> budgets = budgetRepository.findByUserAndMonthAndYear(user, currentMonth, currentYear);

        for (Budget budget : budgets) {
            if (budget.getMonthlyLimit() == null || budget.getMonthlyLimit().signum() <= 0) {
                continue;
            }
            BigDecimal spent = transactionRepository.sumByUserAndCategoryAndMonthAndYear(
                    user.getId(), budget.getCategory(), currentMonth, currentYear);
            if (spent == null) {
                spent = BigDecimal.ZERO;
            }

            double percentage = spent.divide(budget.getMonthlyLimit(), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .doubleValue();

            if (percentage >= 100) {
                fireAlert(
                        user,
                        "BUDGET_EXCEEDED",
                        budget.getCategory().name(),
                        String.format(
                                "You've exceeded your %s budget by $%.2f",
                                budget.getCategory().name(),
                                spent.subtract(budget.getMonthlyLimit()).abs()),
                        spent);
            } else if (percentage >= 80) {
                fireAlert(
                        user,
                        "OVERSPEND_WARNING",
                        budget.getCategory().name(),
                        String.format(
                                "You've used %.0f%% of your %s budget", percentage, budget.getCategory().name()),
                        spent);
            }
        }

        detectSpendingPattern(user, currentMonth, currentYear);
    }

    private void detectSpendingPattern(User user, int month, int year) {
        for (CategoryType category : CategoryType.values()) {
            List<BigDecimal> weeklyTotals =
                    transactionRepository.getWeeklyTotals(user.getId(), category.name(), month, year);

            if (weeklyTotals.size() >= 3) {
                boolean risingPattern = IntStream.range(1, weeklyTotals.size())
                        .allMatch(i -> weeklyTotals.get(i).compareTo(weeklyTotals.get(i - 1)) > 0);

                if (risingPattern) {
                    fireAlert(
                            user,
                            "PATTERN_DETECTED",
                            category.name(),
                            String.format(
                                    "Your %s spending has increased every week this month — consider reviewing",
                                    category.name()),
                            weeklyTotals.get(weeklyTotals.size() - 1));
                }
            }
        }
    }

    private void fireAlert(User user, String type, String category, String message, BigDecimal amount) {
        boolean exists = alertLogRepository.existsByUserIdAndAlertTypeAndCategoryAndCreatedAtAfter(
                user.getId(), type, category, LocalDateTime.now().toLocalDate().atStartOfDay());

        if (!exists) {
            alertLogRepository.save(AlertLog.builder()
                    .userId(user.getId())
                    .alertType(type)
                    .category(category)
                    .message(message)
                    .triggerAmount(amount)
                    .read(false)
                    .createdAt(LocalDateTime.now())
                    .build());
            log.info("Alert fired for user {}: {} - {}", user.getEmail(), type, message);
        }
    }
}
