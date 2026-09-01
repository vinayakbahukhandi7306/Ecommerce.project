package com.vinayak.ecommerce.controller;

import com.vinayak.ecommerce.entity.Product;
import com.vinayak.ecommerce.repository.ProductRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductRepository productRepository;

    public ProductController(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @PostMapping
    public ResponseEntity<Product> createProduct(
            @RequestBody Product product) {

        Product savedProduct = productRepository.save(product);

        return new ResponseEntity<>(
                savedProduct,
                HttpStatus.CREATED
        );
    }

    @GetMapping
    public ResponseEntity<List<Product>> getProducts() {

        return ResponseEntity.ok(
                productRepository.findAll()
        );
    }
}