# 🛍️ PRD 03 — Product Catalog & Search

> **Module:** `product`, `category`  
> **Parent:** [01-PRD-Overview.md](./01-PRD-Overview.md)

---

## 1. Overview

The Product Catalog is the core browsing experience of ShopVerse. It handles product listing, categorization, filtering, sorting, search, and product detail views. Designed for performance with Redis caching and future Elasticsearch integration.

---

## 2. Functional Requirements

### 2.1 Category Management

| ID | Requirement | Priority |
|----|------------|----------|
| CAT-01 | Hierarchical categories (parent → child, max 3 levels deep) | P0 |
| CAT-02 | Category has: name, slug, description, image, parentId | P0 |
| CAT-03 | Admin can CRUD categories | P0 |
| CAT-04 | Category tree API — returns nested structure | P0 |
| CAT-05 | SEO-friendly slugs (e.g., `/electronics/smartphones`) | P1 |
| CAT-06 | Category-level product count | P1 |

#### Category Hierarchy Example

```
Electronics
├── Smartphones
│   ├── Android
│   └── iPhone
├── Laptops
│   ├── Gaming
│   └── Business
└── Accessories
    ├── Cases
    └── Chargers

Fashion
├── Men
│   ├── Shirts
│   └── Shoes
└── Women
    ├── Dresses
    └── Shoes
```

### 2.2 Product Management

| ID | Requirement | Priority |
|----|------------|----------|
| PROD-01 | Product has: name, slug, description, price, SKU, stock, images, category, brand | P0 |
| PROD-02 | Multiple product images (1 primary + up to 5 gallery images) | P0 |
| PROD-03 | Product variants (size, color, storage) with separate stock tracking | P1 |
| PROD-04 | Product status: ACTIVE, DRAFT, OUT_OF_STOCK, DISCONTINUED | P0 |
| PROD-05 | Rich text product description (HTML/Markdown) | P1 |
| PROD-06 | Product tags for cross-cutting categorization | P1 |
| PROD-07 | Related products (auto-generated from same category) | P1 |
| PROD-08 | Featured / bestseller / new arrival flags | P0 |
| PROD-09 | Discount pricing (original price, sale price, discount %) | P0 |
| PROD-10 | Soft delete products (never hard delete — order history integrity) | P0 |

### 2.3 Product Listing & Browsing

| ID | Requirement | Priority |
|----|------------|----------|
| BROWSE-01 | Paginated product listing (default 12 per page, configurable) | P0 |
| BROWSE-02 | Filter by: category, price range, brand, rating, availability | P0 |
| BROWSE-03 | Sort by: price (asc/desc), rating, newest, popularity, name | P0 |
| BROWSE-04 | Multi-filter support (combine filters with AND logic) | P0 |
| BROWSE-05 | Price range slider with min/max bounds | P1 |
| BROWSE-06 | Active filter chips with remove capability | P1 |
| BROWSE-07 | Grid / List view toggle | P1 |
| BROWSE-08 | Product card: image, name, price, rating, "Add to Cart" button | P0 |
| BROWSE-09 | Quick view modal (hover/click on product card) | P2 |
| BROWSE-10 | Infinite scroll OR pagination controls (configurable) | P1 |

### 2.4 Product Detail Page

| ID | Requirement | Priority |
|----|------------|----------|
| DETAIL-01 | Full product info: images gallery, name, price, description, specs | P0 |
| DETAIL-02 | Image carousel with zoom on hover | P0 |
| DETAIL-03 | Variant selector (size, color) with stock indication per variant | P1 |
| DETAIL-04 | Quantity selector with stock limit validation | P0 |
| DETAIL-05 | "Add to Cart" button (with visual feedback) | P0 |
| DETAIL-06 | "Add to Wishlist" button (heart icon toggle) | P1 |
| DETAIL-07 | Product reviews section (see PRD-07) | P0 |
| DETAIL-08 | Related products carousel | P1 |
| DETAIL-09 | Breadcrumb navigation (Home > Category > Subcategory > Product) | P0 |
| DETAIL-10 | Share product (copy link, social media) | P2 |
| DETAIL-11 | Stock status indicator (In Stock / Low Stock / Out of Stock) | P0 |

### 2.5 Search

| ID | Requirement | Priority |
|----|------------|----------|
| SEARCH-01 | Search bar in header (globally accessible) | P0 |
| SEARCH-02 | Search by product name, description, brand, tags | P0 |
| SEARCH-03 | Search results page with same filter/sort as listing | P0 |
| SEARCH-04 | Search autocomplete / suggestions (debounced, top 5 results) | P1 |
| SEARCH-05 | Search highlighting in results | P1 |
| SEARCH-06 | Recent searches (stored in localStorage) | P2 |
| SEARCH-07 | "No results" page with suggestions | P1 |
| SEARCH-08 | Elasticsearch integration for full-text search (Phase 2) | P2 |

---

## 3. Data Models

### 3.1 Category Entity

```java
@Entity
@Table(name = "categories")
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false, unique = true)
    private String slug;

    private String description;
    private String imageUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Category parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
    @OrderBy("name ASC")
    private List<Category> children = new ArrayList<>();

    private int displayOrder;
    private boolean active;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
```

### 3.2 Product Entity

```java
@Entity
@Table(name = "products", indexes = {
    @Index(name = "idx_product_slug", columnList = "slug"),
    @Index(name = "idx_product_category", columnList = "category_id"),
    @Index(name = "idx_product_brand", columnList = "brand"),
    @Index(name = "idx_product_price", columnList = "price"),
    @Index(name = "idx_product_status", columnList = "status")
})
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String slug;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String shortDescription;

    @Column(nullable = false, unique = true)
    private String sku;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(precision = 10, scale = 2)
    private BigDecimal originalPrice;       // for discount display

    @Column(nullable = false)
    private int stockQuantity;

    private int lowStockThreshold = 5;

    private String brand;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("displayOrder ASC")
    private List<ProductImage> images = new ArrayList<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductVariant> variants = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "product_tags")
    private Set<String> tags = new HashSet<>();

    @Enumerated(EnumType.STRING)
    private ProductStatus status = ProductStatus.ACTIVE;

    private boolean featured;
    private boolean bestSeller;
    private boolean newArrival;

    private double averageRating;
    private int reviewCount;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    private boolean deleted;
}
```

### 3.3 Product Image Entity

```java
@Entity
@Table(name = "product_images")
public class ProductImage {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private String imageUrl;

    private String altText;
    private boolean isPrimary;
    private int displayOrder;
}
```

### 3.4 Product Variant Entity

```java
@Entity
@Table(name = "product_variants")
public class ProductVariant {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    private String variantName;     // e.g., "Color", "Size"
    private String variantValue;    // e.g., "Red", "XL", "128GB"

    private String sku;             // variant-specific SKU
    private BigDecimal priceAdjustment = BigDecimal.ZERO;
    private int stockQuantity;
    private String imageUrl;        // variant-specific image
}
```

---

## 4. API Endpoints

### 4.1 Category APIs

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| GET | `/api/v1/categories` | List all root categories | Public |
| GET | `/api/v1/categories/tree` | Get full category tree (nested) | Public |
| GET | `/api/v1/categories/{slug}` | Get category by slug | Public |
| GET | `/api/v1/categories/{id}/products` | Products in category (paginated) | Public |
| POST | `/api/v1/admin/categories` | Create category | Admin |
| PUT | `/api/v1/admin/categories/{id}` | Update category | Admin |
| DELETE | `/api/v1/admin/categories/{id}` | Delete category (if empty) | Admin |

### 4.2 Product APIs

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| GET | `/api/v1/products` | List products (paginated + filter + sort) | Public |
| GET | `/api/v1/products/{slug}` | Get product detail by slug | Public |
| GET | `/api/v1/products/featured` | Get featured products | Public |
| GET | `/api/v1/products/new-arrivals` | Get new arrivals | Public |
| GET | `/api/v1/products/best-sellers` | Get best sellers | Public |
| GET | `/api/v1/products/search?q=` | Search products | Public |
| GET | `/api/v1/products/search/suggest?q=` | Search autocomplete | Public |
| POST | `/api/v1/admin/products` | Create product | Admin |
| PUT | `/api/v1/admin/products/{id}` | Update product | Admin |
| DELETE | `/api/v1/admin/products/{id}` | Soft delete product | Admin |
| POST | `/api/v1/admin/products/{id}/images` | Upload product images | Admin |
| DELETE | `/api/v1/admin/products/{id}/images/{imgId}` | Remove product image | Admin |

### 4.3 Query Parameters for Product Listing

```
GET /api/v1/products?
    page=0
    &size=12
    &sort=price,asc          // price,desc | rating,desc | createdAt,desc | name,asc
    &category=smartphones     // category slug
    &brand=Samsung,Apple      // comma-separated brands
    &minPrice=100
    &maxPrice=1000
    &rating=4                 // minimum rating
    &inStock=true
    &tag=premium
```

---

## 5. Caching Strategy

| Data | Cache Key Pattern | TTL | Invalidation |
|------|------------------|-----|--------------|
| Category tree | `categories:tree` | 1 hour | On category CRUD |
| Product listing page | `products:page:{hash}` | 5 min | On product CRUD |
| Product detail | `products:detail:{slug}` | 15 min | On product update |
| Search suggestions | `search:suggest:{query}` | 10 min | On product CRUD |
| Featured products | `products:featured` | 30 min | On feature flag change |

```java
@Cacheable(value = "products", key = "'detail:' + #slug")
public ProductDetailDTO getProductBySlug(String slug) { ... }

@CacheEvict(value = "products", allEntries = true)
public ProductDTO updateProduct(UUID id, UpdateProductDTO dto) { ... }
```

---

## 6. Frontend Components

### 6.1 Home Page Sections
- **Hero Banner** — rotating carousel with promotional images
- **Featured Products** — horizontal scrollable product cards
- **Shop by Category** — grid of category cards with images
- **New Arrivals** — latest products section
- **Best Sellers** — top-selling products section

### 6.2 Product Listing Page
- **Sidebar Filters** — category tree, price slider, brand checkboxes, rating stars
- **Active Filters Bar** — removable filter chips
- **Sort Dropdown** — sort options
- **Product Grid** — responsive grid (4 cols desktop, 2 cols tablet, 1 col mobile)
- **Pagination / Load More** — page controls

### 6.3 Product Detail Page
- **Image Gallery** — thumbnail strip + main image with zoom
- **Product Info** — name, price, rating summary, short description
- **Variant Selector** — clickable chips for color/size
- **Quantity + Add to Cart** — quantity input with +/- buttons
- **Tabs** — Description | Specifications | Reviews
- **Related Products** — carousel of similar items

---

## 7. Performance Considerations

| Concern | Solution |
|---------|----------|
| Large product catalogs | Cursor-based pagination for APIs, virtual scrolling in UI |
| Image loading | Lazy loading, WebP format, CDN for static assets |
| Search performance | DB full-text index (Phase 1), Elasticsearch (Phase 2) |
| Catalog caching | Redis cache with TTL-based invalidation |
| Database queries | JPA EntityGraph to prevent N+1, proper indexing |
| Mobile performance | Responsive images (srcset), reduced payload on mobile |

---

> **Next:** Read [04-PRD-Cart-Checkout.md](./04-PRD-Cart-Checkout.md) for the Shopping Cart & Checkout specification.
