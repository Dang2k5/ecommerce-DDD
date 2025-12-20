package com.dang.inventoryservice.infrastructure.persistence.jpa;

import com.dang.inventoryservice.domain.model.aggregates.InventoryReservation;
import com.dang.inventoryservice.domain.model.valueobjects.ReservationId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface JpaInventoryReservationRepository extends JpaRepository<InventoryReservation, ReservationId> {
    Optional<InventoryReservation> findBySagaIdAndOrderId(String sagaId, String orderId);
}
