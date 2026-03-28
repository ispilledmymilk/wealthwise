package com.wealthwise.budget;

import com.wealthwise.transaction.CategoryType;
import com.wealthwise.user.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BudgetRepository extends JpaRepository<Budget, Long> {

    List<Budget> findByUserAndMonthAndYear(User user, int month, int year);

    Optional<Budget> findByUserAndCategoryAndMonthAndYear(
            User user, CategoryType category, int month, int year);
}
