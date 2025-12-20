package com.dang.inventoryservice.infrastructure.persistence.jpa.impl;

import com.dang.inventoryservice.domain.model.aggregates.InventoryReservation;
import com.dang.inventoryservice.domain.repository.InventoryReservationRepository;
import com.dang.inventoryservice.infrastructure.persistence.jpa.JpaInventoryReservationRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class InventoryReservationRepositoryImpl implements InventoryReservationRepository {

    private final JpaInventoryReservationRepository jpa;

    public InventoryReservationRepositoryImpl(JpaInventoryReservationRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public InventoryReservation save(InventoryReservation reservation) {
        return jpa.save(reservation);
    }

    @Override
    public Optional<InventoryReservation> findBySagaIdAndOrderId(String sagaId, String orderId) {
        return jpa.findBySagaIdAndOrderId(sagaId, orderId);
    }
}
