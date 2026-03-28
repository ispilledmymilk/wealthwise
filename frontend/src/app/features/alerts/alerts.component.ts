import { DatePipe } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { AlertLog } from '../../core/models/api.types';
import { AlertService } from '../../core/services/alert.service';

@Component({
  selector: 'app-alerts',
  standalone: true,
  imports: [DatePipe],
  templateUrl: './alerts.component.html',
  styleUrl: './alerts.component.scss',
})
export class AlertsComponent implements OnInit {
  private readonly alertsApi = inject(AlertService);

  alerts: AlertLog[] = [];
  errorMessage = '';

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.errorMessage = '';
    this.alertsApi.getAll().subscribe({
      next: (a) => (this.alerts = a),
      error: (e) => (this.errorMessage = e.error?.error ?? 'Failed to load alerts.'),
    });
  }

  markRead(a: AlertLog): void {
    if (a.read) return;
    this.alertsApi.markRead(a.id).subscribe({
      next: () => this.load(),
      error: (e) => (this.errorMessage = e.error?.error ?? 'Could not update alert.'),
    });
  }
}
