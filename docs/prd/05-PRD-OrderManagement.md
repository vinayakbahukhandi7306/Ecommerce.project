# 📦 PRD 05 — Order Management & Tracking

> **Module:** `order`  
> **Parent:** [01-PRD-Overview.md](./01-PRD-Overview.md)

---

## 1. Overview

This module handles the complete order lifecycle — from creation after successful payment, through processing, shipping, delivery, and potential returns/refunds. It provides both customer-facing order history and admin-facing order management.

---

## 2. Functional Requirements

### 2.1 Order Creation

| ID | Requirement | Priority |
|----|------------|----------|
| ORD-01 | Order created after successful payment verification | P0 |
| ORD-02 | Unique order number (human-readable, e.g., `SV-20260725-A1B2C3`) | P0 |
| ORD-03 | Snapshot product details at order time (name, price, image) | P0 |
| ORD-04 | Deduct stock on order creation (atomic operation) | P0 |
| ORD-05 | Clear user's cart after successful order | P0 |
| ORD-06 | Order confirmation email with invoice (async) | P1 |
| ORD-07 | Generate PDF invoice (downloadable) | P1 |

### 2.2 Order Status Lifecycle

```
                    ┌──────────────┐
                    │   PENDING    │  (Just placed, payment processing)
                    └──────┬───────┘
                           │
                    ┌──────▼───────┐
         ┌─────────│  CONFIRMED   │  (Payment verified)
         │         └──────┬───────┘
         │                │
         │         ┌──────▼───────┐
         │         │  PROCESSING  │  (Being packed)
         │         └──────┬───────┘
         │                │
         │         ┌──────▼───────┐
         │         │   SHIPPED    │  (Handed to courier)
         │         └──────┬───────┘
         │                │
         │         ┌──────▼───────┐
         │         │  DELIVERED   │  (Customer received)
         │         └──────┬───────┘
         │                │
         │         ┌──────▼───────┐
         │         │  COMPLETED   │  (Return window expired)
         │         └──────────────┘
         │
         │  (Failure paths)
         │
         ├── PAYMENT_FAILED  (Payment declined/expired)
         ├── CANCELLED       (User/admin cancelled before shipping)
         └── RETURNED        (Customer returned after delivery)
              └── REFUNDED   (Refund processed)
```

| ID | Requirement | Priority |
|----|------------|----------|
| ORD-08 | Order status transitions follow defined state machine | P0 |
| ORD-09 | Status change triggers notification (email + in-app) | P1 |
| ORD-10 | Status change audit log (who, when, old → new status) | P0 |
| ORD-11 | Only valid transitions allowed (e.g., can't go from DELIVERED to PROCESSING) | P0 |

### 2.3 Order History (Customer)

| ID | Requirement | Priority |
|----|------------|----------|
| ORD-12 | View all past orders (paginated, newest first) | P0 |
| ORD-13 | Filter by status, date range | P1 |
| ORD-14 | View order detail (items, address, payment, status timeline) | P0 |
| ORD-15 | Re-order (add all items from a past order to cart) | P2 |
| ORD-16 | Download invoice PDF | P1 |

### 2.4 Order Cancellation

| ID | Requirement | Priority |
|----|------------|----------|
| ORD-17 | Customer can cancel order before SHIPPED status | P0 |
| ORD-18 | Cancellation reason required (dropdown + optional text) | P1 |
| ORD-19 | Stock restored on cancellation | P0 |
| ORD-20 | Automatic refund initiated on cancellation | P0 |
| ORD-21 | Cancellation confirmation email | P1 |

### 2.5 Returns & Refunds

| ID | Requirement | Priority |
|----|------------|----------|
| ORD-22 | Return request within 7 days of delivery | P1 |
| ORD-23 | Return reason required (damaged, wrong item, not as described, etc.) | P1 |
| ORD-24 | Return request photo upload (for damaged items) | P2 |
| ORD-25 | Admin approval for return requests | P1 |
| ORD-26 | Refund to original payment method (via Razorpay Refund API) | P1 |
| ORD-27 | Partial refund support | P2 |
| ORD-28 | Stock restored after return received | P1 |

---

## 3. Data Models

### 3.1 Order Entity

```java
@Entity
@Table(name = "orders", indexes = {
    @Index(name = "idx_order_user", columnList = "user_id"),
    @Index(name = "idx_order_status", columnList = "status"),
    @Index(name = "idx_order_number", columnList = "orderNumber", unique = true),
    @Index(name = "idx_order_created", columnList = "createdAt")
})
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String orderNumber;             // SV-20260725-A1B2C3

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    @OneToOne(mappedBy = "order", cascade = CascadeType.ALL)
    private Payment payment;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status = OrderStatus.PENDING;

    // Shipping address snapshot (denormalized for order immutability)
    @Embedded
    private OrderAddress shippingAddress;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;

    @Column(precision = 10, scale = 2)
    private BigDecimal taxAmount;

    @Column(precision = 10, scale = 2)
    private BigDecimal shippingCost;

    @Column(precision = 10, scale = 2)
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    private String trackingNumber;
    private String courierName;

    private String cancellationReason;
    private LocalDateTime cancelledAt;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    @OrderBy("createdAt ASC")
    private List<OrderStatusHistory> statusHistory = new ArrayList<>();

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    private LocalDateTime shippedAt;
    private LocalDateTime deliveredAt;
    private LocalDateTime completedAt;
}
```

### 3.2 Order Item Entity

```java
@Entity
@Table(name = "order_items")
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variant_id")
    private ProductVariant variant;

    // Snapshots at order time (immutable)
    @Column(nullable = false)
    private String productName;

    private String productImageUrl;
    private String variantInfo;         // "Color: Red, Size: XL"

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal lineTotal;

    @Enumerated(EnumType.STRING)
    private ReturnStatus returnStatus;  // null, REQUESTED, APPROVED, REJECTED, RETURNED, REFUNDED
}
```

### 3.3 Order Address (Embeddable)

```java
@Embeddable
public class OrderAddress {
    private String fullName;
    private String phone;
    private String addressLine1;
    private String addressLine2;
    private String city;
    private String state;
    private String postalCode;
    private String country;
}
```

### 3.4 Order Status History

```java
@Entity
@Table(name = "order_status_history")
public class OrderStatusHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Enumerated(EnumType.STRING)
    private OrderStatus fromStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus toStatus;

    private String note;
    private String changedBy;           // userId or "SYSTEM"

    @CreationTimestamp
    private LocalDateTime createdAt;
}
```

### 3.5 Enums

```java
public enum OrderStatus {
    PENDING,
    CONFIRMED,
    PROCESSING,
    SHIPPED,
    DELIVERED,
    COMPLETED,
    PAYMENT_FAILED,
    CANCELLED,
    RETURNED,
    REFUNDED
}

public enum ReturnStatus {
    REQUESTED,
    APPROVED,
    REJECTED,
    RETURNED,
    REFUNDED
}
```

---

## 4. API Endpoints

### 4.1 Customer Order APIs

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| GET | `/api/v1/orders` | List my orders (paginated) | Authenticated |
| GET | `/api/v1/orders/{orderNumber}` | Get order detail | Authenticated |
| POST | `/api/v1/orders/{orderNumber}/cancel` | Cancel order | Authenticated |
| POST | `/api/v1/orders/{orderNumber}/return` | Request return | Authenticated |
| GET | `/api/v1/orders/{orderNumber}/invoice` | Download invoice PDF | Authenticated |
| GET | `/api/v1/orders/{orderNumber}/track` | Get tracking info | Authenticated |

### 4.2 Admin Order APIs

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| GET | `/api/v1/admin/orders` | List all orders (paginated + filters) | Admin |
| GET | `/api/v1/admin/orders/{orderNumber}` | Get order detail | Admin |
| PUT | `/api/v1/admin/orders/{orderNumber}/status` | Update order status | Admin |
| PUT | `/api/v1/admin/orders/{orderNumber}/tracking` | Add tracking info | Admin |
| PUT | `/api/v1/admin/orders/{orderNumber}/return/approve` | Approve return | Admin |
| PUT | `/api/v1/admin/orders/{orderNumber}/return/reject` | Reject return | Admin |
| POST | `/api/v1/admin/orders/{orderNumber}/refund` | Process refund | Admin |

### 4.3 Query Parameters

```
GET /api/v1/orders?
    page=0
    &size=10
    &status=DELIVERED,COMPLETED
    &fromDate=2026-01-01
    &toDate=2026-07-25
    &sort=createdAt,desc
```

---

## 5. Order Number Generation

```java
/**
 * Format: SV-YYYYMMDD-XXXXXX
 * Example: SV-20260725-A1B2C3
 * 
 * - SV: ShopVerse prefix
 * - YYYYMMDD: Order date
 * - XXXXXX: 6-char alphanumeric (base-36 from sequence)
 */
@Service
public class OrderNumberGenerator {
    
    @Transactional
    public String generate() {
        String datePart = LocalDate.now()
            .format(DateTimeFormatter.BASIC_ISO_DATE);
        String randomPart = RandomStringUtils
            .randomAlphanumeric(6).toUpperCase();
        return "SV-" + datePart + "-" + randomPart;
    }
}
```

---

## 6. State Machine Transitions

```java
// Valid transitions map
private static final Map<OrderStatus, Set<OrderStatus>> VALID_TRANSITIONS = Map.of(
    PENDING,        Set.of(CONFIRMED, PAYMENT_FAILED, CANCELLED),
    CONFIRMED,      Set.of(PROCESSING, CANCELLED),
    PROCESSING,     Set.of(SHIPPED, CANCELLED),
    SHIPPED,        Set.of(DELIVERED),
    DELIVERED,      Set.of(COMPLETED, RETURNED),
    RETURNED,       Set.of(REFUNDED)
);

public void updateStatus(Order order, OrderStatus newStatus, String changedBy) {
    OrderStatus currentStatus = order.getStatus();
    
    if (!VALID_TRANSITIONS.getOrDefault(currentStatus, Set.of()).contains(newStatus)) {
        throw new InvalidStateTransitionException(
            "Cannot transition from " + currentStatus + " to " + newStatus
        );
    }
    
    // Log status change
    OrderStatusHistory history = new OrderStatusHistory();
    history.setFromStatus(currentStatus);
    history.setToStatus(newStatus);
    history.setChangedBy(changedBy);
    order.getStatusHistory().add(history);
    
    order.setStatus(newStatus);
    
    // Trigger side effects
    handleStatusChange(order, currentStatus, newStatus);
}
```

---

## 7. Frontend Pages

### 7.1 Order History Page (`/orders`)
- **Order Cards** — order number, date, status badge (color-coded), total, item thumbnails
- **Filter Bar** — status dropdown, date range picker
- **Pagination** — page controls
- **Empty State** — "No orders yet" with "Start Shopping" CTA

### 7.2 Order Detail Page (`/orders/{orderNumber}`)
- **Status Timeline** — visual progress bar with timestamps for each status change
- **Order Items** — product image, name, variant, quantity, price
- **Shipping Address** — formatted address display
- **Payment Info** — method, status, transaction ID
- **Price Breakdown** — subtotal, tax, shipping, discount, total
- **Actions** — "Cancel Order" (if eligible), "Return Item" (if eligible), "Download Invoice", "Re-order"
- **Tracking Section** — courier name, tracking number, external tracking link

---

## 8. Notification Events

| Event | Channel | Template |
|-------|---------|----------|
| Order Confirmed | Email + In-App | Order confirmation with items & total |
| Order Shipped | Email + In-App | Tracking number & courier info |
| Order Delivered | Email + In-App | Delivery confirmation + feedback request |
| Order Cancelled | Email | Cancellation confirmation + refund info |
| Refund Processed | Email | Refund amount & expected timeline |
| Return Approved | Email + In-App | Return instructions |
| Return Rejected | Email + In-App | Rejection reason |

---

> **Next:** Read [06-PRD-AdminDashboard.md](./06-PRD-AdminDashboard.md) for the Admin Dashboard specification.
