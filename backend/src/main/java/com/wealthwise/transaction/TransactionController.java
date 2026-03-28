package com.wealthwise.transaction;

import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @GetMapping
    public ResponseEntity<List<TransactionDTO>> getAll(
            @AuthenticationPrincipal UserDetails user,
            @RequestParam(required = false) String month,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Integer limit) {
        return ResponseEntity.ok(transactionService.getAll(user.getUsername(), month, category, limit));
    }

    @PostMapping
    public ResponseEntity<TransactionDTO> create(
            @AuthenticationPrincipal UserDetails user, @Valid @RequestBody CreateTransactionRequest request) {
        return ResponseEntity.status(201).body(transactionService.create(user.getUsername(), request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TransactionDTO> update(
            @AuthenticationPrincipal UserDetails user,
            @PathVariable Long id,
            @Valid @RequestBody CreateTransactionRequest request) {
        return ResponseEntity.ok(transactionService.update(user.getUsername(), id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal UserDetails user, @PathVariable Long id) {
        transactionService.delete(user.getUsername(), id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/summary")
    public ResponseEntity<SpendingSummaryDTO> getSummary(
            @AuthenticationPrincipal UserDetails user, @RequestParam int month, @RequestParam int year) {
        return ResponseEntity.ok(transactionService.getMonthlySummary(user.getUsername(), month, year));
    }
}
