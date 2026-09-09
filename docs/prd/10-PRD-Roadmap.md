# 🗺️ PRD 10 — Roadmap & Future Enhancements

> **Module:** All  
> **Parent:** [01-PRD-Overview.md](./01-PRD-Overview.md)

---

## 1. Overview

This document outlines the phased delivery roadmap, future enhancement plans, and scalability strategies. The project is designed with extensibility as a first-class principle — every module uses interfaces, DTOs, and versioned APIs to allow independent evolution.

---

## 2. Phased Delivery Roadmap

### Phase 1 — Core MVP (Weeks 1–6)

> **Goal:** End-to-end shopping flow working with core features.

| Week | Focus | Deliverables |
|------|-------|-------------|
| **Week 1** | Project Setup + Auth | Spring Boot project scaffold, PostgreSQL + Redis Docker setup, User entity, JWT auth (register, login, refresh, logout), Spring Security config |
| **Week 2** | Product Catalog | Category CRUD (admin), Product CRUD (admin), Product listing API (pagination, filtering, sorting), Product detail API, Redis caching for products |
| **Week 3** | Frontend Foundation | React + Vite setup, Redux Toolkit + RTK Query, Layout (Header, Footer, Sidebar), Home page (hero, featured, categories), Product listing page with filters, Product detail page |
| **Week 4** | Cart + Checkout | Cart API (add, update, remove, clear), Cart page (frontend), Checkout flow (address → payment → confirm), Razorpay integration (test mode), Order creation on payment success |
| **Week 5** | Orders + Reviews | Order history API + page, Order detail with status timeline, Order cancellation, Review CRUD API, Review section on product detail page, Wishlist CRUD |
| **Week 6** | Admin Dashboard + Polish | Admin layout + sidebar, Analytics dashboard (revenue, orders, users charts), Product management (CRUD with images), Order management (status update, tracking), User management, Auth pages (login, register, forgot password) |

### Phase 2 — Enhanced Features (Weeks 7–10)

> **Goal:** Production hardening, advanced features, and resume polish.

| Week | Focus | Deliverables |
|------|-------|-------------|
| **Week 7** | OAuth2 + Notifications | Google/GitHub OAuth2 login, Email templates (Thymeleaf), Transactional emails (order lifecycle), In-app notification system |
| **Week 8** | Advanced Search + Performance | PostgreSQL full-text search, Search autocomplete, Image optimization (WebP, lazy loading), Performance tuning (caching, query optimization) |
| **Week 9** | Testing + CI/CD | Unit tests (JUnit 5 + Mockito, ≥ 80% coverage), Integration tests (Testcontainers), Frontend tests (Vitest), GitHub Actions CI pipeline, Docker Compose refinement |
| **Week 10** | Documentation + Deploy | Swagger/OpenAPI documentation, README with setup instructions, Architecture diagrams, Docker production build, Deploy to cloud (Railway / Render / AWS) |

### Phase 3 — Future Enhancements (Post-Submission)

> **Goal:** Continue building to show ongoing growth and learning.

| Feature | Description | Technologies |
|---------|-------------|-------------|
| Elasticsearch Integration | Replace PG full-text search with Elasticsearch | Elasticsearch, Spring Data Elasticsearch |
| Event-Driven Architecture | Async order processing, notification queue | RabbitMQ / Apache Kafka, Spring AMQP |
| Microservices Migration | Split monolith into services | Spring Cloud, API Gateway, Service Discovery |
| Real-time Features | Live order tracking, admin dashboard | WebSocket, STOMP, SockJS |
| Monitoring Stack | Metrics, alerting, log aggregation | Prometheus, Grafana, ELK Stack |
| Kubernetes Deployment | Container orchestration | K8s, Helm Charts |
| Mobile App | React Native companion app | React Native, shared API |

---

## 3. Feature Backlog (Prioritized)

### 3.1 High Value — Resume Impact

| Feature | Complexity | Impact | Technologies Showcased |
|---------|-----------|--------|----------------------|
| Full-text search (Elasticsearch) | Medium | High | Elasticsearch, distributed search |
| RabbitMQ event processing | Medium | High | Message queues, async architecture |
| WebSocket real-time updates | Medium | High | WebSocket, STOMP protocol |
| Kubernetes deployment | Medium | High | Container orchestration, DevOps |
| Comprehensive test suite (≥ 85%) | Medium | High | Testing maturity, TDD |
| API documentation (Swagger) | Low | High | Professional API design |

### 3.2 User Experience Enhancements

| Feature | Description |
|---------|-------------|
| **Coupon / Discount System** | Coupon codes, percentage/flat discounts, min order value, expiry dates |
| **Product Comparison** | Compare up to 4 products side-by-side |
| **Recently Viewed Products** | Track and display browsing history |
| **Product Recommendations** | "Customers also bought" (collaborative filtering — Phase 3) |
| **Multi-currency Support** | Display prices in user's local currency |
| **Multi-language (i18n)** | Internationalization support (React i18next) |
| **Dark Mode** | Theme toggle (light/dark) |
| **Progressive Web App (PWA)** | Offline support, installable app |
| **Accessibility (a11y)** | WCAG 2.1 AA compliance |

### 3.3 Business Features

| Feature | Description |
|---------|-------------|
| **Seller/Vendor Module** | Multi-vendor marketplace (seller registration, product management, commission) |
| **Inventory Alerts** | Auto-reorder notifications when stock hits threshold |
| **Analytics Deep Dive** | Customer segments, cohort analysis, conversion funnel |
| **A/B Testing Framework** | Test UI variations, pricing strategies |
| **Referral System** | Invite friends, earn credits |
| **Loyalty Points** | Points on purchases, redeemable for discounts |
| **Gift Cards** | Purchase and redeem gift cards |
| **Subscription Box** | Recurring product subscriptions |

### 3.4 Technical Debt & Infrastructure

| Item | Description |
|------|-------------|
| **Database Migrations** | Flyway/Liquibase for version-controlled schema changes |
| **API Rate Limiting** | Bucket4j + Redis distributed rate limiting |
| **Distributed Tracing** | OpenTelemetry + Jaeger for request tracing |
| **Feature Flags** | LaunchDarkly / Unleash for gradual rollouts |
| **Load Testing** | Gatling / k6 for performance benchmarking |
| **Backup Strategy** | Automated PostgreSQL backups, point-in-time recovery |
| **Blue-Green Deployment** | Zero-downtime deployments |

---

## 4. Scalability Evolution Path

### Stage 1: Monolith (Current)
```
┌─────────────────────────────────────┐
│         Spring Boot Monolith        │
│  (All modules in one application)   │
│                                     │
│  Auth │ Product │ Cart │ Order      │
│  Review │ Wishlist │ Notification   │
└────────────────┬────────────────────┘
                 │
    ┌────────────┼────────────┐
    ▼            ▼            ▼
PostgreSQL     Redis        MinIO
```

### Stage 2: Modular Monolith (Phase 2)
```
┌─────────────────────────────────────┐
│     Modular Spring Boot App         │
│                                     │
│  ┌──────┐ ┌──────┐ ┌──────┐        │
│  │ Auth │ │Catalog│ │Order │  ...   │
│  │Module│ │Module │ │Module│        │
│  └──┬───┘ └──┬───┘ └──┬───┘        │
│     │        │        │             │
│     └────────┼────────┘             │
│          Internal APIs              │
│         (Java interfaces)           │
└────────────────┬────────────────────┘
                 │
    ┌────────────┼────────────┬────────┐
    ▼            ▼            ▼        ▼
PostgreSQL     Redis        MinIO   RabbitMQ
```

### Stage 3: Microservices (Phase 3+)
```
┌──────────┐  ┌──────────┐  ┌──────────┐
│ Auth     │  │ Catalog  │  │  Order   │
│ Service  │  │ Service  │  │ Service  │
└────┬─────┘  └────┬─────┘  └────┬─────┘
     │             │             │
     ▼             ▼             ▼
  Auth DB      Catalog DB    Order DB
                                        
     └─────────────┼─────────────┘
                   │
            ┌──────▼──────┐
            │ API Gateway │  (Spring Cloud Gateway)
            │ + Service   │  (Eureka / Consul)
            │   Discovery │
            └─────────────┘
                   │
            ┌──────▼──────┐
            │  Message    │  (Kafka / RabbitMQ)
            │   Broker    │
            └─────────────┘
```

---

## 5. Resume Talking Points

After completing this project, you can highlight these in your resume:

### Technical Skills Demonstrated

```
✅ Full-Stack Development (Spring Boot + React)
✅ RESTful API Design (100+ endpoints, versioned, documented)
✅ Authentication & Authorization (JWT, OAuth2, RBAC)
✅ Relational Database Design (PostgreSQL, 15+ tables, indexing)
✅ Caching Strategies (Redis, TTL-based invalidation)
✅ Payment Integration (Razorpay/Stripe)
✅ Asynchronous Processing (@Async, message queues)
✅ Containerization (Docker, Docker Compose)
✅ CI/CD Pipeline (GitHub Actions)
✅ Testing (JUnit 5, Mockito, Testcontainers, 80%+ coverage)
✅ State Management (Redux Toolkit, RTK Query)
✅ Responsive Design (Mobile-first, MUI/Ant Design)
✅ Security Best Practices (OWASP Top 10, rate limiting)
✅ API Documentation (Swagger/OpenAPI)
✅ Clean Architecture (Layered, modular, SOLID principles)
```

### Resume Bullet Points (Examples)

```
• Developed a full-stack e-commerce platform (ShopVerse) using Spring Boot 3,
  React 18, PostgreSQL, and Redis, handling 100+ REST API endpoints with JWT
  authentication, OAuth2 social login, and role-based access control.

• Implemented a complete order lifecycle with Razorpay payment integration,
  real-time stock management using optimistic locking, and async email
  notifications via Spring @Async with Thymeleaf templates.

• Designed a scalable architecture with Redis caching (30% latency reduction),
  HikariCP connection pooling, and Docker containerization. Achieved 80%+
  test coverage using JUnit 5, Mockito, and Testcontainers.

• Built an admin analytics dashboard with revenue charts, inventory alerts,
  order management, and user administration using React with Recharts and
  Redux Toolkit for state management.
```

---

## 6. Deployment Options

| Platform | Type | Cost | Recommendation |
|----------|------|------|----------------|
| **Railway** | PaaS | Free tier available | ⭐ Best for quick deploy + demo |
| **Render** | PaaS | Free tier available | Good alternative to Railway |
| **AWS (EC2 + RDS)** | IaaS | Free tier (12 months) | Best for resume (shows AWS knowledge) |
| **DigitalOcean** | IaaS | $6/mo droplet | Affordable, great docs |
| **Vercel** (frontend) + **Railway** (backend) | Split | Free tiers | Optimal cost |

### Recommended Stack for Demo

```
Frontend:  Vercel (free, auto-deploy from GitHub)
Backend:   Railway (free tier, Docker deploy)
Database:  Railway PostgreSQL (free tier)
Cache:     Railway Redis (free tier)
Storage:   Cloudinary (free tier for images)
```

---

## 7. Project Timeline Summary

```
┌────────────────────────────────────────────────────────────────────┐
│                        PROJECT TIMELINE                            │
├──────────┬─────────────────────────────────────────────────────────┤
│  Week 1  │ ██████  Project Setup + Authentication                 │
│  Week 2  │ ██████  Product Catalog + Categories                   │
│  Week 3  │ ██████  Frontend Foundation + Product Pages            │
│  Week 4  │ ██████  Shopping Cart + Checkout + Payment             │
│  Week 5  │ ██████  Orders + Reviews + Wishlist                    │
│  Week 6  │ ██████  Admin Dashboard + Polish                       │
│  ─ ─ ─ ─ ┼ ─ ─ ─ ─ ─ ─ MVP COMPLETE ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─│
│  Week 7  │ ████    OAuth2 + Notifications                         │
│  Week 8  │ ████    Search + Performance                           │
│  Week 9  │ ████    Testing + CI/CD                                │
│  Week 10 │ ████    Documentation + Deployment                     │
│  ─ ─ ─ ─ ┼ ─ ─ ─ ─ ─ ─ PRODUCTION READY ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─│
│  Week 11+│ ██      Future Enhancements (ongoing)                  │
└──────────┴─────────────────────────────────────────────────────────┘
```

---

## 8. Getting Started Checklist

- [ ] Create GitHub repository (`shopverse` or `ecommerce-platform`)
- [ ] Initialize Spring Boot project (start.spring.io)
- [ ] Initialize React project (Vite)
- [ ] Set up Docker Compose (PostgreSQL + Redis + MinIO)
- [ ] Configure Spring Security + JWT
- [ ] Create base entity classes (BaseEntity with audit fields)
- [ ] Set up global exception handling
- [ ] Configure Swagger/OpenAPI
- [ ] Set up GitHub Actions CI
- [ ] Write your first API: `POST /api/v1/auth/register`

---

> 🎯 **You now have a complete PRD suite. Start with Phase 1, Week 1. Build incrementally, test as you go, and commit often. Good luck, Vinayak!**
