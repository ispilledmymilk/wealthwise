package com.wealthwise.budget;

import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/budgets")
@RequiredArgsConstructor
public class BudgetController {

    private final BudgetService budgetService;

    @GetMapping
    public ResponseEntity<List<BudgetDTO>> getBudgets(
            @AuthenticationPrincipal UserDetails user, @RequestParam int month, @RequestParam int year) {
        return ResponseEntity.ok(budgetService.getBudgets(user.getUsername(), month, year));
    }

    @PostMapping
    public ResponseEntity<BudgetDTO> setBudget(
            @AuthenticationPrincipal UserDetails user, @Valid @RequestBody SetBudgetRequest request) {
        return ResponseEntity.ok(budgetService.setBudget(user.getUsername(), request));
    }

    @GetMapping("/status")
    public ResponseEntity<List<BudgetStatusDTO>> getBudgetStatus(
            @AuthenticationPrincipal UserDetails user, @RequestParam int month, @RequestParam int year) {
        return ResponseEntity.ok(budgetService.getBudgetStatus(user.getUsername(), month, year));
    }
}
