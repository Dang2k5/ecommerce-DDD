package com.dang.sagamessages.message.inventory;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public final class InventoryCommands {
    private InventoryCommands() {}

    public record ReserveInventoryCommand(
            String sagaId,
            String orderId,
            String customerId,
            List<Item> items,
            Instant occurredAt
    ) {
        public record Item(String sku, int quantity, BigDecimal unitPrice) {}
    }

    public record ReleaseInventoryCommand(
            String sagaId,
            String orderId,
            String reason,
            Instant occurredAt
    ) {}
}
