package com.dang.productservice.domain.event;

public record ProductCreated(String productId, String name, long price) {
}
