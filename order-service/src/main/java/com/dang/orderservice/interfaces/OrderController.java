package com.dang.orderservice.interfaces;

import com.dang.orderservice.application.commands.CancelOrderCommand;
import com.dang.orderservice.application.commands.CreateOrderCommand;
import com.dang.orderservice.application.dtos.OrderResponse;
import com.dang.orderservice.application.service.OrderApplicationService;
import com.dang.orderservice.application.saga.OrderSagaOrchestrator;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderApplicationService orderApp;
    private final OrderSagaOrchestrator saga;

    public OrderController(OrderApplicationService orderApp, OrderSagaOrchestrator saga) {
        this.orderApp = orderApp;
        this.saga = saga;
    }

    @PostMapping
    public ResponseEntity<OrderResponse> create(@RequestBody @Valid CreateOrderCommand command) {
        OrderResponse response = orderApp.create(command);
        // create saga start theo orderId
        saga.startCreateOrderSaga(response.orderId());
        return ResponseEntity.created(URI.create("/api/orders/" + response.orderId())).body(response);
    }

    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<OrderResponse> cancel(@PathVariable String orderId,
                                                @RequestBody @Valid CancelOrderCommand command) {
        OrderResponse response = orderApp.requestCancel(orderId, command);

        // Chỉ start cancel saga nếu thật sự đang CANCEL_REQUESTED
        if ("CANCEL_REQUESTED".equalsIgnoreCase(response.status())) {
            saga.startCancelOrderSaga(orderId);
        }

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> get(@PathVariable String orderId) {
        return ResponseEntity.ok(orderApp.getOrder(orderId));
    }
}
