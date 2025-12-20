package com.dang.inventoryservice.application.service;

import com.dang.inventoryservice.application.port.OutboxPort;
import com.dang.inventoryservice.domain.model.aggregates.InventoryReservation;
import com.dang.inventoryservice.domain.model.valueobjects.ReservationLine;
import com.dang.inventoryservice.domain.service.InventoryDomainService;
import com.dang.sagamessages.message.inventory.InventoryCommands;
import com.dang.sagamessages.message.inventory.InventoryEvents;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class InventorySagaCommandHandler {

    private final InventoryDomainService domainService;
    private final OutboxPort outbox;

    public InventorySagaCommandHandler(InventoryDomainService domainService, OutboxPort outbox) {
        this.domainService = domainService;
        this.outbox = outbox;
    }

    /**
     * RESERVE:
     * - idempotent theo (sagaId, orderId) nhờ InventoryReservation unique
     * - reserve = trừ kho ngay (vì không có shipping service)
     * - ghi outbox event (success/fail)
     */
    @Transactional
    public void handleReserve(InventoryCommands.ReserveInventoryCommand cmd) {
        var lines = cmd.items().stream()
                .map(i -> ReservationLine.of(i.sku(), i.quantity()))
                .toList();

        InventoryReservation reservation = domainService.reserve(cmd.sagaId(), cmd.orderId(), lines);

        if (reservation.isFailed()) {
            outbox.add("Inventory", cmd.orderId(), "InventoryReserveFailedEvent",
                    new InventoryEvents.InventoryReserveFailedEvent(
                            cmd.sagaId(), cmd.orderId(),
                            reservation.getFailureReason(),
                            Instant.now()
                    )
            );
            return;
        }

        outbox.add("Inventory", cmd.orderId(), "InventoryReservedEvent",
                new InventoryEvents.InventoryReservedEvent(cmd.sagaId(), cmd.orderId(), Instant.now())
        );
    }

    /**
     * RELEASE (compensation):
     * - nếu không có reservation => compensate OK để saga không kẹt
     * - ghi outbox InventoryReleasedEvent
     */
    @Transactional
    public void handleRelease(InventoryCommands.ReleaseInventoryCommand cmd) {
        try {
            domainService.release(cmd.sagaId(), cmd.orderId());

            outbox.add("Inventory", cmd.orderId(), "InventoryReleasedEvent",
                    new InventoryEvents.InventoryReleasedEvent(cmd.sagaId(), cmd.orderId(), Instant.now())
            );
        } catch (Exception ex) {
            String reason = (ex.getMessage() == null || ex.getMessage().isBlank())
                    ? "Inventory release failed"
                    : ex.getMessage();

            outbox.add("Inventory", cmd.orderId(), "InventoryReleaseFailedEvent",
                    new InventoryEvents.InventoryReleaseFailedEvent(cmd.sagaId(), cmd.orderId(), reason, Instant.now())
            );
        }
    }
}
