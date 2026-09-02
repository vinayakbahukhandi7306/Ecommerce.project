package com.vinayak.ecommerce.service;

import com.vinayak.ecommerce.entity.Product;

import java.util.List;

public interface ProductService {

    Product createProduct(Product product);

    List<Product> getAllProducts();

    Product getProductById(Long productId);

    Product updateProduct(Long productId, Product product);

    void deleteProduct(Long productId);
}