package com.lorenzo.imobiliaria_api.exception;

public record FieldErrorResponse(
        String field,
        String message
) {
}
