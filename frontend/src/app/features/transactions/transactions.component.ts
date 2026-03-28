import { CurrencyPipe, DatePipe } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ALL_CATEGORIES, CategoryType, TransactionDTO } from '../../core/models/api.types';
import { TransactionService } from '../../core/services/transaction.service';

@Component({
  selector: 'app-transactions',
  standalone: true,
  imports: [ReactiveFormsModule, CurrencyPipe, DatePipe],
  templateUrl: './transactions.component.html',
  styleUrl: './transactions.component.scss',
})
export class TransactionsComponent implements OnInit {
  private readonly tx = inject(TransactionService);
  private readonly fb = inject(FormBuilder);

  readonly categories = ALL_CATEGORIES;

  transactions: TransactionDTO[] = [];
  errorMessage = '';
  editingId: number | null = null;

  form = this.fb.nonNullable.group({
    amount: [1, [Validators.required, Validators.min(0.01)]],
    category: ['DINING' as CategoryType, Validators.required],
    description: [''],
    transactionDate: [new Date().toISOString().slice(0, 10), Validators.required],
  });

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.errorMessage = '';
    this.tx.getAll().subscribe({
      next: (list) => (this.transactions = list),
      error: (e) => (this.errorMessage = e.error?.error ?? 'Failed to load transactions.'),
    });
  }

  startCreate(): void {
    this.editingId = null;
    this.form.reset({
      amount: 1,
      category: 'DINING',
      description: '',
      transactionDate: new Date().toISOString().slice(0, 10),
    });
  }

  startEdit(t: TransactionDTO): void {
    this.editingId = t.id;
    this.form.patchValue({
      amount: Number(t.amount),
      category: t.category,
      description: t.description ?? '',
      transactionDate: t.transactionDate,
    });
  }

  cancelEdit(): void {
    this.editingId = null;
    this.startCreate();
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const v = this.form.getRawValue();
    const body = {
      amount: v.amount,
      category: v.category,
      description: v.description || null,
      transactionDate: v.transactionDate,
    };
    this.errorMessage = '';
    const req =
      this.editingId == null
        ? this.tx.create(body)
        : this.tx.update(this.editingId, body);
    req.subscribe({
      next: () => {
        this.load();
        this.startCreate();
      },
      error: (e) => (this.errorMessage = e.error?.error ?? 'Save failed.'),
    });
  }

  remove(t: TransactionDTO): void {
    if (!confirm(`Delete transaction #${t.id}?`)) return;
    this.tx.delete(t.id).subscribe({
      next: () => this.load(),
      error: (e) => (this.errorMessage = e.error?.error ?? 'Delete failed.'),
    });
  }
}
