package com.dang.sagamessages.message.payment;


import java.math.BigDecimal;
import java.time.Instant;

public final class PaymentCommands {
    private PaymentCommands() {}

    public record CapturePaymentCommand(
            String sagaId,
            String orderId,
            String customerId,
            BigDecimal amount,
            String currency,
            Instant occurredAt
    ) {}

    public record RefundPaymentCommand(
            String sagaId,
            String orderId,
            String reason,
            Instant occurredAt
    ) {}
}

