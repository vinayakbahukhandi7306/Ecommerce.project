# ⭐ PRD 07 — Reviews, Wishlist & Notifications

> **Module:** `review`, `wishlist`, `notification`  
> **Parent:** [01-PRD-Overview.md](./01-PRD-Overview.md)

---

## 1. Overview

This document covers three supplementary but important modules: product reviews & ratings, user wishlists, and the notification system (email + in-app). Together they enhance engagement, build trust, and keep users informed.

---

## Part A: Product Reviews & Ratings

### 2. Functional Requirements

| ID | Requirement | Priority |
|----|------------|----------|
| REV-01 | Authenticated users can write a review for a purchased product | P0 |
| REV-02 | One review per user per product (can edit) | P0 |
| REV-03 | Review includes: rating (1–5 stars), title, body text | P0 |
| REV-04 | Optional review images (up to 3 images) | P1 |
| REV-05 | Product page shows: average rating, rating distribution, reviews list | P0 |
| REV-06 | Sort reviews by: newest, highest rated, lowest rated, most helpful | P1 |
| REV-07 | "Was this helpful?" upvote on reviews | P1 |
| REV-08 | Verified purchase badge on reviews | P0 |
| REV-09 | Report abusive reviews | P1 |
| REV-10 | Admin moderation (approve, reject, delete) | P1 |
| REV-11 | Update product average rating & count on review CRUD (async) | P0 |

### 3. Data Model

```java
@Entity
@Table(name = "reviews", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "product_id"})
})
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private int rating;             // 1-5

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String body;

    @ElementCollection
    @CollectionTable(name = "review_images")
    private List<String> imageUrls = new ArrayList<>();

    private boolean verifiedPurchase;

    @Enumerated(EnumType.STRING)
    private ReviewStatus status = ReviewStatus.PUBLISHED;  // PUBLISHED, HIDDEN, FLAGGED

    private int helpfulCount;

    @ElementCollection
    @CollectionTable(name = "review_helpful_votes")
    @Column(name = "user_id")
    private Set<UUID> helpfulVotes = new HashSet<>();      // users who voted helpful

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
```

### 4. API Endpoints

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| GET | `/api/v1/products/{productId}/reviews` | List reviews for product (paginated) | Public |
| GET | `/api/v1/products/{productId}/reviews/summary` | Rating distribution summary | Public |
| POST | `/api/v1/products/{productId}/reviews` | Create review | Authenticated |
| PUT | `/api/v1/reviews/{id}` | Update own review | Authenticated |
| DELETE | `/api/v1/reviews/{id}` | Delete own review | Authenticated |
| POST | `/api/v1/reviews/{id}/helpful` | Toggle helpful vote | Authenticated |
| POST | `/api/v1/reviews/{id}/report` | Report review | Authenticated |
| GET | `/api/v1/admin/reviews` | List all reviews (admin) | Admin |
| PUT | `/api/v1/admin/reviews/{id}/status` | Moderate review | Admin |

### 5. Rating Distribution Response

```json
{
  "productId": "prod-uuid",
  "averageRating": 4.2,
  "totalReviews": 156,
  "distribution": {
    "5": 82,
    "4": 35,
    "3": 20,
    "2": 12,
    "1": 7
  }
}
```

### 6. Frontend Components

- **Star Rating Input** — clickable 5-star component for writing reviews
- **Star Rating Display** — filled/half/empty stars for showing ratings
- **Review Card** — user name, rating, date, title, body, images, helpful button, verified badge
- **Rating Summary** — average + distribution bar chart
- **Review Form** — star selector, title input, body textarea, image upload
- **Sort/Filter Bar** — sort dropdown + filter by rating

---

## Part B: Wishlist

### 7. Functional Requirements

| ID | Requirement | Priority |
|----|------------|----------|
| WISH-01 | Authenticated users can add/remove products to wishlist | P0 |
| WISH-02 | Wishlist page shows all wishlisted products | P0 |
| WISH-03 | "Add to Cart" from wishlist (with variant selection if needed) | P0 |
| WISH-04 | Heart icon toggle on product cards and detail page | P0 |
| WISH-05 | Wishlist count in header (badge) | P1 |
| WISH-06 | Price drop notification for wishlisted items (Phase 2) | P2 |
| WISH-07 | Back-in-stock notification for wishlisted items (Phase 2) | P2 |
| WISH-08 | Share wishlist via link (Phase 2) | P2 |

### 8. Data Model

```java
@Entity
@Table(name = "wishlist_items", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "product_id"})
})
public class WishlistItem {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(precision = 10, scale = 2)
    private BigDecimal priceAtAdd;      // for price drop detection

    @CreationTimestamp
    private LocalDateTime addedAt;
}
```

### 9. API Endpoints

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| GET | `/api/v1/wishlist` | List wishlist items | Authenticated |
| POST | `/api/v1/wishlist/{productId}` | Add to wishlist (toggle) | Authenticated |
| DELETE | `/api/v1/wishlist/{productId}` | Remove from wishlist | Authenticated |
| GET | `/api/v1/wishlist/check/{productId}` | Check if product is wishlisted | Authenticated |
| POST | `/api/v1/wishlist/{productId}/move-to-cart` | Move item to cart | Authenticated |

### 10. Frontend Page (`/wishlist`)

- **Product Grid** — same as product listing cards with heart icon filled
- **"Move to Cart"** button on each item
- **"Remove"** button on each item
- **Price change indicator** — if current price differs from `priceAtAdd`
- **Empty State** — "Your wishlist is empty" + "Explore Products" CTA

---

## Part C: Notification System

### 11. Functional Requirements

| ID | Requirement | Priority |
|----|------------|----------|
| NOTIF-01 | Transactional emails for order lifecycle events | P0 |
| NOTIF-02 | Welcome email on registration | P0 |
| NOTIF-03 | Password reset email | P0 |
| NOTIF-04 | Email verification email | P0 |
| NOTIF-05 | HTML email templates (responsive, branded) | P1 |
| NOTIF-06 | In-app notification bell with unread count | P1 |
| NOTIF-07 | In-app notification list (dropdown) | P1 |
| NOTIF-08 | Mark notification as read / mark all as read | P1 |
| NOTIF-09 | Push notifications (browser — Phase 2) | P2 |
| NOTIF-10 | SMS notifications (Phase 2) | P2 |
| NOTIF-11 | Notification preferences (user can opt in/out per type) | P2 |

### 12. Email Templates

| Template | Trigger | Content |
|----------|---------|---------|
| `welcome.html` | User registration | Welcome message + verify email CTA |
| `email-verification.html` | Registration / resend | Verification link |
| `password-reset.html` | Forgot password | Reset link (1h expiry) |
| `order-confirmation.html` | Order placed | Order number, items, total, address |
| `order-shipped.html` | Order shipped | Tracking number, courier, expected delivery |
| `order-delivered.html` | Order delivered | Delivery confirmation + review CTA |
| `order-cancelled.html` | Order cancelled | Cancellation details + refund info |
| `refund-processed.html` | Refund completed | Refund amount + transaction details |
| `return-approved.html` | Return approved | Return instructions |

### 13. Email Service Architecture

```java
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;  // Thymeleaf

    @Async("emailTaskExecutor")   // async thread pool
    public void sendOrderConfirmation(Order order) {
        Context context = new Context();
        context.setVariable("order", order);
        context.setVariable("items", order.getItems());
        
        String html = templateEngine.process("order-confirmation", context);
        
        sendHtmlEmail(
            order.getUser().getEmail(),
            "Order Confirmed - " + order.getOrderNumber(),
            html
        );
    }

    private void sendHtmlEmail(String to, String subject, String htmlBody) {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlBody, true);
        helper.setFrom("noreply@shopverse.com");
        mailSender.send(message);
    }
}
```

### 14. In-App Notification Model

```java
@Entity
@Table(name = "notifications", indexes = {
    @Index(name = "idx_notif_user", columnList = "user_id"),
    @Index(name = "idx_notif_read", columnList = "user_id, is_read")
})
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    private NotificationType type;      // ORDER_UPDATE, PROMOTION, SYSTEM, REVIEW

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String message;

    private String actionUrl;           // deep link (e.g., /orders/SV-20260725-A1B2C3)

    private boolean isRead;

    @CreationTimestamp
    private LocalDateTime createdAt;
}
```

### 15. Notification API Endpoints

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| GET | `/api/v1/notifications` | List notifications (paginated) | Authenticated |
| GET | `/api/v1/notifications/unread-count` | Get unread count | Authenticated |
| PUT | `/api/v1/notifications/{id}/read` | Mark as read | Authenticated |
| PUT | `/api/v1/notifications/read-all` | Mark all as read | Authenticated |
| DELETE | `/api/v1/notifications/{id}` | Delete notification | Authenticated |

---

## 16. Frontend Components (Notifications)

### Notification Bell (Header)
- Bell icon with red badge showing unread count
- Click opens dropdown with latest 5 notifications
- "View All" link to full notifications page
- Each notification: icon (by type), title, message snippet, time ago

### Notification Center Page (`/notifications`)
- Full list of notifications (paginated)
- Unread highlighted with different background
- Click to navigate to `actionUrl`
- "Mark All as Read" button

---

> **Next:** Read [08-PRD-Database-API.md](./08-PRD-Database-API.md) for the Database Schema & API Reference specification.
