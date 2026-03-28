package com.wealthwise.transaction;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    @Query(
            "select coalesce(sum(t.amount), 0) from Transaction t where t.user.id = :userId and t.category = :category "
                    + "and year(t.transactionDate) = :year and month(t.transactionDate) = :month")
    BigDecimal sumByUserAndCategoryAndMonthAndYear(
            @Param("userId") Long userId,
            @Param("category") CategoryType category,
            @Param("month") int month,
            @Param("year") int year);

    @Query("select t from Transaction t where t.user.id = :userId order by t.transactionDate desc, t.id desc")
    List<Transaction> findByUserIdOrderByTransactionDateDesc(@Param("userId") Long userId);

    @Query(
            "select t from Transaction t where t.user.id = :userId and year(t.transactionDate) = :year "
                    + "and month(t.transactionDate) = :month order by t.transactionDate desc, t.id desc")
    List<Transaction> findByUserIdAndMonthYear(
            @Param("userId") Long userId, @Param("month") int month, @Param("year") int year);

    @Query(
            value =
                    """
            select coalesce(sum(t.amount), 0)
            from transactions t
            where t.user_id = :userId
              and t.category = cast(:category as category_type)
              and extract(month from t.transaction_date) = :month
              and extract(year from t.transaction_date) = :year
            group by ((cast(extract(day from t.transaction_date) as integer) - 1) / 7)
            order by min((cast(extract(day from t.transaction_date) as integer) - 1) / 7)
            """,
            nativeQuery = true)
    List<BigDecimal> getWeeklyTotals(
            @Param("userId") Long userId,
            @Param("category") String category,
            @Param("month") int month,
            @Param("year") int year);
}
