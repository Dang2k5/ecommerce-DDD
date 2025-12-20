package com.dang.inventoryservice.domain.repository;

import com.dang.inventoryservice.domain.model.aggregates.InventoryItem;
import com.dang.inventoryservice.domain.model.exception.NotFoundException;
import com.dang.inventoryservice.domain.model.valueobjects.Sku;

import java.util.List;
import java.util.Optional;

public interface InventoryItemRepository {
    InventoryItem save(InventoryItem item);

    Optional<InventoryItem> findById(Sku sku);

    List<InventoryItem> findAll();

    default InventoryItem getRequired(Sku sku) {
        return findById(sku).orElseThrow(() -> new NotFoundException("InventoryItem not found: " + sku.value()));
    }

}
