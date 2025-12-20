package com.dang.inventoryservice.interfaces;

import com.dang.inventoryservice.application.commands.StockInRequest;
import com.dang.inventoryservice.application.commands.StockOutRequest;
import com.dang.inventoryservice.application.service.InventoryAdminApplicationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/inventory/admin")
@Validated
public class InventoryAdminController {

    private final InventoryAdminApplicationService service;

    public InventoryAdminController(InventoryAdminApplicationService service) {
        this.service = service;
    }

    @PostMapping("/stock-in")
    public ResponseEntity<?> stockIn(@RequestBody @Valid StockInRequest req) {
        return ResponseEntity.ok(service.stockIn(req.sku(), req.qty()));
    }

    @PostMapping("/stock-out")
    public ResponseEntity<?> stockOut(@RequestBody @Valid StockOutRequest req) {
        return ResponseEntity.ok(service.stockOut(req.sku(), req.qty()));
    }

    @GetMapping("/stock/{sku}")
    public ResponseEntity<?> getStock(@PathVariable String sku) {
        return ResponseEntity.ok(service.getStock(sku));
    }
}
