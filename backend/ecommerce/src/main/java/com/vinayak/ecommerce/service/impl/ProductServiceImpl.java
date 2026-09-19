package com.vinayak.ecommerce.service.impl;

import com.vinayak.ecommerce.entity.Product;
import com.vinayak.ecommerce.exception.ProductNotFoundException;
import com.vinayak.ecommerce.repository.OrderRepository;
import com.vinayak.ecommerce.repository.ProductRepository;
import com.vinayak.ecommerce.service.ProductService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;

    public ProductServiceImpl(
            ProductRepository productRepository,
            OrderRepository orderRepository
    ) {
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
    }

    @CacheEvict(value = "products", allEntries = true)
    @Override
    public Product createProduct(Product product) {
        return productRepository.save(product);
    }

    @Cacheable("products")
    @Override
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    @Override
    public Product getProductById(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() ->
                        new ProductNotFoundException("Product not found"));
    }

    @CacheEvict(value = "products", allEntries = true)
    @Override
    public Product updateProduct(Long productId, Product product) {

        Product existingProduct = productRepository.findById(productId)
                .orElseThrow(() ->
                        new ProductNotFoundException("Product not found"));

        existingProduct.setName(product.getName());
        existingProduct.setPrice(product.getPrice());
        existingProduct.setStock(product.getStock());

        return productRepository.save(existingProduct);
    }

    @CacheEvict(value = "products", allEntries = true)
    @Override
    public void deleteProduct(Long productId) {

        Product existingProduct = productRepository.findById(productId)
                .orElseThrow(() ->
                        new ProductNotFoundException("Product not found"));

        if (orderRepository.existsByProduct(existingProduct)) {
            throw new IllegalStateException(
                    "Product cannot be deleted because it is associated with an existing order"
            );
        }

        productRepository.delete(existingProduct);
    }
}