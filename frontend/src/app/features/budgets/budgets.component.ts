import { CurrencyPipe } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ALL_CATEGORIES, BudgetDTO, BudgetStatusDTO, CategoryType } from '../../core/models/api.types';
import { BudgetService } from '../../core/services/budget.service';

@Component({
  selector: 'app-budgets',
  standalone: true,
  imports: [ReactiveFormsModule, CurrencyPipe],
  templateUrl: './budgets.component.html',
  styleUrl: './budgets.component.scss',
})
export class BudgetsComponent implements OnInit {
  private readonly budgetService = inject(BudgetService);
  private readonly fb = inject(FormBuilder);

  readonly categories = ALL_CATEGORIES;

  month = new Date().getMonth() + 1;
  year = new Date().getFullYear();

  budgets: BudgetDTO[] = [];
  statuses: BudgetStatusDTO[] = [];
  errorMessage = '';

  form = this.fb.nonNullable.group({
    category: ['DINING' as CategoryType, Validators.required],
    monthlyLimit: [100, [Validators.required, Validators.min(0.01)]],
    month: [new Date().getMonth() + 1, [Validators.required, Validators.min(1), Validators.max(12)]],
    year: [new Date().getFullYear(), [Validators.required, Validators.min(2000), Validators.max(2100)]],
  });

  ngOnInit(): void {
    this.form.patchValue({ month: this.month, year: this.year });
    this.load();
  }

  load(): void {
    this.errorMessage = '';
    this.budgetService.getBudgets(this.month, this.year).subscribe({
      next: (b) => (this.budgets = b),
      error: (e) => (this.errorMessage = e.error?.error ?? 'Failed to load budgets.'),
    });
    this.budgetService.getBudgetStatus(this.month, this.year).subscribe({
      next: (s) => (this.statuses = s),
      error: () => {
        /* optional */
      },
    });
  }

  applyPeriod(): void {
    this.month = this.form.controls.month.value;
    this.year = this.form.controls.year.value;
    this.load();
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const v = this.form.getRawValue();
    this.errorMessage = '';
    this.budgetService
      .setBudget({
        category: v.category,
        monthlyLimit: v.monthlyLimit,
        month: v.month,
        year: v.year,
      })
      .subscribe({
        next: () => this.load(),
        error: (e) => (this.errorMessage = e.error?.error ?? 'Could not save budget.'),
      });
  }
}
