import { Routes } from '@angular/router';
import { authGuard } from './core/auth.guard';

export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'dashboard' },
  {
    path: 'auth/login',
    loadComponent: () => import('./features/auth/login.component').then((m) => m.LoginComponent),
  },
  {
    path: 'auth/register',
    loadComponent: () => import('./features/auth/register.component').then((m) => m.RegisterComponent),
  },
  {
    path: 'dashboard',
    loadComponent: () => import('./features/dashboard/dashboard.component').then((m) => m.DashboardComponent),
    canActivate: [authGuard],
  },
  {
    path: 'transactions',
    loadComponent: () => import('./features/transactions/transactions.component').then((m) => m.TransactionsComponent),
    canActivate: [authGuard],
  },
  {
    path: 'budgets',
    loadComponent: () => import('./features/budgets/budgets.component').then((m) => m.BudgetsComponent),
    canActivate: [authGuard],
  },
  {
    path: 'alerts',
    loadComponent: () => import('./features/alerts/alerts.component').then((m) => m.AlertsComponent),
    canActivate: [authGuard],
  },
];
