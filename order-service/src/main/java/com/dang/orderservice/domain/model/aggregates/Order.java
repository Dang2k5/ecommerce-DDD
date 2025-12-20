package com.dang.orderservice.domain.model.aggregates;

import com.dang.orderservice.domain.model.valueobjects.CustomerId;
import com.dang.orderservice.domain.model.valueobjects.Money;
import com.dang.orderservice.domain.model.valueobjects.OrderId;
import com.dang.orderservice.domain.model.valueobjects.OrderLine;
import com.dang.orderservice.domain.model.valueobjects.OrderStatus;
import com.dang.orderservice.domain.model.valueobjects.ShippingAddress;
import jakarta.persistence.*;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter
public class Order {

    @EmbeddedId
    private OrderId id;

    @Embedded
    private CustomerId customerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    @Embedded
    private ShippingAddress shippingAddress;

    @Embedded
    private Money total;

    @Column(nullable = false)
    private Instant createdAt;

    @ElementCollection
    @CollectionTable(name = "order_lines", joinColumns = @JoinColumn(name = "order_id"))
    private List<OrderLine> lines = new ArrayList<>();

    @Column(nullable = false)
    private boolean inventoryReserved;

    @Column(nullable = false)
    private boolean paid;

    private String cancelReason;

    protected Order() {
    }

    private Order(OrderId id, CustomerId customerId, ShippingAddress shippingAddress, String currency) {
        this.id = id;
        this.customerId = customerId;
        this.shippingAddress = shippingAddress;
        this.status = OrderStatus.PENDING;
        this.createdAt = Instant.now();
        this.total = Money.zero(currency);
        this.inventoryReserved = false;
        this.paid = false;
    }

    public static Order create(OrderId id, CustomerId customerId, ShippingAddress shippingAddress, String currency) {
        if (id == null) throw new IllegalArgumentException("OrderId is required");
        if (customerId == null) throw new IllegalArgumentException("CustomerId is required");
        if (shippingAddress == null) throw new IllegalArgumentException("ShippingAddress is required");
        if (currency == null || currency.isBlank()) throw new IllegalArgumentException("Currency is required");
        return new Order(id, customerId, shippingAddress, currency);
    }

    public void addLine(String sku, int quantity, BigDecimal unitPrice) {
        if (sku == null || sku.isBlank()) throw new IllegalArgumentException("sku is required");
        if (quantity <= 0) throw new IllegalArgumentException("quantity must be > 0");
        if (unitPrice == null) throw new IllegalArgumentException("unitPrice is required");
        if (unitPrice.signum() < 0) throw new IllegalArgumentException("unitPrice must be >= 0");

        this.lines.add(new OrderLine(sku.strip(), quantity, unitPrice));
        // update total (Money is VO)
        Money lineTotal = Money.of(unitPrice, total.currency()).multiply(quantity);
        this.total = this.total.add(lineTotal);
    }

    public Money total() {
        return total;
    }

    // ===== Domain transitions =====
    public void confirm() {
        this.status = OrderStatus.CONFIRMED;
        this.inventoryReserved = true;
        this.paid = true;
    }

    public void cancel(String reason) {
        this.status = OrderStatus.CANCELLED;
        this.cancelReason = reason;
    }

    public void requestCancel(String reason) {
        this.status = OrderStatus.CANCEL_REQUESTED;
        this.cancelReason = reason;
    }

    public void markInventoryReserved() {
        this.inventoryReserved = true;
    }

    public void markPaid() {
        this.paid = true;
    }

}
