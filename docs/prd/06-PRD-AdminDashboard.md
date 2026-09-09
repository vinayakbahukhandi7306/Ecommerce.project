# 🖥️ PRD 06 — Admin Dashboard

> **Module:** `admin`  
> **Parent:** [01-PRD-Overview.md](./01-PRD-Overview.md)

---

## 1. Overview

The Admin Dashboard provides a comprehensive management interface for platform administrators. It covers product inventory management, order processing, user management, and business analytics — all through a dedicated admin panel with role-based access.

---

## 2. Functional Requirements

### 2.1 Dashboard Home (Analytics)

| ID | Requirement | Priority |
|----|------------|----------|
| ADMIN-01 | Key metrics cards: Total Revenue, Total Orders, Total Users, Total Products | P0 |
| ADMIN-02 | Revenue chart (line/bar) — daily, weekly, monthly granularity | P0 |
| ADMIN-03 | Orders chart — orders per day/week with status breakdown | P1 |
| ADMIN-04 | Recent orders table (last 10) with quick status update | P0 |
| ADMIN-05 | Top selling products (top 10, by quantity or revenue) | P1 |
| ADMIN-06 | Low stock alerts (products below threshold) | P0 |
| ADMIN-07 | New user registrations chart (daily/weekly) | P1 |
| ADMIN-08 | Revenue by category (pie/donut chart) | P1 |
| ADMIN-09 | Date range selector for all analytics | P0 |
| ADMIN-10 | Real-time dashboard refresh (polling every 30s or WebSocket Phase 2) | P2 |

#### Dashboard Layout

```
┌──────────────────────────────────────────────────────────────────┐
│  ADMIN SIDEBAR          │         DASHBOARD HOME                 │
│                         │                                        │
│  📊 Dashboard           │  ┌─────────┐ ┌─────────┐ ┌─────────┐  │
│  📦 Products            │  │ Revenue │ │ Orders  │ │ Users   │  │
│  📁 Categories          │  │ ₹12.5L  │ │  342    │ │  1,205  │  │
│  🛒 Orders              │  │ ↑ 12%   │ │ ↑ 8%   │ │ ↑ 15%  │  │
│  👥 Users               │  └─────────┘ └─────────┘ └─────────┘  │
│  ⭐ Reviews             │                                        │
│  📢 Notifications       │  ┌────────────────────────────────────┐│
│  ⚙️ Settings            │  │      Revenue Chart (30 days)      ││
│                         │  │  📈 ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ ││
│                         │  └────────────────────────────────────┘│
│                         │                                        │
│                         │  ┌──────────────┐ ┌──────────────────┐ │
│                         │  │ Recent Orders│ │ Low Stock Alerts │ │
│                         │  │ #SV-...  ₹.. │ │ iPhone — 3 left  │ │
│                         │  │ #SV-...  ₹.. │ │ AirPods — 5 left │ │
│                         │  └──────────────┘ └──────────────────┘ │
└──────────────────────────────────────────────────────────────────┘
```

### 2.2 Product Management

| ID | Requirement | Priority |
|----|------------|----------|
| ADMIN-11 | List all products (paginated, searchable, filterable) | P0 |
| ADMIN-12 | Create new product (form with all fields + image upload) | P0 |
| ADMIN-13 | Edit product (inline or full-page form) | P0 |
| ADMIN-14 | Delete product (soft delete with confirmation modal) | P0 |
| ADMIN-15 | Bulk actions: activate, deactivate, delete selected products | P1 |
| ADMIN-16 | Manage product images (upload, reorder, set primary, delete) | P0 |
| ADMIN-17 | Manage product variants (add, edit, delete) | P1 |
| ADMIN-18 | Stock management — update stock quantity individually or in bulk | P0 |
| ADMIN-19 | CSV/Excel export of product list | P2 |
| ADMIN-20 | CSV/Excel import for bulk product creation (Phase 2) | P2 |

### 2.3 Category Management

| ID | Requirement | Priority |
|----|------------|----------|
| ADMIN-21 | Tree view of category hierarchy | P0 |
| ADMIN-22 | Create/edit/delete categories | P0 |
| ADMIN-23 | Drag-and-drop reordering (Phase 2) | P2 |
| ADMIN-24 | Upload category image | P1 |
| ADMIN-25 | View product count per category | P1 |

### 2.4 Order Management

| ID | Requirement | Priority |
|----|------------|----------|
| ADMIN-26 | List all orders (paginated with status filter) | P0 |
| ADMIN-27 | View order detail (same as customer view + admin actions) | P0 |
| ADMIN-28 | Update order status (dropdown with valid transitions only) | P0 |
| ADMIN-29 | Add tracking information (courier + tracking number) | P0 |
| ADMIN-30 | Process returns (approve/reject with reason) | P1 |
| ADMIN-31 | Process refunds (full/partial) | P1 |
| ADMIN-32 | Add internal notes to orders (not visible to customer) | P1 |
| ADMIN-33 | Filter orders by: status, date range, customer, amount range | P0 |
| ADMIN-34 | Export orders to CSV/Excel | P2 |

### 2.5 User Management

| ID | Requirement | Priority |
|----|------------|----------|
| ADMIN-35 | List all users (paginated, searchable) | P0 |
| ADMIN-36 | View user profile & order history | P0 |
| ADMIN-37 | Change user role (CUSTOMER ↔ ADMIN) | P0 |
| ADMIN-38 | Disable/enable user account | P0 |
| ADMIN-39 | View user activity log (Phase 2) | P2 |
| ADMIN-40 | Filter by: role, status, registration date | P1 |

### 2.6 Review Moderation

| ID | Requirement | Priority |
|----|------------|----------|
| ADMIN-41 | List all reviews (newest first, filterable) | P1 |
| ADMIN-42 | Moderate reviews (approve, reject, flag) | P1 |
| ADMIN-43 | Reply to reviews (as "ShopVerse Team") | P2 |
| ADMIN-44 | View reported reviews | P2 |

---

## 3. API Endpoints (Admin-Specific)

### 3.1 Analytics APIs

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/admin/analytics/summary` | Key metrics (revenue, orders, users, products) |
| GET | `/api/v1/admin/analytics/revenue` | Revenue data with date range + granularity |
| GET | `/api/v1/admin/analytics/orders` | Order stats with status breakdown |
| GET | `/api/v1/admin/analytics/top-products` | Top selling products |
| GET | `/api/v1/admin/analytics/low-stock` | Products below stock threshold |
| GET | `/api/v1/admin/analytics/category-revenue` | Revenue breakdown by category |
| GET | `/api/v1/admin/analytics/user-growth` | User registration trends |

### 3.2 Query Parameters for Analytics

```
GET /api/v1/admin/analytics/revenue?
    fromDate=2026-07-01
    &toDate=2026-07-25
    &granularity=DAILY      // DAILY, WEEKLY, MONTHLY
```

### 3.3 Response Examples

#### Summary Metrics Response

```json
{
  "totalRevenue": 1250000.00,
  "revenueChange": 12.5,        // % change vs previous period
  "totalOrders": 342,
  "ordersChange": 8.2,
  "totalUsers": 1205,
  "usersChange": 15.0,
  "totalProducts": 89,
  "activeProducts": 76,
  "lowStockCount": 5,
  "pendingOrders": 12,
  "period": {
    "from": "2026-07-01",
    "to": "2026-07-25"
  }
}
```

#### Revenue Chart Response

```json
{
  "granularity": "DAILY",
  "data": [
    { "date": "2026-07-01", "revenue": 45000.00, "orders": 12 },
    { "date": "2026-07-02", "revenue": 52000.00, "orders": 15 },
    ...
  ],
  "total": 1250000.00
}
```

---

## 4. Admin Product APIs

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/admin/products` | List products (with drafts & deleted) |
| POST | `/api/v1/admin/products` | Create product |
| PUT | `/api/v1/admin/products/{id}` | Update product |
| DELETE | `/api/v1/admin/products/{id}` | Soft delete product |
| PUT | `/api/v1/admin/products/{id}/status` | Update product status |
| PUT | `/api/v1/admin/products/{id}/stock` | Update stock quantity |
| PUT | `/api/v1/admin/products/bulk-status` | Bulk update status |
| POST | `/api/v1/admin/products/{id}/images` | Upload images (multipart) |
| PUT | `/api/v1/admin/products/{id}/images/reorder` | Reorder images |
| DELETE | `/api/v1/admin/products/{id}/images/{imgId}` | Delete image |
| POST | `/api/v1/admin/products/{id}/variants` | Add variant |
| PUT | `/api/v1/admin/products/{id}/variants/{varId}` | Update variant |
| DELETE | `/api/v1/admin/products/{id}/variants/{varId}` | Delete variant |

---

## 5. Admin User APIs

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/admin/users` | List users (paginated) |
| GET | `/api/v1/admin/users/{id}` | Get user detail + order summary |
| PUT | `/api/v1/admin/users/{id}/role` | Change user role |
| PUT | `/api/v1/admin/users/{id}/status` | Enable/disable user |

---

## 6. Frontend Pages (Admin Panel)

### 6.1 Admin Layout
- **Collapsible Sidebar** — navigation menu with icons and labels
- **Top Bar** — admin name, avatar, notifications bell, logout
- **Breadcrumbs** — contextual navigation
- **Responsive** — sidebar collapses to icons on smaller screens

### 6.2 Dashboard Page (`/admin/dashboard`)
- Metric cards with sparklines and % change indicators
- Charts: Revenue (line), Orders (bar), Category Revenue (donut)
- Recent orders table with status badges
- Low stock alerts list

### 6.3 Product Management Page (`/admin/products`)
- **Data Table** — name, image, price, stock, status, category, actions
- **Search Bar** — search by name/SKU
- **Filters** — status, category, stock level
- **Bulk Selection** — checkbox per row, bulk action bar
- **Create/Edit Form** — multi-section form with image upload dropzone

### 6.4 Order Management Page (`/admin/orders`)
- **Data Table** — order #, customer, date, total, status, actions
- **Status Filter Tabs** — All / Pending / Processing / Shipped / Delivered
- **Order Detail Drawer/Page** — timeline, items, actions

### 6.5 User Management Page (`/admin/users`)
- **Data Table** — name, email, role, status, joined date, orders count
- **Search** — by name or email
- **Role/Status Quick Actions** — dropdown on each row

---

## 7. Security Considerations

| Concern | Solution |
|---------|----------|
| Admin route protection | `@PreAuthorize("hasRole('ADMIN')")` on all admin endpoints |
| Frontend route guards | React route guard checking user role before rendering admin pages |
| Audit logging | Log all admin actions (who did what, when) |
| Rate limiting | Stricter rate limits on admin endpoints |
| Session monitoring | Track active admin sessions |

---

## 8. Analytics Query Optimization

```java
// Use native queries or JPQL projections for analytics
@Query("""
    SELECT new com.shopverse.admin.dto.DailyRevenueDTO(
        CAST(o.createdAt AS date),
        SUM(o.totalAmount),
        COUNT(o)
    )
    FROM Order o
    WHERE o.status NOT IN ('CANCELLED', 'PAYMENT_FAILED')
    AND o.createdAt BETWEEN :from AND :to
    GROUP BY CAST(o.createdAt AS date)
    ORDER BY CAST(o.createdAt AS date)
    """)
List<DailyRevenueDTO> getDailyRevenue(
    @Param("from") LocalDateTime from,
    @Param("to") LocalDateTime to
);
```

---

> **Next:** Read [07-PRD-Reviews-Wishlist-Notifications.md](./07-PRD-Reviews-Wishlist-Notifications.md) for the Reviews, Wishlist & Notifications specification.
