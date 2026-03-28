export type CategoryType =
  | 'DINING'
  | 'GROCERIES'
  | 'TRANSPORT'
  | 'ENTERTAINMENT'
  | 'UTILITIES'
  | 'HEALTH'
  | 'SHOPPING'
  | 'OTHER';

export const ALL_CATEGORIES: CategoryType[] = [
  'DINING',
  'GROCERIES',
  'TRANSPORT',
  'ENTERTAINMENT',
  'UTILITIES',
  'HEALTH',
  'SHOPPING',
  'OTHER',
];

export interface SpendingSummaryDTO {
  totalSpent: number;
  byCategory: Record<string, number>;
}

export interface TransactionDTO {
  id: number;
  amount: number;
  category: CategoryType;
  description: string | null;
  transactionDate: string;
  createdAt: string | null;
}

export interface CreateTransactionPayload {
  amount: number;
  category: CategoryType;
  description?: string | null;
  transactionDate: string;
}

export interface BudgetDTO {
  id: number;
  category: CategoryType;
  monthlyLimit: number;
  month: number;
  year: number;
}

export interface BudgetStatusDTO {
  category: CategoryType;
  limit: number;
  spent: number;
  remaining: number;
  percentageUsed: number;
  status: 'ON_TRACK' | 'WARNING' | 'EXCEEDED';
}

export interface SetBudgetPayload {
  category: CategoryType;
  monthlyLimit: number;
  month: number;
  year: number;
}

export interface AlertLog {
  id: string;
  userId: number;
  alertType: string;
  category: string;
  message: string;
  triggerAmount: number;
  read: boolean;
  createdAt: string;
}
