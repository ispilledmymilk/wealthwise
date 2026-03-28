import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { API_URL } from '../api-url';
import {
  CreateTransactionPayload,
  SpendingSummaryDTO,
  TransactionDTO,
} from '../models/api.types';

export interface TransactionListParams {
  month?: string;
  category?: string;
  limit?: number;
}

@Injectable({ providedIn: 'root' })
export class TransactionService {
  private readonly http = inject(HttpClient);
  private readonly base = `${API_URL}/api/transactions`;

  getSummary(month: number, year: number): Observable<SpendingSummaryDTO> {
    const params = new HttpParams().set('month', String(month)).set('year', String(year));
    return this.http.get<SpendingSummaryDTO>(`${this.base}/summary`, { params });
  }

  getAll(params?: TransactionListParams): Observable<TransactionDTO[]> {
    let hp = new HttpParams();
    if (params?.month) hp = hp.set('month', params.month);
    if (params?.category) hp = hp.set('category', params.category);
    if (params?.limit != null) hp = hp.set('limit', String(params.limit));
    return this.http.get<TransactionDTO[]>(this.base, { params: hp });
  }

  create(body: CreateTransactionPayload): Observable<TransactionDTO> {
    return this.http.post<TransactionDTO>(this.base, body);
  }

  update(id: number, body: CreateTransactionPayload): Observable<TransactionDTO> {
    return this.http.put<TransactionDTO>(`${this.base}/${id}`, body);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/${id}`);
  }
}
