package com.dang.orderservice.application.service;

import com.dang.orderservice.application.commands.CancelOrderCommand;
import com.dang.orderservice.application.commands.CreateOrderCommand;
import com.dang.orderservice.application.dtos.OrderResponse;
import com.dang.orderservice.application.exceptions.BadRequestException;
import com.dang.orderservice.application.port.CurrentUserPort;
import com.dang.orderservice.domain.model.aggregates.Order;
import com.dang.orderservice.domain.model.valueobjects.*;
import com.dang.orderservice.domain.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderApplicationService {

    private final OrderRepository orderRepository;
    private final CurrentUserPort currentUser;

    public OrderApplicationService(OrderRepository orderRepository, CurrentUserPort currentUser) {
        this.orderRepository = orderRepository;
        this.currentUser = currentUser;
    }

    @Transactional
    public OrderResponse create(CreateOrderCommand command) {
        var sa = command.shippingAddress();

        Order order = Order.create(
                OrderId.generate(),
                CustomerId.of(currentUser.currentUserId()),
                ShippingAddress.of(
                        sa.fullName(),
                        sa.phone(),
                        sa.line1(),
                        sa.line2(),
                        sa.city(),
                        sa.state(),
                        sa.postalCode(),
                        sa.country()
                ),
                command.currency()
        );

        for (var i : command.items()) {
            order.addLine(i.sku(), i.quantity(), i.unitPrice());
        }

        orderRepository.save(order);
        return OrderResponseMapper.toResponse(order);
    }

    @Transactional
    public OrderResponse requestCancel(String orderIdRaw, CancelOrderCommand command) {
        Order order = orderRepository.getRequired(OrderId.of(orderIdRaw));

        // authZ: admin hoặc đúng chủ đơn
        if (!currentUser.isAdmin()) {
            CustomerId currentCustomer = CustomerId.of(currentUser.currentUserId());
            if (!order.getCustomerId().equals(currentCustomer)) {
                throw new BadRequestException("You are not allowed to cancel this order");
            }
        }

        // business rule: không cho cancel lặp
        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new BadRequestException("Order already cancelled");
        }
        if (order.getStatus() == OrderStatus.CANCEL_REQUESTED) {
            // idempotent: trả lại trạng thái hiện tại, không tạo thêm saga
            return OrderResponseMapper.toResponse(order);
        }

        // chỉ cho phép cancel khi PENDING hoặc CONFIRMED (tuỳ policy, bạn có thể siết chặt hơn)
        if (order.getStatus() != OrderStatus.PENDING && order.getStatus() != OrderStatus.CONFIRMED) {
            throw new BadRequestException("Cannot cancel order in status: " + order.getStatus());
        }

        order.requestCancel(command.reason());
        orderRepository.save(order);
        return OrderResponseMapper.toResponse(order);
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrder(String orderIdRaw) {
        Order order = orderRepository.getRequired(OrderId.of(orderIdRaw));

        if (!currentUser.isAdmin()) {
            CustomerId currentCustomer = CustomerId.of(currentUser.currentUserId());
            if (!order.getCustomerId().equals(currentCustomer)) {
                throw new BadRequestException("You are not allowed to view this order");
            }
        }

        return OrderResponseMapper.toResponse(order);
    }
}
