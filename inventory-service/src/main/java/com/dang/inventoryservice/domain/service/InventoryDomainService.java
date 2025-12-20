package com.dang.inventoryservice.domain.service;

import com.dang.inventoryservice.domain.model.aggregates.InventoryItem;
import com.dang.inventoryservice.domain.model.aggregates.InventoryReservation;
import com.dang.inventoryservice.domain.model.valueobjects.ReservationLine;
import com.dang.inventoryservice.domain.model.valueobjects.Sku;
import com.dang.inventoryservice.domain.repository.InventoryItemRepository;
import com.dang.inventoryservice.domain.repository.InventoryReservationRepository;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class InventoryDomainService {

    private final InventoryItemRepository itemRepo;
    private final InventoryReservationRepository reservationRepo;

    public InventoryDomainService(InventoryItemRepository itemRepo, InventoryReservationRepository reservationRepo) {
        this.itemRepo = itemRepo;
        this.reservationRepo = reservationRepo;
    }

    /**
     * Reserve = trừ kho ngay (đơn giản vì không có shipping service).
     * Idempotent theo (sagaId, orderId).
     */
    public InventoryReservation reserve(String sagaId, String orderId, List<ReservationLine> lines) {
        var existingOpt = reservationRepo.findBySagaIdAndOrderId(sagaId, orderId);
        if (existingOpt.isPresent()) {
            return existingOpt.get(); // idempotent
        }

        // Validate đủ tồn cho tất cả lines
        for (var line : lines) {
            InventoryItem item = itemRepo.getRequired(Sku.of(line.getSku()));
            if (item.getAvailableQty() < line.getQuantity()) {
                InventoryReservation failed = InventoryReservation.reserved(sagaId, orderId, lines);
                failed.markFailed("Not enough stock for sku=" + line.getSku());
                return reservationRepo.save(failed);
            }
        }

        // Apply deduct stock
        for (var line : lines) {
            InventoryItem item = itemRepo.getRequired(Sku.of(line.getSku()));
            item.stockOut(line.getQuantity());
            itemRepo.save(item);
        }

        InventoryReservation reservation = InventoryReservation.reserved(sagaId, orderId, lines);
        return reservationRepo.save(reservation);
    }

    /**
     * Release = cộng kho lại theo reservation lines.
     * Idempotent: nếu đã RELEASED thì no-op.
     */
    public InventoryReservation release(String sagaId, String orderId) {
        var existingOpt = reservationRepo.findBySagaIdAndOrderId(sagaId, orderId);
        if (existingOpt.isEmpty()) {
            // Không có reservation thì coi như đã release (để saga không bị kẹt).
            InventoryReservation synthetic = InventoryReservation.reserved(sagaId, orderId, List.of());
            synthetic.markReleased();
            return reservationRepo.save(synthetic);
        }

        InventoryReservation reservation = existingOpt.get();

        if (reservation.isReleased()) return reservation;
        if (reservation.isFailed()) {
            // Reserve đã fail thì không có gì để release => coi như released
            reservation.markReleased();
            return reservationRepo.save(reservation);
        }

        // cộng kho lại
        for (var line : reservation.getLines()) {
            InventoryItem item = itemRepo.getRequired(Sku.of(line.getSku()));
            item.stockIn(line.getQuantity());
            itemRepo.save(item);
        }

        reservation.markReleased();
        return reservationRepo.save(reservation);
    }
}
