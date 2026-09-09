# ⚙️ PRD 09 — Non-Functional Requirements & DevOps

> **Module:** Infrastructure, Cross-cutting  
> **Parent:** [01-PRD-Overview.md](./01-PRD-Overview.md)

---

## 1. Overview

This document specifies non-functional requirements (NFRs) covering performance, security, scalability, reliability, testing, CI/CD, Docker containerization, and monitoring. These are critical for demonstrating production-readiness on your resume.

---

## 2. Performance Requirements

| Requirement | Target | How |
|-------------|--------|-----|
| API response time (p95) | < 200ms | Caching, query optimization, connection pooling |
| API response time (p99) | < 500ms | Async processing for heavy operations |
| Page load time (FCP) | < 1.5s | Code splitting, lazy loading, CDN |
| Database query time | < 50ms | Proper indexing, avoid N+1, pagination |
| Concurrent users | 500+ | Stateless services, connection pooling |
| Image loading | < 2s | WebP, lazy loading, optimized sizes |
| Search autocomplete | < 100ms | Redis cache, debounced requests |

### Connection Pooling (HikariCP)

```yaml
# application.yml
spring:
  datasource:
    hikari:
      minimum-idle: 5
      maximum-pool-size: 20
      idle-timeout: 300000        # 5 min
      max-lifetime: 1800000       # 30 min
      connection-timeout: 20000   # 20 sec
      pool-name: ShopVersePool
```

### Async Configuration

```java
@Configuration
@EnableAsync
public class AsyncConfig {
    
    @Bean("emailTaskExecutor")
    public TaskExecutor emailTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("email-");
        executor.initialize();
        return executor;
    }
    
    @Bean("generalTaskExecutor")
    public TaskExecutor generalTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(3);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(200);
        executor.setThreadNamePrefix("async-");
        executor.initialize();
        return executor;
    }
}
```

---

## 3. Security Requirements

### 3.1 OWASP Top 10 Compliance

| Risk | Mitigation |
|------|-----------|
| **Injection** | Parameterized queries (JPA), input validation |
| **Broken Auth** | JWT with short TTL, refresh token rotation, account lockout |
| **Sensitive Data Exposure** | BCrypt passwords, HTTPS enforced, no secrets in logs |
| **XXE** | Jackson (JSON only, no XML parsing) |
| **Broken Access Control** | `@PreAuthorize`, method-level security, ownership checks |
| **Security Misconfiguration** | Spring Security defaults, CORS configured, CSRF disabled (JWT) |
| **XSS** | React (auto-escapes), Content-Security-Policy header |
| **Insecure Deserialization** | DTO validation, no raw deserialization |
| **Components with Vulnerabilities** | Dependabot, regular dependency updates |
| **Insufficient Logging** | Audit logs for auth events, admin actions |

### 3.2 Security Headers

```java
@Bean
public SecurityFilterChain filterChain(HttpSecurity http) {
    http.headers(headers -> headers
        .contentTypeOptions(Customizer.withDefaults())
        .frameOptions(frame -> frame.deny())
        .xssProtection(Customizer.withDefaults())
        .httpStrictTransportSecurity(hsts -> hsts
            .maxAgeInSeconds(31536000)
            .includeSubDomains(true))
        .contentSecurityPolicy(csp -> csp
            .policyDirectives("default-src 'self'; script-src 'self'"))
    );
}
```

### 3.3 Rate Limiting

```java
// Using Bucket4j + Redis for distributed rate limiting
@Component
public class RateLimitFilter extends OncePerRequestFilter {
    
    // Rate limits per endpoint group:
    // Public APIs:    100 requests/minute per IP
    // Auth APIs:      10 requests/minute per IP (login, register)
    // Authenticated:  200 requests/minute per user
    // Admin APIs:     300 requests/minute per user
}
```

### 3.4 Data Protection

| Data | Protection |
|------|-----------|
| Passwords | BCrypt (strength 12) |
| JWT Secret | Environment variable (never in code/config files) |
| API Keys | Environment variables + encrypted config |
| PII (email, phone, address) | Encrypted at rest (Jasypt), TLS in transit |
| Payment data | Never stored — handled by Razorpay |
| Uploaded files | Validated file type + size, virus scan (Phase 2) |

---

## 4. Scalability Design

### 4.1 Horizontal Scalability Principles

```
┌─────────────────────────────────────────────────────────────┐
│                    SCALABILITY PATTERNS                       │
│                                                               │
│  ✅ Stateless Services       — No server-side sessions        │
│  ✅ JWT Authentication       — No session store needed         │
│  ✅ Redis Caching            — Shared cache across instances   │
│  ✅ Connection Pooling       — HikariCP per instance           │
│  ✅ Async Processing         — @Async for emails, notifications│
│  ✅ API Versioning           — /api/v1/ for backward compat    │
│  ✅ DB Read Replicas (P2)    — Separate read/write datasources │
│  ✅ Event-driven (P2)        — RabbitMQ for decoupled services │
│  ✅ Container-ready          — Docker images, K8s ready        │
│  ✅ CDN for static assets    — Offload image/CSS/JS serving    │
└─────────────────────────────────────────────────────────────┘
```

### 4.2 Multi-Profile Configuration

```yaml
# application.yml (common)
spring:
  profiles:
    active: ${SPRING_PROFILE:dev}

---
# application-dev.yml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/shopverse_dev
  jpa:
    show-sql: true
    hibernate:
      ddl-auto: update

---
# application-prod.yml
spring:
  datasource:
    url: ${DATABASE_URL}
  jpa:
    show-sql: false
    hibernate:
      ddl-auto: validate
  cache:
    type: redis
  redis:
    host: ${REDIS_HOST}
    port: ${REDIS_PORT}
```

---

## 5. Testing Strategy

### 5.1 Test Pyramid

```
          ┌────────────────┐
          │    E2E Tests   │    10%  (Cypress / Playwright)
          │  (Critical     │
          │   user flows)  │
          ├────────────────┤
          │  Integration   │    30%  (Spring Boot Test + Testcontainers)
          │    Tests       │
          │ (API, DB, etc) │
          ├────────────────┤
          │   Unit Tests   │    60%  (JUnit 5 + Mockito)
          │  (Services,    │
          │   Utils)       │
          └────────────────┘
```

### 5.2 Backend Testing

| Layer | Tool | What to Test |
|-------|------|-------------|
| **Unit Tests** | JUnit 5 + Mockito | Service layer logic, validators, utilities |
| **Repository Tests** | `@DataJpaTest` | Custom queries, entity mappings |
| **Controller Tests** | `@WebMvcTest` + MockMvc | Request/response, validation, security |
| **Integration Tests** | `@SpringBootTest` + Testcontainers | Full flow (API → Service → DB) |
| **Security Tests** | Spring Security Test | Auth flows, role-based access |

#### Example Test Structure

```java
// Unit Test
@ExtendWith(MockitoExtension.class)
class ProductServiceTest {
    @Mock private ProductRepository productRepository;
    @Mock private CategoryRepository categoryRepository;
    @InjectMocks private ProductServiceImpl productService;

    @Test
    void getProductBySlug_WhenExists_ReturnsProduct() { ... }

    @Test
    void getProductBySlug_WhenNotFound_ThrowsException() { ... }

    @Test
    void createProduct_WithValidData_SavesAndReturns() { ... }
}

// Integration Test
@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
class ProductControllerIT {
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15");

    @Autowired private MockMvc mockMvc;

    @Test
    void listProducts_Returns200WithPaginatedResults() { ... }
}
```

### 5.3 Frontend Testing

| Layer | Tool | What to Test |
|-------|------|-------------|
| **Unit Tests** | Vitest + React Testing Library | Components, hooks, utils |
| **Component Tests** | React Testing Library | User interactions, rendering |
| **E2E Tests** | Cypress / Playwright | Critical user flows |

### 5.4 Coverage Targets

| Module | Target |
|--------|--------|
| Service layer | ≥ 85% |
| Controller layer | ≥ 80% |
| Repository layer | ≥ 70% |
| Frontend components | ≥ 75% |
| Overall backend | ≥ 80% |

---

## 6. CI/CD Pipeline

### 6.1 GitHub Actions Workflow

```yaml
# .github/workflows/ci.yml
name: ShopVerse CI/CD

on:
  push:
    branches: [main, develop]
  pull_request:
    branches: [main]

jobs:
  # ─── Backend ─────────────────────────────────
  backend-test:
    runs-on: ubuntu-latest
    services:
      postgres:
        image: postgres:15
        env:
          POSTGRES_DB: shopverse_test
          POSTGRES_USER: test
          POSTGRES_PASSWORD: test
        ports: ['5432:5432']
      redis:
        image: redis:7
        ports: ['6379:6379']
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'
      - name: Run tests
        run: ./mvnw verify -pl backend
      - name: Upload coverage
        uses: codecov/codecov-action@v3

  # ─── Frontend ────────────────────────────────
  frontend-test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-node@v4
        with:
          node-version: '20'
      - run: cd frontend && npm ci
      - run: cd frontend && npm run test
      - run: cd frontend && npm run build

  # ─── Docker Build ────────────────────────────
  docker-build:
    needs: [backend-test, frontend-test]
    runs-on: ubuntu-latest
    if: github.ref == 'refs/heads/main'
    steps:
      - uses: actions/checkout@v4
      - name: Build & Push Docker images
        run: |
          docker compose build
          # Push to Docker Hub / GHCR
```

### 6.2 Branch Strategy

```
main          ← Production-ready code
  ↑
develop       ← Integration branch
  ↑
feature/*     ← Feature branches (e.g., feature/cart-checkout)
  ↑
bugfix/*      ← Bug fix branches
```

---

## 7. Docker Configuration

### 7.1 Docker Compose (Development)

```yaml
# docker-compose.yml
version: '3.8'

services:
  # ─── Database ─────────────────
  postgres:
    image: postgres:15-alpine
    environment:
      POSTGRES_DB: shopverse
      POSTGRES_USER: shopverse
      POSTGRES_PASSWORD: shopverse_dev
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data
      - ./backend/src/main/resources/db/init.sql:/docker-entrypoint-initdb.d/init.sql

  # ─── Cache ────────────────────
  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"
    volumes:
      - redis_data:/data

  # ─── Object Storage ──────────
  minio:
    image: minio/minio:latest
    command: server /data --console-address ":9001"
    environment:
      MINIO_ROOT_USER: minioadmin
      MINIO_ROOT_PASSWORD: minioadmin
    ports:
      - "9000:9000"
      - "9001:9001"
    volumes:
      - minio_data:/data

  # ─── Backend ──────────────────
  backend:
    build:
      context: ./backend
      dockerfile: Dockerfile
    environment:
      SPRING_PROFILES_ACTIVE: docker
      DATABASE_URL: jdbc:postgresql://postgres:5432/shopverse
      REDIS_HOST: redis
      MINIO_ENDPOINT: http://minio:9000
    ports:
      - "8080:8080"
    depends_on:
      - postgres
      - redis
      - minio

  # ─── Frontend ─────────────────
  frontend:
    build:
      context: ./frontend
      dockerfile: Dockerfile
    ports:
      - "3000:3000"
    depends_on:
      - backend

volumes:
  postgres_data:
  redis_data:
  minio_data:
```

### 7.2 Backend Dockerfile

```dockerfile
# backend/Dockerfile
FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /app
COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn
RUN ./mvnw dependency:resolve
COPY src ./src
RUN ./mvnw package -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### 7.3 Frontend Dockerfile

```dockerfile
# frontend/Dockerfile
FROM node:20-alpine AS build
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
RUN npm run build

FROM nginx:alpine
COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 3000
CMD ["nginx", "-g", "daemon off;"]
```

---

## 8. Monitoring & Observability (Phase 2)

### 8.1 Spring Boot Actuator

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health, info, metrics, prometheus
  endpoint:
    health:
      show-details: when_authorized
  health:
    db:
      enabled: true
    redis:
      enabled: true
```

### 8.2 Monitoring Stack

```
Spring Actuator ──► Prometheus ──► Grafana (Dashboards)
     │
     └── /actuator/health       → Liveness / Readiness probes
         /actuator/metrics      → JVM, HTTP, DB metrics
         /actuator/prometheus   → Prometheus scrape endpoint
```

### 8.3 Logging Strategy

```yaml
logging:
  level:
    root: INFO
    com.shopverse: DEBUG
    org.springframework.security: WARN
    org.hibernate.SQL: DEBUG        # Only in dev
  pattern:
    console: "%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n"
  file:
    name: logs/shopverse.log
    max-size: 10MB
    max-history: 30
```

---

## 9. Environment Variables

| Variable | Description | Example |
|----------|-------------|---------|
| `SPRING_PROFILES_ACTIVE` | Active profile | `dev`, `prod`, `docker` |
| `DATABASE_URL` | PostgreSQL connection | `jdbc:postgresql://...` |
| `DATABASE_USERNAME` | DB username | `shopverse` |
| `DATABASE_PASSWORD` | DB password | `*****` |
| `REDIS_HOST` | Redis host | `localhost` |
| `REDIS_PORT` | Redis port | `6379` |
| `JWT_SECRET` | JWT signing key | (256-bit random) |
| `JWT_EXPIRATION_MS` | Access token TTL | `900000` (15 min) |
| `RAZORPAY_KEY_ID` | Razorpay API key | `rzp_test_...` |
| `RAZORPAY_KEY_SECRET` | Razorpay secret | `*****` |
| `MINIO_ENDPOINT` | MinIO/S3 endpoint | `http://localhost:9000` |
| `MINIO_ACCESS_KEY` | MinIO access key | `minioadmin` |
| `MINIO_SECRET_KEY` | MinIO secret key | `minioadmin` |
| `MAIL_HOST` | SMTP host | `smtp.gmail.com` |
| `MAIL_PORT` | SMTP port | `587` |
| `MAIL_USERNAME` | SMTP username | `noreply@shopverse.com` |
| `MAIL_PASSWORD` | SMTP password | `*****` |
| `FRONTEND_URL` | Frontend base URL | `http://localhost:3000` |

---

> **Next:** Read [10-PRD-Roadmap.md](./10-PRD-Roadmap.md) for the Roadmap & Future Enhancements specification.
