package com.vinayak.ecommerce.controller;

import com.vinayak.ecommerce.entity.Product;
import com.vinayak.ecommerce.service.ProductService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    public ResponseEntity<Product> createProduct(
            @RequestBody Product product) {

        Product savedProduct = productService.createProduct(product);

        return new ResponseEntity<>(
                savedProduct,
                HttpStatus.CREATED
        );
    }

    @GetMapping
    public ResponseEntity<List<Product>> getProducts() {

        return ResponseEntity.ok(
                productService.getAllProducts()
        );
    }

    @GetMapping("/{productId}")
    public ResponseEntity<Product> getProductById(
            @PathVariable Long productId) {

        return ResponseEntity.ok(
                productService.getProductById(productId)
        );
    }

    @PutMapping("/{productId}")
    public ResponseEntity<Product> updateProduct(
            @PathVariable Long productId,
            @RequestBody Product product) {

        return ResponseEntity.ok(
                productService.updateProduct(productId, product)
        );
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> deleteProduct(
            @PathVariable Long productId) {

        productService.deleteProduct(productId);

        return ResponseEntity.noContent().build();
    }
}