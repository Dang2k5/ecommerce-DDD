package com.dang.inventoryservice.application.port;

public interface OutboxPort {
    void add(String aggregateType, String aggregateId, String eventType, Object payload);
}
