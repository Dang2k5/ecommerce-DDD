package com.dang.paymentservice.interfaces;

import com.dang.paymentservice.application.service.PaymentQueryApplicationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/payments")
public class PaymentQueryController {

    private final PaymentQueryApplicationService paymentQueryApplicationService;

    public PaymentQueryController(PaymentQueryApplicationService paymentQueryApplicationService) {
        this.paymentQueryApplicationService = paymentQueryApplicationService;
    }

    @GetMapping("/by-order/{orderId}")
    public ResponseEntity<?> byOrder(@PathVariable String orderId) {
        return paymentQueryApplicationService.findByOrderId(orderId)
                .map(p -> ResponseEntity.ok(Map.of(
                        "paymentId", p.getId().value(),
                        "orderId", p.getOrderId().value(),
                        "amount", p.getAmount().amount(),
                        "currency", p.getAmount().currency(),
                        "status", p.getStatus().name(),
                        "createdAt", p.getCreatedAt().toString()
                )))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/{paymentId}")
    public ResponseEntity<?> byId(@PathVariable String paymentId) {
        return paymentQueryApplicationService.findById(paymentId)
                .map(p -> ResponseEntity.ok(Map.of(
                        "paymentId", p.getId().value(),
                        "orderId", p.getOrderId().value(),
                        "amount", p.getAmount().amount(),
                        "currency", p.getAmount().currency(),
                        "status", p.getStatus().name(),
                        "createdAt", p.getCreatedAt().toString()
                )))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
