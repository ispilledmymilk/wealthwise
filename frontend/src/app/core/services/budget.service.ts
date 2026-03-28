import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { API_URL } from '../api-url';
import { BudgetDTO, BudgetStatusDTO, SetBudgetPayload } from '../models/api.types';

@Injectable({ providedIn: 'root' })
export class BudgetService {
  private readonly http = inject(HttpClient);
  private readonly base = `${API_URL}/api/budgets`;

  getBudgets(month: number, year: number): Observable<BudgetDTO[]> {
    const params = new HttpParams().set('month', String(month)).set('year', String(year));
    return this.http.get<BudgetDTO[]>(this.base, { params });
  }

  setBudget(body: SetBudgetPayload): Observable<BudgetDTO> {
    return this.http.post<BudgetDTO>(this.base, body);
  }

  getBudgetStatus(month: number, year: number): Observable<BudgetStatusDTO[]> {
    const params = new HttpParams().set('month', String(month)).set('year', String(year));
    return this.http.get<BudgetStatusDTO[]>(`${this.base}/status`, { params });
  }
}
