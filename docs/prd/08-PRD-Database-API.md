# 🗄️ PRD 08 — Database Schema & API Reference

> **Module:** All  
> **Parent:** [01-PRD-Overview.md](./01-PRD-Overview.md)

---

## 1. Overview

This document provides the consolidated database schema (Entity-Relationship diagram), table definitions, indexing strategy, and complete REST API reference across all modules.

---

## 2. Entity-Relationship Diagram

```
┌──────────────┐         ┌──────────────┐         ┌──────────────┐
│    users     │         │   addresses  │         │refresh_tokens│
│──────────────│         │──────────────│         │──────────────│
│ id (PK, UUID)│◄───┐    │ id (PK, UUID)│         │ id (PK, UUID)│
│ email        │    │    │ user_id (FK) │────────►│ user_id (FK) │
│ password     │    │    │ label        │         │ token        │
│ first_name   │    │    │ full_name    │         │ device_info  │
│ last_name    │    │    │ phone        │         │ expires_at   │
│ phone        │    │    │ address_line1│         │ revoked      │
│ avatar_url   │    │    │ city, state  │         │ created_at   │
│ role         │    │    │ postal_code  │         └──────────────┘
│ provider     │    │    │ country      │
│ email_verified│   │    │ is_default   │
│ created_at   │    │    │ created_at   │
│ updated_at   │    │    └──────────────┘
│ deleted      │    │
└──────────────┘    │
        │           │
        │           │    ┌──────────────┐        ┌──────────────────┐
        │           │    │  categories  │◄──┐    │ product_images   │
        │           │    │──────────────│   │    │──────────────────│
        │           │    │ id (PK, UUID)│   │    │ id (PK, UUID)    │
        │           │    │ name         │   │    │ product_id (FK)  │
        │           │    │ slug         │   │    │ image_url        │
        │           │    │ parent_id(FK)│───┘    │ alt_text         │
        │           │    │ image_url    │        │ is_primary       │
        │           │    │ active       │        │ display_order    │
        │           │    │ created_at   │        └──────────────────┘
        │           │    └──────┬───────┘               ▲
        │           │           │                       │
        │           │    ┌──────▼───────┐        ┌──────┴───────────┐
        │           │    │   products   │───────►│ product_variants │
        │           │    │──────────────│        │──────────────────│
        │           │    │ id (PK, UUID)│        │ id (PK, UUID)    │
        │           │    │ name, slug   │        │ product_id (FK)  │
        │           │    │ description  │        │ variant_name     │
        │           │    │ sku          │        │ variant_value    │
        │           │    │ price        │        │ sku              │
        │           │    │ original_price│       │ price_adjustment │
        │           │    │ stock_quantity│       │ stock_quantity   │
        │           │    │ brand        │        │ image_url        │
        │           │    │ category_id  │        └──────────────────┘
        │           │    │ status       │
        │           │    │ featured     │
        │           │    │ avg_rating   │
        │           │    │ review_count │
        │           │    │ version      │  ← Optimistic locking
        │           │    │ created_at   │
        │           │    │ deleted      │
        │           │    └──────────────┘
        │           │           │
        │           │           │ (many-to-many via cart_items, order_items, etc.)
        │           │           │
        │    ┌──────▼───────┐   │     ┌──────────────┐
        │    │    carts     │   │     │  cart_items   │
        │    │──────────────│   │     │──────────────│
        │    │ id (PK, UUID)│   │     │ id (PK, UUID)│
        │    │ user_id (FK) │   │     │ cart_id (FK) │
        │    │ created_at   │   │     │ product_id   │
        │    │ updated_at   │   │     │ variant_id   │
        │    └──────────────┘   │     │ quantity     │
        │                       │     │ unit_price   │
        │                       │     │ added_at     │
        │                       │     └──────────────┘
        │
        │    ┌──────────────┐         ┌──────────────┐
        ├───►│    orders    │────────►│  order_items  │
        │    │──────────────│         │──────────────│
        │    │ id (PK, UUID)│         │ id (PK, UUID)│
        │    │ order_number │         │ order_id (FK)│
        │    │ user_id (FK) │         │ product_id   │
        │    │ status       │         │ variant_id   │
        │    │ subtotal     │         │ product_name │ ← snapshot
        │    │ tax_amount   │         │ unit_price   │ ← snapshot
        │    │ shipping_cost│         │ quantity     │
        │    │ total_amount │         │ line_total   │
        │    │ tracking_no  │         │ return_status│
        │    │ created_at   │         └──────────────┘
        │    └──────┬───────┘
        │           │
        │    ┌──────▼───────────────┐  ┌──────────────┐
        │    │order_status_history │  │   payments   │
        │    │─────────────────────│  │──────────────│
        │    │ id (PK, UUID)       │  │ id (PK, UUID)│
        │    │ order_id (FK)       │  │ order_id (FK)│
        │    │ from_status         │  │ method       │
        │    │ to_status           │  │ status       │
        │    │ note                │  │ amount       │
        │    │ changed_by          │  │ razorpay_*   │
        │    │ created_at          │  │ created_at   │
        │    └─────────────────────┘  │ paid_at      │
        │                              └──────────────┘
        │
        │    ┌──────────────┐         ┌──────────────┐
        ├───►│   reviews    │         │wishlist_items│
        │    │──────────────│         │──────────────│
        │    │ id (PK, UUID)│         │ id (PK, UUID)│
        │    │ user_id (FK) │         │ user_id (FK) │◄──── (user)
        │    │ product_id   │         │ product_id   │
        │    │ rating       │         │ price_at_add │
        │    │ title, body  │         │ added_at     │
        │    │ verified     │         └──────────────┘
        │    │ helpful_count│
        │    │ status       │
        │    │ created_at   │
        │    └──────────────┘
        │
        │    ┌──────────────┐
        └───►│notifications │
             │──────────────│
             │ id (PK, UUID)│
             │ user_id (FK) │
             │ type         │
             │ title        │
             │ message      │
             │ action_url   │
             │ is_read      │
             │ created_at   │
             └──────────────┘
```

---

## 3. Database Indexing Strategy

### 3.1 Primary Indexes (Auto-created)

All `id` (PK) columns have auto-created unique indexes.

### 3.2 Unique Indexes

| Table | Columns | Purpose |
|-------|---------|---------|
| `users` | `email` | Unique email constraint |
| `products` | `slug` | SEO-friendly URL lookup |
| `products` | `sku` | Inventory tracking |
| `categories` | `slug` | URL lookup |
| `orders` | `order_number` | Human-readable lookup |
| `refresh_tokens` | `token` | Token lookup |
| `reviews` | `(user_id, product_id)` | One review per user per product |
| `wishlist_items` | `(user_id, product_id)` | Prevent duplicates |
| `cart_items` | `(cart_id, product_id, variant_id)` | Prevent duplicates |

### 3.3 Performance Indexes

| Table | Columns | Purpose |
|-------|---------|---------|
| `products` | `category_id` | Filter by category |
| `products` | `brand` | Filter by brand |
| `products` | `price` | Sort/filter by price |
| `products` | `status` | Filter active products |
| `products` | `(status, category_id, price)` | Composite for listing queries |
| `orders` | `user_id` | User's order history |
| `orders` | `status` | Admin order filtering |
| `orders` | `created_at` | Sort by date |
| `order_items` | `order_id` | Order detail lookup |
| `reviews` | `product_id` | Product reviews listing |
| `notifications` | `(user_id, is_read)` | Unread notifications |
| `cart_items` | `cart_id` | Cart item lookup |

### 3.4 Full-Text Search Index (PostgreSQL)

```sql
-- Product search using PostgreSQL full-text search (Phase 1)
ALTER TABLE products ADD COLUMN search_vector tsvector;

CREATE INDEX idx_product_search ON products USING gin(search_vector);

-- Trigger to auto-update search vector
CREATE TRIGGER trig_product_search_update
    BEFORE INSERT OR UPDATE ON products
    FOR EACH ROW EXECUTE FUNCTION
    tsvector_update_trigger(search_vector, 'pg_catalog.english', name, description, brand);
```

---

## 4. Complete REST API Reference

### 4.1 API Convention

```
Base URL:        /api/v1
Content-Type:    application/json
Auth Header:     Authorization: Bearer <access_token>
Pagination:      ?page=0&size=12&sort=createdAt,desc
Error Format:    { timestamp, status, error, message, path }
```

### 4.2 Standardized Response Envelope

```json
// Success (single resource)
{
  "success": true,
  "data": { ... },
  "timestamp": "2026-07-25T00:00:00Z"
}

// Success (paginated list)
{
  "success": true,
  "data": {
    "content": [ ... ],
    "page": 0,
    "size": 12,
    "totalElements": 156,
    "totalPages": 13,
    "first": true,
    "last": false
  },
  "timestamp": "2026-07-25T00:00:00Z"
}

// Error
{
  "success": false,
  "error": {
    "status": 400,
    "code": "VALIDATION_ERROR",
    "message": "Validation failed",
    "details": [
      { "field": "email", "message": "must be a valid email address" }
    ]
  },
  "timestamp": "2026-07-25T00:00:00Z"
}
```

### 4.3 Full Endpoint Catalog

#### Authentication (`/api/v1/auth`)

| # | Method | Endpoint | Description | Auth |
|---|--------|----------|-------------|------|
| 1 | POST | `/auth/register` | Register user | Public |
| 2 | POST | `/auth/login` | Login | Public |
| 3 | POST | `/auth/refresh` | Refresh token | Public |
| 4 | POST | `/auth/logout` | Logout | User |
| 5 | POST | `/auth/forgot-password` | Forgot password | Public |
| 6 | POST | `/auth/reset-password` | Reset password | Public |
| 7 | PUT | `/auth/change-password` | Change password | User |
| 8 | GET | `/auth/verify-email` | Verify email | Public |

#### Users (`/api/v1/users`)

| # | Method | Endpoint | Description | Auth |
|---|--------|----------|-------------|------|
| 9 | GET | `/users/me` | Get profile | User |
| 10 | PUT | `/users/me` | Update profile | User |
| 11 | POST | `/users/me/avatar` | Upload avatar | User |
| 12 | DELETE | `/users/me` | Delete account | User |
| 13 | GET | `/users/me/addresses` | List addresses | User |
| 14 | POST | `/users/me/addresses` | Add address | User |
| 15 | PUT | `/users/me/addresses/{id}` | Update address | User |
| 16 | DELETE | `/users/me/addresses/{id}` | Delete address | User |
| 17 | PUT | `/users/me/addresses/{id}/default` | Set default | User |

#### Categories (`/api/v1/categories`)

| # | Method | Endpoint | Description | Auth |
|---|--------|----------|-------------|------|
| 18 | GET | `/categories` | List root categories | Public |
| 19 | GET | `/categories/tree` | Full tree | Public |
| 20 | GET | `/categories/{slug}` | Get by slug | Public |
| 21 | GET | `/categories/{id}/products` | Products in category | Public |

#### Products (`/api/v1/products`)

| # | Method | Endpoint | Description | Auth |
|---|--------|----------|-------------|------|
| 22 | GET | `/products` | List (paginated + filter) | Public |
| 23 | GET | `/products/{slug}` | Detail by slug | Public |
| 24 | GET | `/products/featured` | Featured products | Public |
| 25 | GET | `/products/new-arrivals` | New arrivals | Public |
| 26 | GET | `/products/best-sellers` | Best sellers | Public |
| 27 | GET | `/products/search` | Search products | Public |
| 28 | GET | `/products/search/suggest` | Autocomplete | Public |

#### Cart (`/api/v1/cart`)

| # | Method | Endpoint | Description | Auth |
|---|--------|----------|-------------|------|
| 29 | GET | `/cart` | Get cart | User |
| 30 | POST | `/cart/items` | Add item | User |
| 31 | PUT | `/cart/items/{itemId}` | Update quantity | User |
| 32 | DELETE | `/cart/items/{itemId}` | Remove item | User |
| 33 | DELETE | `/cart` | Clear cart | User |
| 34 | POST | `/cart/merge` | Merge guest cart | User |

#### Checkout (`/api/v1/checkout`)

| # | Method | Endpoint | Description | Auth |
|---|--------|----------|-------------|------|
| 35 | POST | `/checkout/initiate` | Start checkout | User |
| 36 | POST | `/checkout/verify` | Verify payment | User |
| 37 | POST | `/checkout/cod` | Place COD order | User |

#### Orders (`/api/v1/orders`)

| # | Method | Endpoint | Description | Auth |
|---|--------|----------|-------------|------|
| 38 | GET | `/orders` | My orders | User |
| 39 | GET | `/orders/{orderNumber}` | Order detail | User |
| 40 | POST | `/orders/{orderNumber}/cancel` | Cancel order | User |
| 41 | POST | `/orders/{orderNumber}/return` | Request return | User |
| 42 | GET | `/orders/{orderNumber}/invoice` | Download invoice | User |
| 43 | GET | `/orders/{orderNumber}/track` | Track order | User |

#### Reviews (`/api/v1/reviews`)

| # | Method | Endpoint | Description | Auth |
|---|--------|----------|-------------|------|
| 44 | GET | `/products/{id}/reviews` | List reviews | Public |
| 45 | GET | `/products/{id}/reviews/summary` | Rating summary | Public |
| 46 | POST | `/products/{id}/reviews` | Write review | User |
| 47 | PUT | `/reviews/{id}` | Edit review | User |
| 48 | DELETE | `/reviews/{id}` | Delete review | User |
| 49 | POST | `/reviews/{id}/helpful` | Vote helpful | User |
| 50 | POST | `/reviews/{id}/report` | Report review | User |

#### Wishlist (`/api/v1/wishlist`)

| # | Method | Endpoint | Description | Auth |
|---|--------|----------|-------------|------|
| 51 | GET | `/wishlist` | List wishlist | User |
| 52 | POST | `/wishlist/{productId}` | Add to wishlist | User |
| 53 | DELETE | `/wishlist/{productId}` | Remove from wishlist | User |
| 54 | GET | `/wishlist/check/{productId}` | Check wishlisted | User |
| 55 | POST | `/wishlist/{productId}/move-to-cart` | Move to cart | User |

#### Notifications (`/api/v1/notifications`)

| # | Method | Endpoint | Description | Auth |
|---|--------|----------|-------------|------|
| 56 | GET | `/notifications` | List notifications | User |
| 57 | GET | `/notifications/unread-count` | Unread count | User |
| 58 | PUT | `/notifications/{id}/read` | Mark read | User |
| 59 | PUT | `/notifications/read-all` | Mark all read | User |
| 60 | DELETE | `/notifications/{id}` | Delete | User |

#### Admin (`/api/v1/admin`)

| # | Method | Endpoint | Description | Auth |
|---|--------|----------|-------------|------|
| 61-67 | GET | `/admin/analytics/*` | Analytics endpoints | Admin |
| 68-80 | * | `/admin/products/*` | Product management | Admin |
| 81-84 | * | `/admin/categories/*` | Category management | Admin |
| 85-93 | * | `/admin/orders/*` | Order management | Admin |
| 94-97 | * | `/admin/users/*` | User management | Admin |
| 98-100 | * | `/admin/reviews/*` | Review moderation | Admin |

**Total: ~100 endpoints across all modules.**

---

## 5. HTTP Status Code Usage

| Code | Usage |
|------|-------|
| `200 OK` | Successful GET, PUT |
| `201 Created` | Successful POST (resource created) |
| `204 No Content` | Successful DELETE |
| `400 Bad Request` | Validation error, malformed request |
| `401 Unauthorized` | Missing or invalid token |
| `403 Forbidden` | Insufficient role/permissions |
| `404 Not Found` | Resource not found |
| `409 Conflict` | Duplicate resource (e.g., email already exists) |
| `422 Unprocessable Entity` | Business logic error |
| `423 Locked` | Account locked |
| `429 Too Many Requests` | Rate limit exceeded |
| `500 Internal Server Error` | Unexpected server error |

---

> **Next:** Read [09-PRD-NFR-DevOps.md](./09-PRD-NFR-DevOps.md) for Non-Functional Requirements & DevOps specification.
