package com.wealthwise.budget;

import com.wealthwise.common.exception.ResourceNotFoundException;
import com.wealthwise.transaction.CategoryType;
import com.wealthwise.transaction.TransactionRepository;
import com.wealthwise.user.User;
import com.wealthwise.user.UserRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BudgetService {

    private final BudgetRepository budgetRepository;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;

    public List<BudgetDTO> getBudgets(String email, int month, int year) {
        User user = requireUser(email);
        return budgetRepository.findByUserAndMonthAndYear(user, month, year).stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional
    public BudgetDTO setBudget(String email, SetBudgetRequest request) {
        User user = requireUser(email);
        Budget budget = budgetRepository
                .findByUserAndCategoryAndMonthAndYear(user, request.getCategory(), request.getMonth(), request.getYear())
                .orElseGet(() -> Budget.builder()
                        .user(user)
                        .category(request.getCategory())
                        .month(request.getMonth())
                        .year(request.getYear())
                        .build());
        budget.setMonthlyLimit(request.getMonthlyLimit());
        return toDto(budgetRepository.save(budget));
    }

    public List<BudgetStatusDTO> getBudgetStatus(String email, int month, int year) {
        User user = requireUser(email);
        List<Budget> budgets = budgetRepository.findByUserAndMonthAndYear(user, month, year);
        return budgets.stream().map(b -> toStatus(user, b, month, year)).toList();
    }

    private BudgetStatusDTO toStatus(User user, Budget budget, int month, int year) {
        BigDecimal spent = transactionRepository.sumByUserAndCategoryAndMonthAndYear(
                user.getId(), budget.getCategory(), month, year);
        if (spent == null) {
            spent = BigDecimal.ZERO;
        }
        BigDecimal limit = budget.getMonthlyLimit();
        BigDecimal remaining = limit.subtract(spent);
        double pct = 0;
        if (limit.signum() > 0) {
            pct = spent.divide(limit, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .doubleValue();
        }
        BudgetStatusLevel status;
        if (spent.compareTo(limit) >= 0) {
            status = BudgetStatusLevel.EXCEEDED;
        } else if (pct >= 80) {
            status = BudgetStatusLevel.WARNING;
        } else {
            status = BudgetStatusLevel.ON_TRACK;
        }
        return BudgetStatusDTO.builder()
                .category(budget.getCategory())
                .limit(limit)
                .spent(spent)
                .remaining(remaining)
                .percentageUsed(pct)
                .status(status)
                .build();
    }

    private User requireUser(String email) {
        return userRepository
                .findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private BudgetDTO toDto(Budget b) {
        return BudgetDTO.builder()
                .id(b.getId())
                .category(b.getCategory())
                .monthlyLimit(b.getMonthlyLimit())
                .month(b.getMonth())
                .year(b.getYear())
                .build();
    }
}
