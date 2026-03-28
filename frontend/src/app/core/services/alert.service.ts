import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { API_URL } from '../api-url';
import { AlertLog } from '../models/api.types';

@Injectable({ providedIn: 'root' })
export class AlertService {
  private readonly http = inject(HttpClient);
  private readonly base = `${API_URL}/api/alerts`;

  getAll(): Observable<AlertLog[]> {
    return this.http.get<AlertLog[]>(this.base);
  }

  getUnread(): Observable<AlertLog[]> {
    return this.http.get<AlertLog[]>(`${this.base}/unread`);
  }

  markRead(id: string): Observable<void> {
    return this.http.put<void>(`${this.base}/${id}/read`, {});
  }
}
