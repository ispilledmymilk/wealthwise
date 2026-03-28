import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Chart, registerables } from 'chart.js';
import { of } from 'rxjs';
import { AlertService } from '../../core/services/alert.service';
import { BudgetService } from '../../core/services/budget.service';
import { TransactionService } from '../../core/services/transaction.service';
import { DashboardComponent } from './dashboard.component';

describe('DashboardComponent', () => {
  let component: DashboardComponent;
  let fixture: ComponentFixture<DashboardComponent>;
  let mockTransactionService: jasmine.SpyObj<TransactionService>;
  let mockBudgetService: jasmine.SpyObj<BudgetService>;
  let mockAlertService: jasmine.SpyObj<AlertService>;

  beforeAll(() => {
    Chart.register(...registerables);
  });

  beforeEach(async () => {
    mockTransactionService = jasmine.createSpyObj('TransactionService', ['getSummary', 'getAll']);
    mockBudgetService = jasmine.createSpyObj('BudgetService', ['getBudgetStatus']);
    mockAlertService = jasmine.createSpyObj('AlertService', ['getUnread']);

    mockTransactionService.getSummary.and.returnValue(of({ totalSpent: 500, byCategory: {} }));
    mockBudgetService.getBudgetStatus.and.returnValue(of([]));
    mockTransactionService.getAll.and.returnValue(of([]));
    mockAlertService.getUnread.and.returnValue(of([]));

    await TestBed.configureTestingModule({
      imports: [DashboardComponent],
      providers: [
        { provide: TransactionService, useValue: mockTransactionService },
        { provide: BudgetService, useValue: mockBudgetService },
        { provide: AlertService, useValue: mockAlertService },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(DashboardComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should load dashboard data on init', () => {
    expect(mockTransactionService.getSummary).toHaveBeenCalled();
    expect(mockBudgetService.getBudgetStatus).toHaveBeenCalled();
    expect(mockTransactionService.getAll).toHaveBeenCalled();
    expect(mockAlertService.getUnread).toHaveBeenCalled();
    expect(component.spendingSummary?.totalSpent).toBe(500);
  });
});
