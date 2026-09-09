# 📦 ShopVerse — E-Commerce Platform PRD (Master Overview)

> **Project Name:** ShopVerse  
> **Version:** 1.0  
> **Author:** Vinayak  
> **Date:** July 2026  
> **Status:** Draft  
> **Stack:** Spring Boot · React · PostgreSQL · Redis · Docker

---

## 1. Executive Summary

ShopVerse is a **production-grade, full-stack e-commerce platform** built to demonstrate advanced software engineering skills for a B.Tech resume portfolio. The platform enables users to browse products, manage carts, place orders, process payments, and track deliveries — while administrators manage inventory, orders, and analytics through a dedicated dashboard.

The system is designed with **scalability, modularity, and extensibility** as first-class principles, using a layered architecture with clear separation of concerns.

---

## 2. Goals & Objectives

| # | Goal | Metric |
|---|------|--------|
| G1 | Build a resume-worthy full-stack project | Demonstrates 10+ technical competencies |
| G2 | Handle real-world e-commerce flows end-to-end | Covers auth → browse → cart → checkout → order → delivery |
| G3 | Design for horizontal scalability | Stateless services, cache layer, async processing |
| G4 | Follow industry best practices | Clean architecture, CI/CD, testing, documentation |
| G5 | Enable future extensibility | Plugin-style module boundaries, versioned APIs |

---

## 3. Technology Stack

### 3.1 Backend

| Layer | Technology | Purpose |
|-------|-----------|---------|
| **Framework** | Spring Boot 3.x | REST API, dependency injection, auto-configuration |
| **Security** | Spring Security + JWT + OAuth2 | Authentication, authorization, social login |
| **ORM** | Spring Data JPA + Hibernate | Database abstraction, entity mapping |
| **Validation** | Jakarta Bean Validation | Request DTO validation |
| **API Docs** | SpringDoc OpenAPI (Swagger) | Auto-generated API documentation |
| **Caching** | Spring Cache + Redis | Session caching, product catalog caching |
| **Messaging** | RabbitMQ / Apache Kafka (Phase 2) | Async order processing, notifications |
| **Email** | Spring Mail + Thymeleaf templates | Transactional emails |
| **File Storage** | AWS S3 / MinIO (local dev) | Product images, user avatars |
| **Build Tool** | Maven / Gradle | Dependency management, build lifecycle |

### 3.2 Frontend

| Layer | Technology | Purpose |
|-------|-----------|---------|
| **Framework** | React 18+ (Vite) | UI rendering, component architecture |
| **State Management** | Redux Toolkit + RTK Query | Global state, API caching, data fetching |
| **Routing** | React Router v6 | Client-side navigation |
| **UI Library** | Material UI (MUI) / Ant Design | Pre-built accessible components |
| **Forms** | React Hook Form + Zod | Form handling with schema validation |
| **HTTP Client** | Axios (via RTK Query) | API communication |
| **Charts** | Recharts / Chart.js | Admin analytics dashboards |
| **Notifications** | React Toastify | User feedback toasts |

### 3.3 Database & Infrastructure

| Layer | Technology | Purpose |
|-------|-----------|---------|
| **Primary DB** | PostgreSQL 15+ | Relational data storage |
| **Cache** | Redis 7+ | Caching, session store, rate limiting |
| **Search** | Elasticsearch (Phase 2) | Full-text product search |
| **Containerization** | Docker + Docker Compose | Local dev environment, deployment |
| **CI/CD** | GitHub Actions | Automated testing, build, deploy |
| **Monitoring** | Spring Actuator + Prometheus + Grafana (Phase 2) | Health checks, metrics |
| **Logging** | SLF4J + Logback + ELK Stack (Phase 2) | Centralized logging |

---

## 4. High-Level Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                        CLIENT TIER                          │
│                                                             │
│   React SPA (Vite)  ←→  Redux Toolkit  ←→  RTK Query       │
│                                                             │
└──────────────────────────┬──────────────────────────────────┘
                           │ HTTPS (REST API)
                           ▼
┌─────────────────────────────────────────────────────────────┐
│                      API GATEWAY TIER                       │
│                                                             │
│   Spring Boot App                                           │
│   ┌───────────┐  ┌──────────────┐  ┌───────────────────┐   │
│   │  Security  │  │  Rate Limiter │  │  CORS / Filters   │  │
│   │  (JWT)     │  │  (Redis)      │  │                   │  │
│   └───────────┘  └──────────────┘  └───────────────────┘   │
│                                                             │
└──────────────────────────┬──────────────────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────────────────────────┐
│                     SERVICE TIER                            │
│                                                             │
│   ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────────┐  │
│   │  Auth    │ │ Product  │ │  Order   │ │   Payment    │  │
│   │ Service  │ │ Service  │ │ Service  │ │   Service    │  │
│   └──────────┘ └──────────┘ └──────────┘ └──────────────┘  │
│   ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────────┐  │
│   │  Cart    │ │ Review   │ │Notificat.│ │   Admin      │  │
│   │ Service  │ │ Service  │ │ Service  │ │   Service    │  │
│   └──────────┘ └──────────┘ └──────────┘ └──────────────┘  │
│                                                             │
└──────────────────────────┬──────────────────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────────────────────────┐
│                    DATA / INFRA TIER                        │
│                                                             │
│   ┌────────────┐  ┌─────────┐  ┌─────────┐  ┌───────────┐  │
│   │ PostgreSQL │  │  Redis  │  │   S3 /  │  │ RabbitMQ  │  │
│   │            │  │         │  │  MinIO  │  │           │  │
│   └────────────┘  └─────────┘  └─────────┘  └───────────┘  │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

---

## 5. Backend Package Structure (Spring Boot)

```
com.shopverse
├── config/                  # Security, CORS, Redis, Swagger configs
├── exception/               # Global exception handler, custom exceptions
├── common/                  # Shared DTOs, enums, utils, constants
│
├── auth/                    # Authentication & Authorization module
│   ├── controller/
│   ├── service/
│   ├── repository/
│   ├── entity/
│   ├── dto/
│   └── security/            # JWT provider, filters, OAuth2 handlers
│
├── user/                    # User profile management
│   ├── controller/
│   ├── service/
│   ├── repository/
│   ├── entity/
│   └── dto/
│
├── product/                 # Product catalog module
│   ├── controller/
│   ├── service/
│   ├── repository/
│   ├── entity/
│   └── dto/
│
├── category/                # Category & subcategory management
│   ├── controller/
│   ├── service/
│   ├── repository/
│   ├── entity/
│   └── dto/
│
├── cart/                    # Shopping cart module
│   ├── controller/
│   ├── service/
│   ├── repository/
│   ├── entity/
│   └── dto/
│
├── order/                   # Order processing module
│   ├── controller/
│   ├── service/
│   ├── repository/
│   ├── entity/
│   └── dto/
│
├── payment/                 # Payment integration module
│   ├── controller/
│   ├── service/
│   └── dto/
│
├── review/                  # Product reviews & ratings
│   ├── controller/
│   ├── service/
│   ├── repository/
│   ├── entity/
│   └── dto/
│
├── wishlist/                # Wishlist module
│   ├── controller/
│   ├── service/
│   ├── repository/
│   ├── entity/
│   └── dto/
│
├── notification/            # Email & push notifications
│   ├── service/
│   ├── templates/
│   └── dto/
│
├── admin/                   # Admin dashboard APIs
│   ├── controller/
│   ├── service/
│   └── dto/
│
└── ShopVerseApplication.java
```

---

## 6. Frontend Directory Structure (React)

```
src/
├── assets/                  # Static images, fonts, icons
├── components/              # Reusable UI components
│   ├── common/              # Button, Input, Modal, Loader, etc.
│   ├── layout/              # Header, Footer, Sidebar, Navbar
│   ├── product/             # ProductCard, ProductGrid, ProductDetail
│   ├── cart/                # CartItem, CartSummary
│   ├── order/               # OrderCard, OrderTimeline
│   ├── review/              # ReviewCard, ReviewForm, StarRating
│   └── admin/               # DataTable, StatsCard, Charts
│
├── pages/                   # Page-level components (route targets)
│   ├── Home.jsx
│   ├── ProductListing.jsx
│   ├── ProductDetail.jsx
│   ├── Cart.jsx
│   ├── Checkout.jsx
│   ├── OrderHistory.jsx
│   ├── OrderDetail.jsx
│   ├── Wishlist.jsx
│   ├── Profile.jsx
│   ├── Login.jsx
│   ├── Register.jsx
│   └── admin/
│       ├── Dashboard.jsx
│       ├── ManageProducts.jsx
│       ├── ManageOrders.jsx
│       └── ManageUsers.jsx
│
├── features/                # Redux slices (RTK)
│   ├── auth/
│   ├── product/
│   ├── cart/
│   ├── order/
│   ├── review/
│   └── admin/
│
├── services/                # RTK Query API definitions
│   ├── authApi.js
│   ├── productApi.js
│   ├── cartApi.js
│   ├── orderApi.js
│   ├── reviewApi.js
│   └── adminApi.js
│
├── hooks/                   # Custom React hooks
├── utils/                   # Helper functions, formatters, validators
├── constants/               # App-wide constants, enums
├── routes/                  # Route definitions, guards
├── App.jsx
├── main.jsx
└── index.css
```

---

## 7. PRD Document Index

This PRD is split into the following focused documents:

| # | Document | Covers |
|---|----------|--------|
| 01 | **PRD Overview** (this file) | Vision, tech stack, architecture, project structure |
| 02 | [Auth & User Management](./02-PRD-Auth-UserManagement.md) | Registration, login, JWT, OAuth2, roles, profile |
| 03 | [Product Catalog & Search](./03-PRD-ProductCatalog-Search.md) | Products, categories, filtering, sorting, search |
| 04 | [Shopping Cart & Checkout](./04-PRD-Cart-Checkout.md) | Cart CRUD, checkout flow, address, payment |
| 05 | [Order Management & Tracking](./05-PRD-OrderManagement.md) | Order lifecycle, status tracking, returns, history |
| 06 | [Admin Dashboard](./06-PRD-AdminDashboard.md) | Inventory, user mgmt, analytics, order mgmt |
| 07 | [Reviews, Wishlist & Notifications](./07-PRD-Reviews-Wishlist-Notifications.md) | Ratings, reviews, wishlists, email/push |
| 08 | [Database Schema & API Reference](./08-PRD-Database-API.md) | ER diagram, table schemas, REST API endpoints |
| 09 | [Non-Functional Requirements & DevOps](./09-PRD-NFR-DevOps.md) | Performance, security, CI/CD, Docker, monitoring |
| 10 | [Roadmap & Future Enhancements](./10-PRD-Roadmap.md) | Phased delivery, future features, scalability plan |

---

## 8. User Roles

| Role | Description | Permissions |
|------|-------------|-------------|
| **GUEST** | Unauthenticated visitor | Browse products, view details, search |
| **CUSTOMER** | Registered & logged-in user | All GUEST + cart, orders, reviews, wishlist, profile |
| **ADMIN** | Platform administrator | All CUSTOMER + manage products, orders, users, analytics |
| **SUPER_ADMIN** | System owner (Phase 2) | All ADMIN + manage admins, system config |

---

## 9. Key Non-Functional Requirements (Summary)

| Requirement | Target |
|-------------|--------|
| **Response Time** | < 200ms for API calls (p95) |
| **Availability** | 99.9% uptime target |
| **Security** | OWASP Top 10 compliance, encrypted data at rest & in transit |
| **Scalability** | Stateless services, horizontal scaling via Docker |
| **Test Coverage** | ≥ 80% unit test coverage, integration tests for critical flows |
| **API Versioning** | URL-based versioning (`/api/v1/...`) |
| **Database** | Connection pooling (HikariCP), indexing strategy |
| **Caching** | Redis for hot data (products, sessions) — TTL-based invalidation |

---

> **Next:** Read [02-PRD-Auth-UserManagement.md](./02-PRD-Auth-UserManagement.md) for the Authentication & User Management specification.
