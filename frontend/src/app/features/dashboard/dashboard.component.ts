import { CurrencyPipe, DatePipe, KeyValuePipe, NgClass } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { ChartData } from 'chart.js';
import { NgChartsModule } from 'ng2-charts';
import { forkJoin } from 'rxjs';
import {
  AlertLog,
  BudgetStatusDTO,
  SpendingSummaryDTO,
  TransactionDTO,
} from '../../core/models/api.types';
import { AlertService } from '../../core/services/alert.service';
import { BudgetService } from '../../core/services/budget.service';
import { TransactionService } from '../../core/services/transaction.service';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CurrencyPipe, DatePipe, KeyValuePipe, NgChartsModule, NgClass],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.scss',
})
export class DashboardComponent implements OnInit {
  private readonly transactionService = inject(TransactionService);
  private readonly budgetService = inject(BudgetService);
  private readonly alertService = inject(AlertService);

  spendingSummary: SpendingSummaryDTO | null = null;
  budgetStatuses: BudgetStatusDTO[] = [];
  recentTransactions: TransactionDTO[] = [];
  unreadAlerts: AlertLog[] = [];
  transactionCount = 0;
  budgetsOnTrack = 0;
  loadError = '';

  currentMonth = new Date().getMonth() + 1;
  currentYear = new Date().getFullYear();

  spendingChartData: ChartData<'doughnut'> = { labels: [], datasets: [] };
  trendChartData: ChartData<'line'> = { labels: [], datasets: [] };

  ngOnInit(): void {
    this.loadDashboard();
  }

  alertClass(type: string): string {
    switch (type) {
      case 'BUDGET_EXCEEDED':
        return 'alert-danger';
      case 'OVERSPEND_WARNING':
        return 'alert-warning';
      default:
        return 'alert-info';
    }
  }

  loadDashboard(): void {
    this.loadError = '';
    const ym = `${this.currentYear}-${String(this.currentMonth).padStart(2, '0')}`;
    forkJoin({
      summary: this.transactionService.getSummary(this.currentMonth, this.currentYear),
      budgets: this.budgetService.getBudgetStatus(this.currentMonth, this.currentYear),
      transactions: this.transactionService.getAll({ month: ym }),
      alerts: this.alertService.getUnread(),
    }).subscribe({
      next: ({ summary, budgets, transactions, alerts }) => {
        this.spendingSummary = summary;
        this.budgetStatuses = budgets;
        this.transactionCount = transactions.length;
        this.recentTransactions = transactions.slice(0, 5);
        this.unreadAlerts = alerts;
        this.budgetsOnTrack = budgets.filter((b) => b.status === 'ON_TRACK').length;
        this.buildCharts(summary, transactions);
      },
      error: (err) => {
        this.loadError = err.error?.error ?? err.message ?? 'Failed to load dashboard.';
      },
    });
  }

  private buildCharts(summary: SpendingSummaryDTO, monthTxs: TransactionDTO[]): void {
    const entries = Object.entries(summary.byCategory).filter(([, v]) => Number(v) > 0);
    this.spendingChartData = {
      labels: entries.map(([k]) => k),
      datasets: [
        {
          data: entries.map(([, v]) => Number(v)),
          backgroundColor: [
            '#6366f1',
            '#f59e0b',
            '#10b981',
            '#ef4444',
            '#3b82f6',
            '#8b5cf6',
            '#ec4899',
            '#14b8a6',
          ],
        },
      ],
    };

    const byDay = new Map<string, number>();
    for (const t of monthTxs) {
      const d = t.transactionDate;
      byDay.set(d, (byDay.get(d) ?? 0) + Number(t.amount));
    }
    const days = [...byDay.keys()].sort();
    this.trendChartData = {
      labels: days,
      datasets: [
        {
          label: 'Daily spend',
          data: days.map((d) => byDay.get(d) ?? 0),
          borderColor: '#3b82f6',
          backgroundColor: 'rgba(59, 130, 246, 0.15)',
          fill: true,
          tension: 0.3,
        },
      ],
    };
  }
}
