package com.dang.sagamessages.message.inventory;

import java.time.Instant;

public final class InventoryEvents {
    private InventoryEvents() {}

    public record InventoryReservedEvent(
            String sagaId,
            String orderId,
            Instant occurredAt
    ) {}

    public record InventoryReserveFailedEvent(
            String sagaId,
            String orderId,
            String reason,
            Instant occurredAt
    ) {}

    public record InventoryReleasedEvent(
            String sagaId,
            String orderId,
            Instant occurredAt
    ) {}

    public record InventoryReleaseFailedEvent(
            String sagaId,
            String orderId,
            String reason,
            Instant occurredAt
    ) {}
}
