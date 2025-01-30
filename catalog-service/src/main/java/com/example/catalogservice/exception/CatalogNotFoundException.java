package com.example.catalogservice.exception;

public class CatalogNotFoundException extends RuntimeException{
    public CatalogNotFoundException(String message) {
        super(message);
    }
}
