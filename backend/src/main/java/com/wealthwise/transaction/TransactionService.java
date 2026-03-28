package com.wealthwise.transaction;

import com.wealthwise.common.exception.ResourceNotFoundException;
import com.wealthwise.user.User;
import com.wealthwise.user.UserRepository;
import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    public List<TransactionDTO> getAll(String email, String month, String category, Integer limit) {
        User user = requireUser(email);
        List<Transaction> list;
        if (month != null && month.matches("\\d{4}-\\d{2}")) {
            YearMonth ym = YearMonth.parse(month);
            list = transactionRepository.findByUserIdAndMonthYear(user.getId(), ym.getMonthValue(), ym.getYear());
        } else {
            list = transactionRepository.findByUserIdOrderByTransactionDateDesc(user.getId());
        }
        if (category != null && !category.isBlank()) {
            CategoryType cat = CategoryType.valueOf(category.trim().toUpperCase());
            list = list.stream().filter(t -> t.getCategory() == cat).toList();
        }
        if (limit != null && limit > 0) {
            list = list.stream().limit(limit).toList();
        }
        return list.stream().map(this::toDto).toList();
    }

    @Transactional
    public TransactionDTO create(String email, CreateTransactionRequest request) {
        User user = requireUser(email);
        Transaction tx = Transaction.builder()
                .user(user)
                .amount(request.getAmount())
                .category(request.getCategory())
                .description(request.getDescription())
                .transactionDate(request.getTransactionDate())
                .build();
        return toDto(transactionRepository.save(tx));
    }

    @Transactional
    public TransactionDTO update(String email, Long id, CreateTransactionRequest request) {
        Transaction tx = requireOwnedTransaction(email, id);
        tx.setAmount(request.getAmount());
        tx.setCategory(request.getCategory());
        tx.setDescription(request.getDescription());
        tx.setTransactionDate(request.getTransactionDate());
        return toDto(transactionRepository.save(tx));
    }

    @Transactional
    public void delete(String email, Long id) {
        Transaction tx = requireOwnedTransaction(email, id);
        transactionRepository.delete(tx);
    }

    public SpendingSummaryDTO getMonthlySummary(String email, int month, int year) {
        User user = requireUser(email);
        List<Transaction> txs =
                transactionRepository.findByUserIdAndMonthYear(user.getId(), month, year);
        Map<String, BigDecimal> byCategory = new HashMap<>();
        for (CategoryType c : CategoryType.values()) {
            byCategory.put(c.name(), BigDecimal.ZERO);
        }
        BigDecimal total = BigDecimal.ZERO;
        for (Transaction t : txs) {
            total = total.add(t.getAmount());
            byCategory.merge(t.getCategory().name(), t.getAmount(), BigDecimal::add);
        }
        return SpendingSummaryDTO.builder().totalSpent(total).byCategory(byCategory).build();
    }

    private User requireUser(String email) {
        return userRepository
                .findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private Transaction requireOwnedTransaction(String email, Long id) {
        User user = requireUser(email);
        Transaction tx = transactionRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));
        if (!tx.getUser().getId().equals(user.getId())) {
            throw new ResourceNotFoundException("Transaction not found");
        }
        return tx;
    }

    private TransactionDTO toDto(Transaction t) {
        return TransactionDTO.builder()
                .id(t.getId())
                .amount(t.getAmount())
                .category(t.getCategory())
                .description(t.getDescription())
                .transactionDate(t.getTransactionDate())
                .createdAt(t.getCreatedAt())
                .build();
    }
}
