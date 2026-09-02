package com.vinayak.ecommerce.exception;

public class CartNotFoundException extends RuntimeException {

    public CartNotFoundException(String message) {
        super(message);
    }
}