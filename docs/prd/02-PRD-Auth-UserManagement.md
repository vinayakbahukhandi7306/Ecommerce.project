# 🔐 PRD 02 — Authentication & User Management

> **Module:** `auth`, `user`  
> **Parent:** [01-PRD-Overview.md](./01-PRD-Overview.md)

---

## 1. Overview

This module handles user registration, authentication (JWT + OAuth2), authorization (role-based access control), password management, and user profile operations. It is the security backbone of ShopVerse.

---

## 2. Functional Requirements

### 2.1 User Registration

| ID | Requirement | Priority |
|----|------------|----------|
| AUTH-01 | Users can register with email, password, first name, last name | P0 |
| AUTH-02 | Email must be unique and validated via regex + verification email | P0 |
| AUTH-03 | Password must meet strength requirements (min 8 chars, 1 uppercase, 1 number, 1 special) | P0 |
| AUTH-04 | Passwords are stored using BCrypt hashing (strength 12) | P0 |
| AUTH-05 | Email verification link sent on registration (token-based, expires in 24h) | P1 |
| AUTH-06 | Phone number (optional) with OTP verification (Phase 2) | P2 |

#### Registration Flow

```
User fills form → POST /api/v1/auth/register
    → Validate input (Bean Validation)
    → Check email uniqueness
    → Hash password (BCrypt)
    → Save user (role = CUSTOMER, status = UNVERIFIED)
    → Generate email verification token (UUID, TTL = 24h)
    → Send verification email (async via Spring @Async)
    → Return 201 Created + user summary DTO
```

### 2.2 Authentication (Login)

| ID | Requirement | Priority |
|----|------------|----------|
| AUTH-07 | Login with email + password | P0 |
| AUTH-08 | Return JWT access token (15 min TTL) + refresh token (7 day TTL) | P0 |
| AUTH-09 | Refresh token rotation — old refresh token invalidated on use | P0 |
| AUTH-10 | Account lockout after 5 failed attempts (lock for 30 min) | P1 |
| AUTH-11 | OAuth2 social login (Google, GitHub) | P1 |
| AUTH-12 | "Remember Me" functionality (extend refresh token to 30 days) | P2 |

#### JWT Token Strategy

```
Access Token (Short-lived):
  ┌──────────────────────────────────────────────────┐
  │ Header: { alg: HS512, typ: JWT }                 │
  │ Payload: {                                       │
  │   sub: userId,                                   │
  │   email: "user@email.com",                       │
  │   roles: ["CUSTOMER"],                           │
  │   iat: <issued-at>,                              │
  │   exp: <issued-at + 15min>                       │
  │ }                                                │
  │ Signature: HMAC-SHA512(header.payload, secret)   │
  └──────────────────────────────────────────────────┘

Refresh Token (Long-lived):
  - Stored in DB (refresh_tokens table)
  - UUID-based, not JWT
  - Linked to user_id + device fingerprint
  - Single-use (rotation on every refresh)
```

#### Login Flow

```
POST /api/v1/auth/login { email, password }
    → Authenticate via AuthenticationManager
    → If failed → increment failed_attempts → 423 Locked (if >= 5)
    → If success → reset failed_attempts
    → Generate access token (JWT) + refresh token (UUID)
    → Store refresh token in DB
    → Return { accessToken, refreshToken, expiresIn, user }
```

#### Token Refresh Flow

```
POST /api/v1/auth/refresh { refreshToken }
    → Look up refresh token in DB
    → Validate: not expired, not revoked, matches user
    → Revoke old refresh token
    → Generate new access token + new refresh token
    → Store new refresh token in DB
    → Return { accessToken, refreshToken, expiresIn }
```

### 2.3 OAuth2 Social Login

| ID | Requirement | Priority |
|----|------------|----------|
| AUTH-13 | Google OAuth2 login/register | P1 |
| AUTH-14 | GitHub OAuth2 login/register | P1 |
| AUTH-15 | Auto-create user on first social login (no password required) | P1 |
| AUTH-16 | Link social account to existing account (same email) | P1 |

#### OAuth2 Flow

```
1. Frontend redirects to: GET /oauth2/authorize/google
2. Spring Security redirects to Google consent screen
3. User grants permission → Google redirects back with auth code
4. Backend exchanges code for Google access token
5. Fetch user info from Google (name, email, avatar)
6. Find or create user in DB
7. Generate ShopVerse JWT tokens
8. Redirect to frontend with tokens: /oauth2/callback?token=...
```

### 2.4 Password Management

| ID | Requirement | Priority |
|----|------------|----------|
| AUTH-17 | Forgot password — send reset link to email (token-based, 1h TTL) | P0 |
| AUTH-18 | Reset password via token + new password | P0 |
| AUTH-19 | Change password (authenticated) — requires current password | P0 |
| AUTH-20 | Password history — prevent reuse of last 3 passwords (Phase 2) | P2 |

### 2.5 Authorization (RBAC)

| ID | Requirement | Priority |
|----|------------|----------|
| AUTH-21 | Role-based access control (GUEST, CUSTOMER, ADMIN) | P0 |
| AUTH-22 | Method-level security via `@PreAuthorize` annotations | P0 |
| AUTH-23 | Custom `@CurrentUser` annotation to inject authenticated user | P0 |
| AUTH-24 | API endpoints protected by role (see table below) | P0 |

#### Endpoint Access Matrix

| Endpoint Pattern | GUEST | CUSTOMER | ADMIN |
|-----------------|-------|----------|-------|
| `POST /auth/**` | ✅ | ✅ | ✅ |
| `GET /products/**` | ✅ | ✅ | ✅ |
| `GET /categories/**` | ✅ | ✅ | ✅ |
| `POST/PUT/DELETE /cart/**` | ❌ | ✅ | ✅ |
| `POST /orders/**` | ❌ | ✅ | ✅ |
| `GET /orders/my` | ❌ | ✅ | ✅ |
| `GET/PUT /profile/**` | ❌ | ✅ | ✅ |
| `POST /reviews/**` | ❌ | ✅ | ✅ |
| `POST/PUT/DELETE /admin/**` | ❌ | ❌ | ✅ |

### 2.6 User Profile Management

| ID | Requirement | Priority |
|----|------------|----------|
| USER-01 | View own profile (name, email, phone, avatar, join date) | P0 |
| USER-02 | Update profile (name, phone, avatar) | P0 |
| USER-03 | Upload profile avatar (image upload to S3/MinIO) | P1 |
| USER-04 | Manage multiple shipping addresses (CRUD) | P0 |
| USER-05 | Set default shipping address | P0 |
| USER-06 | View account activity (login history) — Phase 2 | P2 |
| USER-07 | Delete account (soft delete, data retained 30 days) | P1 |

---

## 3. Data Models

### 3.1 User Entity

```java
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String email;

    private String password;        // null for OAuth-only users

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    private String phone;
    private String avatarUrl;

    @Enumerated(EnumType.STRING)
    private Role role;              // CUSTOMER, ADMIN

    @Enumerated(EnumType.STRING)
    private AuthProvider provider;  // LOCAL, GOOGLE, GITHUB

    private String providerId;      // OAuth provider user ID

    private boolean emailVerified;
    private int failedLoginAttempts;
    private LocalDateTime lockoutUntil;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    private boolean deleted;        // soft delete flag

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Address> addresses;
}
```

### 3.2 Address Entity

```java
@Entity
@Table(name = "addresses")
public class Address {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private String label;           // "Home", "Work", etc.
    private String fullName;
    private String phone;
    private String addressLine1;
    private String addressLine2;
    private String city;
    private String state;
    private String postalCode;
    private String country;
    private boolean isDefault;

    @CreationTimestamp
    private LocalDateTime createdAt;
}
```

### 3.3 Refresh Token Entity

```java
@Entity
@Table(name = "refresh_tokens")
public class RefreshToken {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String token;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private String deviceInfo;      // user-agent / fingerprint
    private LocalDateTime expiresAt;
    private boolean revoked;

    @CreationTimestamp
    private LocalDateTime createdAt;
}
```

---

## 4. API Endpoints

### 4.1 Authentication APIs

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| POST | `/api/v1/auth/register` | Register new user | Public |
| POST | `/api/v1/auth/login` | Login (email + password) | Public |
| POST | `/api/v1/auth/refresh` | Refresh access token | Public |
| POST | `/api/v1/auth/logout` | Revoke refresh token | Authenticated |
| POST | `/api/v1/auth/forgot-password` | Request password reset | Public |
| POST | `/api/v1/auth/reset-password` | Reset password with token | Public |
| PUT | `/api/v1/auth/change-password` | Change password | Authenticated |
| GET | `/api/v1/auth/verify-email?token=` | Verify email address | Public |
| GET | `/oauth2/authorize/{provider}` | Initiate OAuth2 flow | Public |

### 4.2 User Profile APIs

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| GET | `/api/v1/users/me` | Get current user profile | Authenticated |
| PUT | `/api/v1/users/me` | Update profile | Authenticated |
| POST | `/api/v1/users/me/avatar` | Upload avatar | Authenticated |
| DELETE | `/api/v1/users/me` | Soft delete account | Authenticated |
| GET | `/api/v1/users/me/addresses` | List addresses | Authenticated |
| POST | `/api/v1/users/me/addresses` | Add address | Authenticated |
| PUT | `/api/v1/users/me/addresses/{id}` | Update address | Authenticated |
| DELETE | `/api/v1/users/me/addresses/{id}` | Delete address | Authenticated |
| PUT | `/api/v1/users/me/addresses/{id}/default` | Set default address | Authenticated |

---

## 5. Request / Response DTOs

### 5.1 Registration Request

```json
{
  "firstName": "Vinayak",
  "lastName": "Sharma",
  "email": "vinayak@email.com",
  "password": "SecurePass@123",
  "confirmPassword": "SecurePass@123"
}
```

### 5.2 Login Response

```json
{
  "accessToken": "eyJhbGciOiJIUzUxMiJ9...",
  "refreshToken": "550e8400-e29b-41d4-a716-446655440000",
  "tokenType": "Bearer",
  "expiresIn": 900,
  "user": {
    "id": "123e4567-e89b-12d3-a456-426614174000",
    "email": "vinayak@email.com",
    "firstName": "Vinayak",
    "lastName": "Sharma",
    "role": "CUSTOMER",
    "avatarUrl": null
  }
}
```

### 5.3 Standardized Error Response

```json
{
  "timestamp": "2026-07-25T00:00:00Z",
  "status": 401,
  "error": "Unauthorized",
  "message": "Invalid email or password",
  "path": "/api/v1/auth/login"
}
```

---

## 6. Security Configuration

### Spring Security Filter Chain

```
HTTP Request
    │
    ▼
CorsFilter
    │
    ▼
JwtAuthenticationFilter (OncePerRequestFilter)
    │ Extract JWT from Authorization header
    │ Validate token → Load UserDetails
    │ Set SecurityContext
    │
    ▼
ExceptionTranslationFilter
    │
    ▼
FilterSecurityInterceptor
    │ Check @PreAuthorize / endpoint security
    │
    ▼
Controller
```

### Key Security Configs

```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    // Stateless session (no server-side session)
    // CSRF disabled (using JWT, not cookies)
    // CORS configured for frontend origin
    // Public endpoints: /auth/**, /products/**, /categories/**
    // All other endpoints: authenticated
    // OAuth2 login configured with custom success handler
}
```

---

## 7. Frontend Pages

### 7.1 Login Page
- Email + password form
- "Forgot Password?" link
- "Sign in with Google" / "Sign in with GitHub" buttons
- Link to registration page
- Form validation with real-time feedback

### 7.2 Registration Page
- First name, last name, email, password, confirm password
- Password strength indicator
- Terms & conditions checkbox
- Social signup buttons

### 7.3 Profile Page
- Display user info (avatar, name, email, phone)
- Edit mode toggle
- Address management section (list, add, edit, delete, set default)
- "Change Password" section
- "Delete Account" button (with confirmation modal)

---

## 8. Edge Cases & Error Handling

| Scenario | Handling |
|----------|----------|
| Duplicate email registration | 409 Conflict with clear message |
| Expired JWT | 401 Unauthorized, frontend auto-refreshes |
| Expired refresh token | 401, redirect to login |
| Account locked | 423 Locked, include unlock time |
| Invalid reset token | 400 Bad Request |
| OAuth user tries password login | 400 "Please use Google to sign in" |
| Concurrent refresh token use | Revoke all tokens for user (possible theft) |

---

> **Next:** Read [03-PRD-ProductCatalog-Search.md](./03-PRD-ProductCatalog-Search.md) for the Product Catalog & Search specification.
