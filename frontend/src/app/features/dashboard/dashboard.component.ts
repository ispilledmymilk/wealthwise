import { DecimalPipe, KeyValuePipe } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Component, inject, OnInit } from '@angular/core';
import { API_URL } from '../../core/api-url';

interface SpendingSummary {
  totalSpent: number;
  byCategory: Record<string, number>;
}

interface BudgetStatus {
  category: string;
  limit: number;
  spent: number;
  remaining: number;
  percentageUsed: number;
  status: string;
}

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [DecimalPipe, KeyValuePipe],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.scss',
})
export class DashboardComponent implements OnInit {
  private readonly http = inject(HttpClient);

  month = new Date().getMonth() + 1;
  year = new Date().getFullYear();

  summary: SpendingSummary | null = null;
  budgetStatuses: BudgetStatus[] = [];
  loadError = '';

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loadError = '';
    this.http
      .get<SpendingSummary>(`${API_URL}/api/transactions/summary`, {
        params: { month: String(this.month), year: String(this.year) },
      })
      .subscribe({
        next: (s) => (this.summary = s),
        error: (e) => (this.loadError = e.error?.error ?? 'Could not load spending summary.'),
      });

    this.http
      .get<BudgetStatus[]>(`${API_URL}/api/budgets/status`, {
        params: { month: String(this.month), year: String(this.year) },
      })
      .subscribe({
        next: (b) => (this.budgetStatuses = b),
        error: () => {
          /* optional — user may have no budgets */
        },
      });
  }
}
