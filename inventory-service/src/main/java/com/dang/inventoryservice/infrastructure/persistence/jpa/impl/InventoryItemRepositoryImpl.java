package com.dang.inventoryservice.infrastructure.persistence.jpa.impl;

import com.dang.inventoryservice.domain.model.aggregates.InventoryItem;
import com.dang.inventoryservice.domain.model.valueobjects.Sku;
import com.dang.inventoryservice.domain.repository.InventoryItemRepository;
import com.dang.inventoryservice.infrastructure.persistence.jpa.JpaInventoryItemRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class InventoryItemRepositoryImpl implements InventoryItemRepository {

    private final JpaInventoryItemRepository jpa;

    public InventoryItemRepositoryImpl(JpaInventoryItemRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public InventoryItem save(InventoryItem item) {
        return jpa.save(item);
    }

    @Override
    public Optional<InventoryItem> findById(Sku sku) {
        return jpa.findById(sku);
    }

    @Override
    public List<InventoryItem> findAll() {
        return jpa.findAll();
    }
}
