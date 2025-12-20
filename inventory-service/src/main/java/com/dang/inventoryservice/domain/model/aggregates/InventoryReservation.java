package com.dang.inventoryservice.domain.model.aggregates;

import com.dang.inventoryservice.domain.model.valueobjects.ReservationId;
import com.dang.inventoryservice.domain.model.valueobjects.ReservationLine;
import com.dang.inventoryservice.domain.model.valueobjects.ReservationStatus;
import jakarta.persistence.*;
import lombok.Getter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "inventory_reservations",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_reservation_saga_order", columnNames = {"saga_id", "order_id"})
        }
)
@Getter
public class InventoryReservation {

    @EmbeddedId
    private ReservationId id;

    @Column(name = "saga_id", nullable = false, length = 64)
    private String sagaId;

    @Column(name = "order_id", nullable = false, length = 64)
    private String orderId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReservationStatus status;

    @Column(nullable = false)
    private Instant createdAt;

    @ElementCollection
    @CollectionTable(name = "inventory_reservation_lines", joinColumns = @JoinColumn(name = "reservation_id"))
    private List<ReservationLine> lines = new ArrayList<>();

    @Column(length = 500)
    private String failureReason;

    protected InventoryReservation() {
    }

    private InventoryReservation(String sagaId, String orderId, List<ReservationLine> lines) {
        this.id = ReservationId.of(java.util.UUID.randomUUID().toString());
        this.sagaId = sagaId;
        this.orderId = orderId;
        this.status = ReservationStatus.RESERVED;
        this.createdAt = Instant.now();
        this.lines = new ArrayList<>(lines);
    }

    public static InventoryReservation reserved(String sagaId, String orderId, List<ReservationLine> lines) {
        return new InventoryReservation(sagaId, orderId, lines);
    }

    public void markReleased() {
        this.status = ReservationStatus.RELEASED;
    }

    public void markFailed(String reason) {
        this.status = ReservationStatus.FAILED;
        this.failureReason = reason;
    }

    public boolean isReserved() {
        return status == ReservationStatus.RESERVED;
    }

    public boolean isReleased() {
        return status == ReservationStatus.RELEASED;
    }

    public boolean isFailed() {
        return status == ReservationStatus.FAILED;
    }
}
