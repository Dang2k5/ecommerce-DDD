package com.dang.orderservice.infrastructure.persistence.jpa;

import com.dang.orderservice.domain.model.aggregates.Order;
import com.dang.orderservice.domain.model.valueobjects.OrderId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaOrderRepository extends JpaRepository<Order, OrderId> {
}
