package com.dang.inventoryservice.application.service;

import com.dang.inventoryservice.domain.model.aggregates.InventoryItem;
import com.dang.inventoryservice.domain.model.valueobjects.Sku;
import com.dang.inventoryservice.domain.repository.InventoryItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InventoryAdminApplicationService {

    private final InventoryItemRepository itemRepo;

    public InventoryAdminApplicationService(InventoryItemRepository itemRepo) {
        this.itemRepo = itemRepo;
    }


    @Transactional
    public InventoryItem stockIn(String sku, int qty) {
        var vo = Sku.of(sku);
        var item = itemRepo.findById(vo).orElseGet(() -> InventoryItem.create(sku, 0));
        item.stockIn(qty);
        return itemRepo.save(item);
    }

    @Transactional
    public InventoryItem stockOut(String sku, int qty) {
        var item = itemRepo.getRequired(Sku.of(sku));
        item.stockOut(qty);
        return itemRepo.save(item);
    }

    @Transactional(readOnly = true)
    public InventoryItem getStock(String sku) {
        return itemRepo.getRequired(Sku.of(sku));
    }

}
