package com.dang.inventoryservice.domain.repository;

import com.dang.inventoryservice.domain.model.aggregates.InventoryReservation;

import java.util.Optional;

public interface InventoryReservationRepository {
    InventoryReservation save(InventoryReservation reservation);

    Optional<InventoryReservation> findBySagaIdAndOrderId(String sagaId, String orderId);
}
