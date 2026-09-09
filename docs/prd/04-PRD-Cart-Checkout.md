# 🛒 PRD 04 — Shopping Cart & Checkout

> **Module:** `cart`, `payment`  
> **Parent:** [01-PRD-Overview.md](./01-PRD-Overview.md)

---

## 1. Overview

This module handles the shopping cart lifecycle (add, update, remove items) and the checkout flow (address selection, payment processing, order placement). The cart persists server-side for authenticated users and in localStorage for guests.

---

## 2. Functional Requirements

### 2.1 Shopping Cart

| ID | Requirement | Priority |
|----|------------|----------|
| CART-01 | Authenticated users: cart stored in DB (server-side) | P0 |
| CART-02 | Guest users: cart stored in localStorage (client-side) | P1 |
| CART-03 | Merge guest cart into user cart on login | P1 |
| CART-04 | Add product to cart (with variant selection if applicable) | P0 |
| CART-05 | Update item quantity (with stock validation) | P0 |
| CART-06 | Remove item from cart | P0 |
| CART-07 | Clear entire cart | P0 |
| CART-08 | Cart summary: subtotal, item count, estimated tax, total | P0 |
| CART-09 | Real-time stock validation (prevent adding more than available) | P0 |
| CART-10 | Price change detection (notify user if price changed since add) | P1 |
| CART-11 | Coupon code application (Phase 2) | P2 |
| CART-12 | Cart persistence across sessions (for authenticated users) | P0 |
| CART-13 | Cart item limit: max 50 unique items | P1 |

#### Cart Flow

```
Add to Cart
    │
    ▼
Validate: Product exists? Active? In stock?
    │
    ├── YES → Check if item already in cart
    │           ├── YES → Update quantity (validate stock)
    │           └── NO  → Add new cart item
    │
    └── NO → Return error (404 / 400 / out of stock)
    │
    ▼
Recalculate cart totals
    │
    ▼
Return updated cart summary
```

### 2.2 Checkout Flow

| ID | Requirement | Priority |
|----|------------|----------|
| CHECK-01 | Multi-step checkout: Address → Payment → Review → Confirm | P0 |
| CHECK-02 | Select existing address or add new address | P0 |
| CHECK-03 | Order summary with itemized pricing | P0 |
| CHECK-04 | Payment integration (Razorpay / Stripe) | P0 |
| CHECK-05 | Final stock re-validation before order placement | P0 |
| CHECK-06 | Order confirmation page with order number | P0 |
| CHECK-07 | Order confirmation email (async) | P1 |
| CHECK-08 | Checkout as guest (Phase 2 — requires email for order tracking) | P2 |

#### Checkout Flow Diagram

```
┌───────────────┐    ┌───────────────┐    ┌───────────────┐    ┌───────────────┐
│  Step 1       │    │  Step 2       │    │  Step 3       │    │  Step 4       │
│  SHIPPING     │ →  │  PAYMENT      │ →  │  REVIEW       │ →  │  CONFIRMATION │
│  ADDRESS      │    │  METHOD       │    │  ORDER        │    │  SUCCESS      │
│               │    │               │    │               │    │               │
│ • Select addr │    │ • Card        │    │ • Items list  │    │ • Order #     │
│ • Add new     │    │ • UPI         │    │ • Address     │    │ • Summary     │
│ • Edit        │    │ • Net Banking │    │ • Payment     │    │ • Track link  │
│               │    │ • COD         │    │ • Totals      │    │ • Continue    │
└───────────────┘    └───────────────┘    └───────────────┘    └───────────────┘
```

### 2.3 Payment Integration

| ID | Requirement | Priority |
|----|------------|----------|
| PAY-01 | Razorpay integration (recommended for Indian market) | P0 |
| PAY-02 | Stripe integration (international fallback) | P1 |
| PAY-03 | Cash on Delivery (COD) option | P0 |
| PAY-04 | Payment methods: Credit/Debit Card, UPI, Net Banking, Wallets | P0 |
| PAY-05 | Secure payment — PCI-DSS compliance (via Razorpay hosted checkout) | P0 |
| PAY-06 | Payment webhook handling (success, failure, refund) | P0 |
| PAY-07 | Idempotent payment creation (prevent double charges) | P0 |
| PAY-08 | Payment timeout handling (15 min window) | P1 |
| PAY-09 | Retry failed payments | P1 |

#### Payment Processing Flow

```
Frontend                     Backend                      Razorpay
   │                            │                            │
   │  POST /checkout/initiate   │                            │
   │ ──────────────────────────>│                            │
   │                            │  Create Razorpay Order     │
   │                            │ ──────────────────────────>│
   │                            │  <── razorpay_order_id ────│
   │  <── { orderId, razorpay   │                            │
   │       OrderId, amount }    │                            │
   │                            │                            │
   │  Open Razorpay Checkout    │                            │
   │  (Client-side SDK)         │                            │
   │ ─────────────────────────────────────────────────────── >│
   │                            │                            │
   │  <── Payment Success ───────────────────────────────────│
   │  { razorpay_payment_id,    │                            │
   │    razorpay_signature }    │                            │
   │                            │                            │
   │  POST /checkout/verify     │                            │
   │ ──────────────────────────>│                            │
   │                            │  Verify Signature          │
   │                            │  (HMAC-SHA256)             │
   │                            │                            │
   │                            │  Create Order              │
   │                            │  Deduct Stock              │
   │                            │  Clear Cart                │
   │                            │  Send Confirmation Email   │
   │  <── { order details } ────│                            │
   │                            │                            │
   │                       ┌────┴────┐                       │
   │                       │ Webhook │ (async backup)        │
   │                       │ POST /webhooks/razorpay         │
   │                       └─────────┘                       │
```

---

## 3. Data Models

### 3.1 Cart Entity

```java
@Entity
@Table(name = "carts")
public class Cart {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CartItem> items = new ArrayList<>();

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    // Transient calculated fields
    @Transient
    public BigDecimal getSubtotal() {
        return items.stream()
            .map(CartItem::getLineTotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Transient
    public int getTotalItems() {
        return items.stream()
            .mapToInt(CartItem::getQuantity)
            .sum();
    }
}
```

### 3.2 Cart Item Entity

```java
@Entity
@Table(name = "cart_items",
    uniqueConstraints = @UniqueConstraint(
        columnNames = {"cart_id", "product_id", "variant_id"}
    ))
public class CartItem {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variant_id")
    private ProductVariant variant;     // null if no variant

    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;       // price at time of adding

    @CreationTimestamp
    private LocalDateTime addedAt;

    @Transient
    public BigDecimal getLineTotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}
```

### 3.3 Payment Entity

```java
@Entity
@Table(name = "payments")
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Enumerated(EnumType.STRING)
    private PaymentMethod method;       // CARD, UPI, NET_BANKING, COD, WALLET

    @Enumerated(EnumType.STRING)
    private PaymentStatus status;       // PENDING, SUCCESS, FAILED, REFUNDED

    @Column(precision = 10, scale = 2)
    private BigDecimal amount;

    private String currency = "INR";

    private String razorpayOrderId;
    private String razorpayPaymentId;
    private String razorpaySignature;

    private String failureReason;

    @CreationTimestamp
    private LocalDateTime createdAt;

    private LocalDateTime paidAt;
}
```

---

## 4. API Endpoints

### 4.1 Cart APIs

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| GET | `/api/v1/cart` | Get current user's cart | Authenticated |
| POST | `/api/v1/cart/items` | Add item to cart | Authenticated |
| PUT | `/api/v1/cart/items/{itemId}` | Update item quantity | Authenticated |
| DELETE | `/api/v1/cart/items/{itemId}` | Remove item from cart | Authenticated |
| DELETE | `/api/v1/cart` | Clear entire cart | Authenticated |
| POST | `/api/v1/cart/merge` | Merge guest cart on login | Authenticated |

### 4.2 Checkout APIs

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| POST | `/api/v1/checkout/initiate` | Start checkout → create payment order | Authenticated |
| POST | `/api/v1/checkout/verify` | Verify payment & place order | Authenticated |
| POST | `/api/v1/checkout/cod` | Place COD order (no payment flow) | Authenticated |
| POST | `/api/v1/webhooks/razorpay` | Razorpay webhook handler | Public (verified) |

### 4.3 Request / Response Examples

#### Add to Cart Request

```json
POST /api/v1/cart/items
{
  "productId": "123e4567-e89b-12d3-a456-426614174000",
  "variantId": "987fcdeb-51a2-3b4c-d567-890123456789",  // optional
  "quantity": 2
}
```

#### Cart Response

```json
{
  "id": "cart-uuid",
  "items": [
    {
      "id": "cart-item-uuid",
      "product": {
        "id": "prod-uuid",
        "name": "iPhone 15 Pro",
        "slug": "iphone-15-pro",
        "imageUrl": "https://...",
        "price": 134900.00,
        "stockQuantity": 25,
        "status": "ACTIVE"
      },
      "variant": {
        "id": "var-uuid",
        "name": "Storage",
        "value": "256GB"
      },
      "quantity": 2,
      "unitPrice": 134900.00,
      "lineTotal": 269800.00,
      "priceChanged": false
    }
  ],
  "subtotal": 269800.00,
  "totalItems": 2,
  "estimatedTax": 48564.00,
  "total": 318364.00
}
```

#### Checkout Initiate Request

```json
POST /api/v1/checkout/initiate
{
  "addressId": "addr-uuid",
  "paymentMethod": "CARD"        // CARD, UPI, NET_BANKING, COD
}
```

---

## 5. Cart Validation Rules

| Rule | Handling |
|------|----------|
| Product not found / deleted | Remove item, notify user |
| Product out of stock | Mark item as unavailable, block checkout |
| Quantity > available stock | Adjust quantity to max available, notify user |
| Price changed since add | Update price, show old vs new price to user |
| Product became inactive | Remove from cart, notify user |
| Cart is empty | Block checkout, show empty cart page |
| Max items exceeded (50) | Prevent adding, show limit message |

---

## 6. Frontend Pages

### 6.1 Cart Page (`/cart`)
- **Cart Items List** — product image, name, variant, unit price, quantity control (+/-), line total, remove button
- **Price Change Alert** — yellow banner if any prices changed
- **Stock Warning** — red text for low stock / out of stock items
- **Cart Summary Sidebar** — subtotal, estimated tax, estimated shipping, total, "Proceed to Checkout" button
- **Empty Cart State** — illustration + "Continue Shopping" button
- **"Save for Later"** section (uses wishlist)

### 6.2 Checkout Page (`/checkout`)
- **Progress Steps Bar** — visual step indicator (Shipping → Payment → Review)
- **Step 1: Shipping** — address cards with select radio, "Add New Address" form
- **Step 2: Payment** — payment method selection, Razorpay modal integration
- **Step 3: Review** — order summary, edit buttons to go back, "Place Order" CTA
- **Order Confirmation** — success checkmark animation, order number, expected delivery, "View Order" button

---

## 7. Concurrency & Race Conditions

| Scenario | Solution |
|----------|----------|
| Two users buy last item simultaneously | Optimistic locking on stock (`@Version`), first commit wins |
| Stock depleted during checkout | Re-validate stock at order creation, fail gracefully |
| Payment succeeds but order creation fails | Webhook-based reconciliation, idempotency keys |
| Double-click on "Place Order" | Idempotency key in request, debounce on frontend |
| Cart modified during checkout | Re-fetch cart at review step, show diff if changed |

```java
@Entity
@Table(name = "products")
public class Product {
    // ... other fields

    @Version
    private Long version;  // Optimistic locking for concurrent stock updates

    @Column(nullable = false)
    private int stockQuantity;
}
```

---

> **Next:** Read [05-PRD-OrderManagement.md](./05-PRD-OrderManagement.md) for the Order Management & Tracking specification.
