package com.scaler.harsh.productservice.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ProductServiceException.class)
    public ResponseEntity<String> handleProductServiceException(ProductServiceException ex) {
        return new ResponseEntity<>(ex.getMessage(), ex.getStatus());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<String>
    handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        return new ResponseEntity<>("Invalid ID Format: Must be a number",
                HttpStatus.BAD_REQUEST);
    }
}
