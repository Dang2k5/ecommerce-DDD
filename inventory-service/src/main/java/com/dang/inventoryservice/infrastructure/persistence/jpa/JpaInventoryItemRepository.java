package com.dang.inventoryservice.infrastructure.persistence.jpa;

import com.dang.inventoryservice.domain.model.aggregates.InventoryItem;
import com.dang.inventoryservice.domain.model.valueobjects.Sku;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaInventoryItemRepository extends JpaRepository<InventoryItem, Sku> {
}
