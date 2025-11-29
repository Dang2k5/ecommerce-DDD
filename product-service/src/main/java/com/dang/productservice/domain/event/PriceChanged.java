package com.dang.productservice.domain.event;

public record PriceChanged(String productId, long newPrice) {
}
