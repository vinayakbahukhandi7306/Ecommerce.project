package com.vinayak.ecommerce.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import com.vinayak.ecommerce.exception.CartNotFoundException;
import com.vinayak.ecommerce.exception.CartItemNotFoundException;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<String> handleEmailAlreadyExistsException(
            EmailAlreadyExistsException ex) {

        return new ResponseEntity<>(ex.getMessage(), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {

        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        return ResponseEntity.badRequest().body(errors);
    }
    @ExceptionHandler(OrderAccessDeniedException.class)
    public ResponseEntity<String> handleOrderAccessDeniedException(
            OrderAccessDeniedException ex) {

        return new ResponseEntity<>(
                ex.getMessage(),
                HttpStatus.FORBIDDEN
        );
    }
    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<String> handleProductNotFoundException(
            ProductNotFoundException ex) {

        return new ResponseEntity<>(
                ex.getMessage(),
                HttpStatus.NOT_FOUND
        );
    }

    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<String> handleOrderNotFoundException(
            OrderNotFoundException ex) {

        return new ResponseEntity<>(
                ex.getMessage(),
                HttpStatus.NOT_FOUND
        );
    }

    @ExceptionHandler(InvalidQuantityException.class)
    public ResponseEntity<String> handleInvalidQuantityException(
            InvalidQuantityException ex) {

        return new ResponseEntity<>(
                ex.getMessage(),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(InsufficientStockException.class)
    public ResponseEntity<String> handleInsufficientStockException(
            InsufficientStockException ex) {

        return new ResponseEntity<>(
                ex.getMessage(),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(CartNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleCartNotFound(
            CartNotFoundException ex) {

        Map<String, String> response = new HashMap<>();

        response.put("error", "Cart Not Found");
        response.put("message", ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(response);
    }

    @ExceptionHandler(CartItemNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleCartItemNotFound(
            CartItemNotFoundException ex) {

        Map<String, String> response = new HashMap<>();

        response.put("error", "Cart Item Not Found");
        response.put("message", ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(response);
    }
}