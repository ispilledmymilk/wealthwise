# wealthwise
# WealthWise — Cursor AI Project Blueprint
> Full-stack FinTech platform | Angular + SpringBoot + PostgreSQL + MongoDB

This file is a complete step-by-step instruction set for Cursor AI to scaffold, build, and wire up the WealthWise project from scratch. Follow each phase in order. Do not skip phases.

---

## 🧠 Project Overview

**WealthWise** is a personal finance intelligence platform that:
- Ingests and categorizes transactions
- Tracks budgets per category
- Runs a nightly rule-based alert engine to detect overspending patterns
- Surfaces smart alerts and predictions on a real-time Angular dashboard
- Uses JWT authentication for secure multi-user support

---

## 🗂️ Tech Stack

| Layer | Technology |
|---|---|
| Frontend | Angular 17, TypeScript, Bootstrap 5, Chart.js |
| Backend | Java 17, Spring Boot 3, Spring Security, Spring Scheduler |
| Primary DB | PostgreSQL |
| Secondary DB | MongoDB |
| Testing (BE) | JUnit 5, PowerMock, Mockito |
| Testing (FE) | Jasmine, Karma |
| API Testing | Postman (collection exported to `/postman`) |
| DevOps | Docker, Docker Compose |

---

## 📁 Final Project Structure

```
wealthwise/
├── frontend/                          # Angular app
│   ├── src/
│   │   ├── app/
│   │   │   ├── core/                  # Auth guards, interceptors, services
│   │   │   ├── shared/                # Reusable components, pipes, directives
│   │   │   ├── features/
│   │   │   │   ├── auth/              # Login, Register pages
│   │   │   │   ├── dashboard/         # Main dashboard with charts
│   │   │   │   ├── transactions/      # Transaction list, add/edit
│   │   │   │   ├── budgets/           # Budget management
│   │   │   │   └── alerts/            # Alert center
│   │   │   ├── app-routing.module.ts
│   │   │   └── app.module.ts
│   │   ├── assets/
│   │   └── environments/
│   ├── angular.json
│   └── package.json
│
├── backend/                           # Spring Boot app
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/wealthwise/
│   │   │   │   ├── auth/              # JWT auth, login, register
│   │   │   │   ├── user/              # User entity & service
│   │   │   │   ├── transaction/       # Transaction CRUD
│   │   │   │   ├── budget/            # Budget CRUD
│   │   │   │   ├── alert/             # Alert engine + MongoDB logs
│   │   │   │   ├── scheduler/         # Nightly batch jobs
│   │   │   │   └── config/            # Security, CORS, DB config
│   │   │   └── resources/
│   │   │       ├── application.yml
│   │   │       └── db/migration/      # Flyway SQL migrations
│   │   └── test/                      # JUnit + PowerMock tests
│   ├── pom.xml
│   └── Dockerfile
│
├── postman/
│   └── WealthWise.postman_collection.json
│
├── docker-compose.yml
└── README.md
```

---

## 🚀 PHASE 1 — Project Initialization

### 1.1 Root Setup
```bash
mkdir wealthwise && cd wealthwise
git init
echo "node_modules/\ntarget/\n.env\n*.class" > .gitignore
```

### 1.2 Frontend — Angular
```bash
npm install -g @angular/cli
ng new frontend --routing=true --style=scss --strict=true
cd frontend
npm install bootstrap chart.js ng2-charts @auth0/angular-jwt axios
```

Add to `angular.json` styles array:
```json
"node_modules/bootstrap/dist/css/bootstrap.min.css"
```

### 1.3 Backend — Spring Boot
Use [start.spring.io](https://start.spring.io) or Cursor to generate with:
- **Group**: `com.wealthwise`
- **Artifact**: `backend`
- **Java**: 17
- **Dependencies**: Spring Web, Spring Security, Spring Data JPA, Spring Data MongoDB, Spring Scheduler, PostgreSQL Driver, Lombok, Validation, JWT (jjwt)

Or scaffold `pom.xml` directly — see Phase 2.

---

## 🗄️ PHASE 2 — Database Setup

### 2.1 docker-compose.yml (run this first)
```yaml
version: '3.8'
services:
  postgres:
    image: postgres:15
    container_name: wealthwise_postgres
    environment:
      POSTGRES_DB: wealthwise
      POSTGRES_USER: wealthuser
      POSTGRES_PASSWORD: wealthpass
    ports:
      - "5432:5432"
    volumes:
      - pgdata:/var/lib/postgresql/data

  mongodb:
    image: mongo:6
    container_name: wealthwise_mongo
    ports:
      - "27017:27017"
    volumes:
      - mongodata:/data/db

volumes:
  pgdata:
  mongodata:
```

Run with:
```bash
docker-compose up -d
```

### 2.2 PostgreSQL Schema (Flyway Migrations)
Create files in `backend/src/main/resources/db/migration/`:

**V1__create_users.sql**
```sql
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(255),
    created_at TIMESTAMP DEFAULT NOW()
);
```

**V2__create_transactions.sql**
```sql
CREATE TYPE category_type AS ENUM (
    'DINING', 'GROCERIES', 'TRANSPORT', 'ENTERTAINMENT',
    'UTILITIES', 'HEALTH', 'SHOPPING', 'OTHER'
);

CREATE TABLE transactions (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
    amount DECIMAL(10,2) NOT NULL,
    category category_type NOT NULL,
    description VARCHAR(500),
    transaction_date DATE NOT NULL,
    created_at TIMESTAMP DEFAULT NOW()
);
```

**V3__create_budgets.sql**
```sql
CREATE TABLE budgets (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
    category category_type NOT NULL,
    monthly_limit DECIMAL(10,2) NOT NULL,
    month INT NOT NULL,
    year INT NOT NULL,
    UNIQUE(user_id, category, month, year)
);
```

### 2.3 MongoDB Collections (auto-created by Spring)
Two collections will be created automatically:
- `alert_logs` — stores fired alerts per user
- `spending_insights` — stores nightly analysis snapshots

---

## ⚙️ PHASE 3 — Backend Implementation

### 3.1 application.yml
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/wealthwise
    username: wealthuser
    password: wealthpass
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false
  data:
    mongodb:
      uri: mongodb://localhost:27017/wealthwise
  flyway:
    enabled: true

app:
  jwt:
    secret: your-super-secret-jwt-key-minimum-256-bits
    expiration: 86400000 # 24 hours

server:
  port: 8080
```

### 3.2 JWT Auth — Files to Create

**`auth/JwtUtil.java`**
```java
@Component
public class JwtUtil {
    @Value("${app.jwt.secret}")
    private String secret;

    @Value("${app.jwt.expiration}")
    private long expiration;

    public String generateToken(UserDetails userDetails) {
        return Jwts.builder()
            .setSubject(userDetails.getUsername())
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + expiration))
            .signWith(Keys.hmacShaKeyFor(secret.getBytes()), SignatureAlgorithm.HS256)
            .compact();
    }

    public String extractUsername(String token) {
        return Jwts.parserBuilder()
            .setSigningKey(Keys.hmacShaKeyFor(secret.getBytes()))
            .build()
            .parseClaimsJws(token)
            .getBody()
            .getSubject();
    }

    public boolean validateToken(String token, UserDetails userDetails) {
        return extractUsername(token).equals(userDetails.getUsername())
            && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return Jwts.parserBuilder()
            .setSigningKey(Keys.hmacShaKeyFor(secret.getBytes()))
            .build()
            .parseClaimsJws(token)
            .getBody()
            .getExpiration()
            .before(new Date());
    }
}
```

**`auth/AuthController.java`**
```java
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
}
```

**DTOs to create:**
- `RegisterRequest` — email, password, fullName
- `LoginRequest` — email, password
- `AuthResponse` — token, email, fullName

### 3.3 Transaction Module

**`transaction/Transaction.java`** (Entity)
```java
@Entity
@Table(name = "transactions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "category_type")
    private CategoryType category;

    private String description;
    private LocalDate transactionDate;
    private LocalDateTime createdAt;
}
```

**`transaction/TransactionController.java`**
```java
@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @GetMapping
    public ResponseEntity<List<TransactionDTO>> getAll(
            @AuthenticationPrincipal UserDetails user,
            @RequestParam(required = false) String month,
            @RequestParam(required = false) String category) {
        return ResponseEntity.ok(transactionService.getAll(user.getUsername(), month, category));
    }

    @PostMapping
    public ResponseEntity<TransactionDTO> create(
            @AuthenticationPrincipal UserDetails user,
            @Valid @RequestBody CreateTransactionRequest request) {
        return ResponseEntity.status(201).body(transactionService.create(user.getUsername(), request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TransactionDTO> update(
            @AuthenticationPrincipal UserDetails user,
            @PathVariable Long id,
            @Valid @RequestBody CreateTransactionRequest request) {
        return ResponseEntity.ok(transactionService.update(user.getUsername(), id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal UserDetails user,
            @PathVariable Long id) {
        transactionService.delete(user.getUsername(), id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/summary")
    public ResponseEntity<SpendingSummaryDTO> getSummary(
            @AuthenticationPrincipal UserDetails user,
            @RequestParam int month,
            @RequestParam int year) {
        return ResponseEntity.ok(transactionService.getMonthlySummary(user.getUsername(), month, year));
    }
}
```

### 3.4 Budget Module

**`budget/BudgetController.java`**
```java
@RestController
@RequestMapping("/api/budgets")
@RequiredArgsConstructor
public class BudgetController {

    private final BudgetService budgetService;

    @GetMapping
    public ResponseEntity<List<BudgetDTO>> getBudgets(
            @AuthenticationPrincipal UserDetails user,
            @RequestParam int month,
            @RequestParam int year) {
        return ResponseEntity.ok(budgetService.getBudgets(user.getUsername(), month, year));
    }

    @PostMapping
    public ResponseEntity<BudgetDTO> setBudget(
            @AuthenticationPrincipal UserDetails user,
            @Valid @RequestBody SetBudgetRequest request) {
        return ResponseEntity.ok(budgetService.setBudget(user.getUsername(), request));
    }

    @GetMapping("/status")
    public ResponseEntity<List<BudgetStatusDTO>> getBudgetStatus(
            @AuthenticationPrincipal UserDetails user,
            @RequestParam int month,
            @RequestParam int year) {
        return ResponseEntity.ok(budgetService.getBudgetStatus(user.getUsername(), month, year));
    }
}
```

**`BudgetStatusDTO`** should include:
- category, limit, spent, remaining, percentageUsed, status (ON_TRACK / WARNING / EXCEEDED)

### 3.5 Alert Engine

**`alert/AlertLog.java`** (MongoDB Document)
```java
@Document(collection = "alert_logs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertLog {
    @Id
    private String id;
    private Long userId;
    private String alertType;       // OVERSPEND_WARNING, BUDGET_EXCEEDED, PATTERN_DETECTED
    private String category;
    private String message;
    private BigDecimal triggerAmount;
    private boolean read;
    private LocalDateTime createdAt;
}
```

**`alert/AlertEngine.java`** (Core Rule Engine)
```java
@Service
@RequiredArgsConstructor
@Slf4j
public class AlertEngine {

    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final BudgetRepository budgetRepository;
    private final AlertLogRepository alertLogRepository;

    public void runForUser(User user) {
        int currentMonth = LocalDate.now().getMonthValue();
        int currentYear = LocalDate.now().getYear();

        List<Budget> budgets = budgetRepository.findByUserAndMonthAndYear(user, currentMonth, currentYear);

        for (Budget budget : budgets) {
            BigDecimal spent = transactionRepository
                .sumByUserAndCategoryAndMonthAndYear(user, budget.getCategory(), currentMonth, currentYear);

            if (spent == null) spent = BigDecimal.ZERO;

            double percentage = spent.divide(budget.getMonthlyLimit(), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100)).doubleValue();

            // Rule 1: Budget exceeded
            if (percentage >= 100) {
                fireAlert(user, "BUDGET_EXCEEDED", budget.getCategory().name(),
                    String.format("You've exceeded your %s budget by $%.2f",
                        budget.getCategory(), spent.subtract(budget.getMonthlyLimit()).abs()),
                    spent);
            }
            // Rule 2: 80% warning
            else if (percentage >= 80) {
                fireAlert(user, "OVERSPEND_WARNING", budget.getCategory().name(),
                    String.format("You've used %.0f%% of your %s budget", percentage, budget.getCategory()),
                    spent);
            }
        }

        // Rule 3: Detect 3-week overspend pattern
        detectSpendingPattern(user, currentMonth, currentYear);
    }

    private void detectSpendingPattern(User user, int month, int year) {
        // Check each category for 3 consecutive weeks of above-average spending
        for (CategoryType category : CategoryType.values()) {
            List<BigDecimal> weeklyTotals = transactionRepository
                .getWeeklyTotals(user, category, month, year);

            if (weeklyTotals.size() >= 3) {
                boolean risingPattern = IntStream.range(1, weeklyTotals.size())
                    .allMatch(i -> weeklyTotals.get(i).compareTo(weeklyTotals.get(i - 1)) > 0);

                if (risingPattern) {
                    fireAlert(user, "PATTERN_DETECTED", category.name(),
                        String.format("Your %s spending has increased every week this month — consider reviewing", category),
                        weeklyTotals.get(weeklyTotals.size() - 1));
                }
            }
        }
    }

    private void fireAlert(User user, String type, String category, String message, BigDecimal amount) {
        // Avoid duplicate alerts for same user+type+category today
        boolean exists = alertLogRepository.existsByUserIdAndAlertTypeAndCategoryAndCreatedAtAfter(
            user.getId(), type, category, LocalDateTime.now().toLocalDate().atStartOfDay());

        if (!exists) {
            alertLogRepository.save(AlertLog.builder()
                .userId(user.getId())
                .alertType(type)
                .category(category)
                .message(message)
                .triggerAmount(amount)
                .read(false)
                .createdAt(LocalDateTime.now())
                .build());
            log.info("Alert fired for user {}: {} - {}", user.getEmail(), type, message);
        }
    }
}
```

### 3.6 Scheduler

**`scheduler/NightlyAnalysisJob.java`**
```java
@Component
@RequiredArgsConstructor
@Slf4j
public class NightlyAnalysisJob {

    private final UserRepository userRepository;
    private final AlertEngine alertEngine;
    private final SpendingInsightService insightService;

    @Scheduled(cron = "0 0 2 * * *") // Runs at 2:00 AM every night
    public void runNightlyAnalysis() {
        log.info("Starting nightly analysis job...");
        List<User> users = userRepository.findAll();

        users.forEach(user -> {
            try {
                alertEngine.runForUser(user);
                insightService.generateSnapshot(user);
            } catch (Exception e) {
                log.error("Nightly job failed for user {}: {}", user.getEmail(), e.getMessage());
            }
        });

        log.info("Nightly analysis complete for {} users", users.size());
    }
}
```

Add `@EnableScheduling` to your main application class.

### 3.7 Security Config

**`config/SecurityConfig.java`**
```java
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final UserDetailsService userDetailsService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()
                .anyRequest().authenticated()
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
            .build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:4200"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        CorsRegistration source = new UrlBasedCorsConfigurationSource();
        ((UrlBasedCorsConfigurationSource) source).registerCorsConfiguration("/**", config);
        return (CorsConfigurationSource) source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

---

## 🧪 PHASE 4 — Backend Testing

### 4.1 JUnit + Mockito — Transaction Service Test
Create `test/java/com/wealthwise/transaction/TransactionServiceTest.java`:
```java
@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock private TransactionRepository transactionRepository;
    @Mock private UserRepository userRepository;
    @InjectMocks private TransactionService transactionService;

    @Test
    void createTransaction_validRequest_returnsDTO() {
        // Arrange
        User user = User.builder().id(1L).email("test@test.com").build();
        CreateTransactionRequest request = new CreateTransactionRequest(
            new BigDecimal("50.00"), CategoryType.DINING, "Lunch", LocalDate.now());

        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
        when(transactionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // Act
        TransactionDTO result = transactionService.create("test@test.com", request);

        // Assert
        assertNotNull(result);
        assertEquals(new BigDecimal("50.00"), result.getAmount());
        assertEquals(CategoryType.DINING, result.getCategory());
        verify(transactionRepository, times(1)).save(any());
    }

    @Test
    void createTransaction_userNotFound_throwsException() {
        when(userRepository.findByEmail(any())).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class,
            () -> transactionService.create("unknown@test.com", any()));
    }
}
```

### 4.2 Alert Engine Test
Create `test/java/com/wealthwise/alert/AlertEngineTest.java`:
```java
@ExtendWith(MockitoExtension.class)
class AlertEngineTest {

    @Mock private TransactionRepository transactionRepository;
    @Mock private BudgetRepository budgetRepository;
    @Mock private AlertLogRepository alertLogRepository;
    @InjectMocks private AlertEngine alertEngine;

    @Test
    void runForUser_budgetExceeded_firesAlert() {
        User user = User.builder().id(1L).build();
        Budget budget = Budget.builder()
            .category(CategoryType.DINING)
            .monthlyLimit(new BigDecimal("200.00"))
            .build();

        when(budgetRepository.findByUserAndMonthAndYear(any(), anyInt(), anyInt()))
            .thenReturn(List.of(budget));
        when(transactionRepository.sumByUserAndCategoryAndMonthAndYear(any(), any(), anyInt(), anyInt()))
            .thenReturn(new BigDecimal("250.00"));
        when(alertLogRepository.existsByUserIdAndAlertTypeAndCategoryAndCreatedAtAfter(any(), any(), any(), any()))
            .thenReturn(false);

        alertEngine.runForUser(user);

        verify(alertLogRepository, times(1)).save(argThat(alert ->
            alert.getAlertType().equals("BUDGET_EXCEEDED")));
    }
}
```

---

## 🖥️ PHASE 5 — Frontend Implementation

### 5.1 Core Module Setup

**`core/services/auth.service.ts`**
```typescript
@Injectable({ providedIn: 'root' })
export class AuthService {
  private apiUrl = 'http://localhost:8080/api/auth';
  private tokenKey = 'wealthwise_token';

  constructor(private http: HttpClient, private router: Router) {}

  login(email: string, password: string): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/login`, { email, password })
      .pipe(tap(res => localStorage.setItem(this.tokenKey, res.token)));
  }

  register(data: RegisterRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/register`, data)
      .pipe(tap(res => localStorage.setItem(this.tokenKey, res.token)));
  }

  logout(): void {
    localStorage.removeItem(this.tokenKey);
    this.router.navigate(['/auth/login']);
  }

  getToken(): string | null {
    return localStorage.getItem(this.tokenKey);
  }

  isLoggedIn(): boolean {
    return !!this.getToken();
  }
}
```

**`core/interceptors/jwt.interceptor.ts`**
```typescript
@Injectable()
export class JwtInterceptor implements HttpInterceptor {
  constructor(private authService: AuthService) {}

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    const token = this.authService.getToken();
    if (token) {
      req = req.clone({
        setHeaders: { Authorization: `Bearer ${token}` }
      });
    }
    return next.handle(req);
  }
}
```

**`core/guards/auth.guard.ts`**
```typescript
@Injectable({ providedIn: 'root' })
export class AuthGuard implements CanActivate {
  constructor(private authService: AuthService, private router: Router) {}

  canActivate(): boolean {
    if (this.authService.isLoggedIn()) return true;
    this.router.navigate(['/auth/login']);
    return false;
  }
}
```

### 5.2 Dashboard Component

**`features/dashboard/dashboard.component.ts`**
```typescript
@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html'
})
export class DashboardComponent implements OnInit {
  spendingSummary: SpendingSummaryDTO | null = null;
  budgetStatuses: BudgetStatusDTO[] = [];
  recentTransactions: TransactionDTO[] = [];
  unreadAlerts: AlertLog[] = [];

  currentMonth = new Date().getMonth() + 1;
  currentYear = new Date().getFullYear();

  // Chart.js data
  spendingChartData: ChartData<'doughnut'> = { labels: [], datasets: [] };
  trendChartData: ChartData<'line'> = { labels: [], datasets: [] };

  constructor(
    private transactionService: TransactionService,
    private budgetService: BudgetService,
    private alertService: AlertService
  ) {}

  ngOnInit(): void {
    this.loadDashboard();
  }

  loadDashboard(): void {
    forkJoin({
      summary: this.transactionService.getSummary(this.currentMonth, this.currentYear),
      budgets: this.budgetService.getBudgetStatus(this.currentMonth, this.currentYear),
      transactions: this.transactionService.getAll({ limit: 5 }),
      alerts: this.alertService.getUnread()
    }).subscribe(({ summary, budgets, transactions, alerts }) => {
      this.spendingSummary = summary;
      this.budgetStatuses = budgets;
      this.recentTransactions = transactions;
      this.unreadAlerts = alerts;
      this.buildCharts(summary, budgets);
    });
  }

  private buildCharts(summary: SpendingSummaryDTO, budgets: BudgetStatusDTO[]): void {
    // Doughnut: spending by category
    this.spendingChartData = {
      labels: Object.keys(summary.byCategory),
      datasets: [{
        data: Object.values(summary.byCategory),
        backgroundColor: ['#6366f1','#f59e0b','#10b981','#ef4444','#3b82f6','#8b5cf6','#ec4899','#14b8a6']
      }]
    };
  }
}
```

**`features/dashboard/dashboard.component.html`** — Key sections to build:
```html
<div class="dashboard-container">
  <!-- Alert Banner -->
  <div *ngIf="unreadAlerts.length > 0" class="alert-banner">
    <div *ngFor="let alert of unreadAlerts" class="alert-item" [ngClass]="alert.alertType">
      <i class="bi bi-exclamation-triangle"></i>
      {{ alert.message }}
    </div>
  </div>

  <!-- Summary Cards Row -->
  <div class="row g-3 mb-4">
    <div class="col-md-3">
      <div class="stat-card">
        <span class="label">Total Spent</span>
        <span class="value">{{ spendingSummary?.totalSpent | currency }}</span>
      </div>
    </div>
    <!-- repeat for: Transactions, Budgets on Track, Active Alerts -->
  </div>

  <!-- Charts Row -->
  <div class="row g-3 mb-4">
    <div class="col-md-6">
      <canvas baseChart [data]="spendingChartData" type="doughnut"></canvas>
    </div>
    <div class="col-md-6">
      <canvas baseChart [data]="trendChartData" type="line"></canvas>
    </div>
  </div>

  <!-- Budget Status Cards -->
  <div class="row g-3 mb-4">
    <div *ngFor="let budget of budgetStatuses" class="col-md-4">
      <div class="budget-card" [ngClass]="budget.status.toLowerCase()">
        <span>{{ budget.category }}</span>
        <div class="progress">
          <div class="progress-bar" [style.width.%]="budget.percentageUsed"></div>
        </div>
        <span>{{ budget.spent | currency }} / {{ budget.limit | currency }}</span>
      </div>
    </div>
  </div>

  <!-- Recent Transactions Table -->
  <table class="table">
    <thead>
      <tr><th>Date</th><th>Description</th><th>Category</th><th>Amount</th></tr>
    </thead>
    <tbody>
      <tr *ngFor="let tx of recentTransactions">
        <td>{{ tx.transactionDate | date }}</td>
        <td>{{ tx.description }}</td>
        <td><span class="badge">{{ tx.category }}</span></td>
        <td>{{ tx.amount | currency }}</td>
      </tr>
    </tbody>
  </table>
</div>
```

### 5.3 Angular Routing

**`app-routing.module.ts`**
```typescript
const routes: Routes = [
  { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
  {
    path: 'auth',
    loadChildren: () => import('./features/auth/auth.module').then(m => m.AuthModule)
  },
  {
    path: 'dashboard',
    component: DashboardComponent,
    canActivate: [AuthGuard]
  },
  {
    path: 'transactions',
    loadChildren: () => import('./features/transactions/transactions.module').then(m => m.TransactionsModule),
    canActivate: [AuthGuard]
  },
  {
    path: 'budgets',
    loadChildren: () => import('./features/budgets/budgets.module').then(m => m.BudgetsModule),
    canActivate: [AuthGuard]
  },
  {
    path: 'alerts',
    loadChildren: () => import('./features/alerts/alerts.module').then(m => m.AlertsModule),
    canActivate: [AuthGuard]
  }
];
```

---

## 🧪 PHASE 6 — Frontend Testing

### 6.1 Jasmine + Karma — Dashboard Component Test
```typescript
describe('DashboardComponent', () => {
  let component: DashboardComponent;
  let fixture: ComponentFixture<DashboardComponent>;
  let mockTransactionService: jasmine.SpyObj<TransactionService>;
  let mockBudgetService: jasmine.SpyObj<BudgetService>;
  let mockAlertService: jasmine.SpyObj<AlertService>;

  beforeEach(async () => {
    mockTransactionService = jasmine.createSpyObj('TransactionService', ['getSummary', 'getAll']);
    mockBudgetService = jasmine.createSpyObj('BudgetService', ['getBudgetStatus']);
    mockAlertService = jasmine.createSpyObj('AlertService', ['getUnread']);

    mockTransactionService.getSummary.and.returnValue(of({ totalSpent: 500, byCategory: {} }));
    mockBudgetService.getBudgetStatus.and.returnValue(of([]));
    mockTransactionService.getAll.and.returnValue(of([]));
    mockAlertService.getUnread.and.returnValue(of([]));

    await TestBed.configureTestingModule({
      declarations: [DashboardComponent],
      providers: [
        { provide: TransactionService, useValue: mockTransactionService },
        { provide: BudgetService, useValue: mockBudgetService },
        { provide: AlertService, useValue: mockAlertService }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(DashboardComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => expect(component).toBeTruthy());

  it('should load dashboard data on init', () => {
    expect(mockTransactionService.getSummary).toHaveBeenCalled();
    expect(mockBudgetService.getBudgetStatus).toHaveBeenCalled();
    expect(component.spendingSummary?.totalSpent).toBe(500);
  });
});
```

---

## 📬 PHASE 7 — Postman Collection

Create `postman/WealthWise.postman_collection.json` with these requests:

```
Auth
  POST /api/auth/register
  POST /api/auth/login

Transactions
  GET  /api/transactions
  GET  /api/transactions?month=2025-01&category=DINING
  POST /api/transactions
  PUT  /api/transactions/:id
  DELETE /api/transactions/:id
  GET  /api/transactions/summary?month=1&year=2025

Budgets
  GET  /api/budgets?month=1&year=2025
  POST /api/budgets
  GET  /api/budgets/status?month=1&year=2025

Alerts
  GET  /api/alerts
  GET  /api/alerts/unread
  PUT  /api/alerts/:id/read
```

Set up a Postman **environment** with:
- `base_url`: `http://localhost:8080`
- `token`: (auto-filled by login test script)

Add this to the login request **Tests tab**:
```javascript
const res = pm.response.json();
pm.environment.set("token", res.token);
```

---

## 🐳 PHASE 8 — Docker & Final Setup

### 8.1 Backend Dockerfile
```dockerfile
FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN ./mvnw package -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### 8.2 Update docker-compose.yml to include backend + frontend
```yaml
  backend:
    build: ./backend
    ports:
      - "8080:8080"
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/wealthwise
      SPRING_DATA_MONGODB_URI: mongodb://mongodb:27017/wealthwise
    depends_on:
      - postgres
      - mongodb
```

---

## 📖 PHASE 9 — README.md

Create a `README.md` at the root with:
- Project description + screenshot placeholder
- Architecture diagram (ASCII or linked image)
- Prerequisites: Java 17, Node 18, Docker
- Setup: `docker-compose up -d` → `./mvnw spring-boot:run` → `ng serve`
- API docs link (Postman collection)
- Test commands: `./mvnw test` and `ng test`

---

## ✅ Build Order Checklist for Cursor

Follow this sequence exactly:

- [ ] Phase 1 — Init both projects
- [ ] Phase 2 — Spin up Docker DBs, run Flyway migrations
- [ ] Phase 3.1 — application.yml
- [ ] Phase 3.2 — JWT Auth (JwtUtil, Filter, Controller, Service)
- [ ] Phase 3.3 — Transaction module (Entity → Repo → Service → Controller)
- [ ] Phase 3.4 — Budget module (Entity → Repo → Service → Controller)
- [ ] Phase 3.5 — Alert Engine (MongoDB doc → Repo → Engine)
- [ ] Phase 3.6 — Scheduler
- [ ] Phase 3.7 — Security Config + CORS
- [ ] Phase 4 — Backend tests (TransactionServiceTest, AlertEngineTest)
- [ ] Phase 5.1 — Angular core (AuthService, JwtInterceptor, AuthGuard)
- [ ] Phase 5.2 — Dashboard component
- [ ] Phase 5.3 — Routing + lazy-loaded modules
- [ ] Phase 6 — Frontend tests
- [ ] Phase 7 — Postman collection
- [ ] Phase 8 — Docker full setup
- [ ] Phase 9 — README

---

*Generated for WealthWise — Full Stack FinTech Resume Project*