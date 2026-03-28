package com.wealthwise.alert;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.wealthwise.budget.Budget;
import com.wealthwise.budget.BudgetRepository;
import com.wealthwise.transaction.CategoryType;
import com.wealthwise.transaction.TransactionRepository;
import com.wealthwise.user.User;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AlertEngineTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private BudgetRepository budgetRepository;

    @Mock
    private AlertLogRepository alertLogRepository;

    @InjectMocks
    private AlertEngine alertEngine;

    @Test
    void runForUser_budgetExceeded_firesAlert() {
        User user = User.builder().id(1L).email("u@test.com").build();
        Budget budget = Budget.builder()
                .category(CategoryType.DINING)
                .monthlyLimit(new BigDecimal("200.00"))
                .build();

        when(budgetRepository.findByUserAndMonthAndYear(any(), anyInt(), anyInt()))
                .thenReturn(List.of(budget));
        when(transactionRepository.sumByUserAndCategoryAndMonthAndYear(anyLong(), any(), anyInt(), anyInt()))
                .thenReturn(new BigDecimal("250.00"));
        when(alertLogRepository.existsByUserIdAndAlertTypeAndCategoryAndCreatedAtAfter(
                        anyLong(), anyString(), anyString(), any()))
                .thenReturn(false);
        when(transactionRepository.getWeeklyTotals(anyLong(), anyString(), anyInt(), anyInt()))
                .thenReturn(Collections.emptyList());

        alertEngine.runForUser(user);

        verify(alertLogRepository, times(1))
                .save(ArgumentMatchers.argThat(alert -> "BUDGET_EXCEEDED".equals(alert.getAlertType())));
    }

    @Test
    void runForUser_atEightyPercent_firesOverspendWarning() {
        User user = User.builder().id(2L).email("v@test.com").build();
        Budget budget = Budget.builder()
                .category(CategoryType.GROCERIES)
                .monthlyLimit(new BigDecimal("100.00"))
                .build();

        when(budgetRepository.findByUserAndMonthAndYear(any(), anyInt(), anyInt()))
                .thenReturn(List.of(budget));
        when(transactionRepository.sumByUserAndCategoryAndMonthAndYear(anyLong(), any(), anyInt(), anyInt()))
                .thenReturn(new BigDecimal("80.00"));
        when(alertLogRepository.existsByUserIdAndAlertTypeAndCategoryAndCreatedAtAfter(
                        anyLong(), anyString(), anyString(), any()))
                .thenReturn(false);
        when(transactionRepository.getWeeklyTotals(anyLong(), anyString(), anyInt(), anyInt()))
                .thenReturn(Collections.emptyList());

        alertEngine.runForUser(user);

        verify(alertLogRepository, times(1))
                .save(ArgumentMatchers.argThat(alert -> "OVERSPEND_WARNING".equals(alert.getAlertType())));
    }
}
