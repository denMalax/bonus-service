package com.bonus.exception;

import lombok.Getter;

@Getter
public class NotFoundException extends RuntimeException {

    private final String resourceType;
    private final String identifier;

    public NotFoundException(String message) {
        super(message);
        this.resourceType = null;
        this.identifier = null;
    }

    public NotFoundException(String resourceType, String identifier) {
        super(String.format("%s с идентификатором '%s' не найден", resourceType, identifier));
        this.resourceType = resourceType;
        this.identifier = identifier;
    }

    public NotFoundException(String resourceType, Long id) {
        super(String.format("%s с ID %d не найден", resourceType, id));
        this.resourceType = resourceType;
        this.identifier = String.valueOf(id);
    }
}