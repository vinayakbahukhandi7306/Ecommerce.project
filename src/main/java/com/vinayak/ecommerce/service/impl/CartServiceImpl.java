package com.vinayak.ecommerce.service.impl;

import com.vinayak.ecommerce.dto.CartItemResponse;
import com.vinayak.ecommerce.dto.CartResponse;
import com.vinayak.ecommerce.entity.Cart;
import com.vinayak.ecommerce.entity.CartItem;
import com.vinayak.ecommerce.entity.Product;
import com.vinayak.ecommerce.entity.User;
import com.vinayak.ecommerce.exception.CartItemNotFoundException;
import com.vinayak.ecommerce.exception.CartNotFoundException;
import com.vinayak.ecommerce.exception.InsufficientStockException;
import com.vinayak.ecommerce.exception.InvalidQuantityException;
import com.vinayak.ecommerce.exception.ProductNotFoundException;
import com.vinayak.ecommerce.repository.CartItemRepository;
import com.vinayak.ecommerce.repository.CartRepository;
import com.vinayak.ecommerce.repository.ProductRepository;
import com.vinayak.ecommerce.security.SecurityService;
import com.vinayak.ecommerce.service.CartService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final SecurityService securityService;

    public CartServiceImpl(
            CartRepository cartRepository,
            CartItemRepository cartItemRepository,
            ProductRepository productRepository,
            SecurityService securityService) {

        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
        this.securityService = securityService;
    }

    @Override
    @Transactional
    public CartResponse getMyCart() {

        User currentUser = securityService.getCurrentUser();

        Cart cart = cartRepository.findByUser(currentUser)
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setUser(currentUser);
                    return cartRepository.save(newCart);
                });

        return convertToResponse(cart);
    }

    @Override
    @Transactional
    public CartResponse addToCart(Long productId, Integer quantity) {

        User currentUser = securityService.getCurrentUser();

        Product product = productRepository.findById(productId)
                .orElseThrow(() ->
                        new ProductNotFoundException("Product not found"));

        if (quantity == null || quantity <= 0) {
            throw new InvalidQuantityException(
                    "Quantity must be greater than zero");
        }

        if (product.getStock() < quantity) {
            throw new InsufficientStockException(
                    "Insufficient stock");
        }

        Cart cart = cartRepository.findByUser(currentUser)
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setUser(currentUser);
                    return cartRepository.save(newCart);
                });

        CartItem cartItem = cartItemRepository
                .findByCartAndProduct(cart, product)
                .orElse(null);

        if (cartItem != null) {

            int newQuantity =
                    cartItem.getQuantity() + quantity;

            if (newQuantity > product.getStock()) {
                throw new InsufficientStockException(
                        "Insufficient stock");
            }

            cartItem.setQuantity(newQuantity);

        } else {

            cartItem = new CartItem();
            cartItem.setCart(cart);
            cartItem.setProduct(product);
            cartItem.setQuantity(quantity);
        }

        cartItemRepository.save(cartItem);

        return convertToResponse(cart);
    }

    @Override
    @Transactional
    public CartResponse updateCartItem(
            Long productId,
            Integer quantity) {

        User currentUser = securityService.getCurrentUser();

        if (quantity == null || quantity <= 0) {
            throw new InvalidQuantityException(
                    "Quantity must be greater than zero");
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() ->
                        new ProductNotFoundException(
                                "Product not found"));

        Cart cart = cartRepository.findByUser(currentUser)
                .orElseThrow(() ->
                        new CartNotFoundException(
                                "Cart not found"));

        CartItem cartItem = cartItemRepository
                .findByCartAndProduct(cart, product)
                .orElseThrow(() ->
                        new CartItemNotFoundException(
                                "Product not in cart"));

        if (quantity > product.getStock()) {
            throw new InsufficientStockException(
                    "Insufficient stock");
        }

        cartItem.setQuantity(quantity);

        cartItemRepository.save(cartItem);

        return convertToResponse(cart);
    }

    @Override
    @Transactional
    public void removeFromCart(Long productId) {

        User currentUser = securityService.getCurrentUser();

        Product product = productRepository.findById(productId)
                .orElseThrow(() ->
                        new ProductNotFoundException(
                                "Product not found"));

        Cart cart = cartRepository.findByUser(currentUser)
                .orElseThrow(() ->
                        new CartNotFoundException(
                                "Cart not found"));

        CartItem cartItem = cartItemRepository
                .findByCartAndProduct(cart, product)
                .orElseThrow(() ->
                        new CartItemNotFoundException(
                                "Product not in cart"));

        cartItemRepository.delete(cartItem);
    }

    @Override
    @Transactional
    public void clearCart() {

        User currentUser = securityService.getCurrentUser();

        Cart cart = cartRepository.findByUser(currentUser)
                .orElseThrow(() ->
                        new CartNotFoundException(
                                "Cart not found"));

        cart.getItems().clear();

        cartRepository.save(cart);
    }

    private CartResponse convertToResponse(Cart cart) {

        List<CartItemResponse> items = cart.getItems()
                .stream()
                .map(item -> new CartItemResponse(
                        item.getProduct().getId(),
                        item.getProduct().getName(),
                        item.getProduct().getPrice(),
                        item.getQuantity()
                ))
                .toList();

        return new CartResponse(
                cart.getId(),
                items
        );
    }
}