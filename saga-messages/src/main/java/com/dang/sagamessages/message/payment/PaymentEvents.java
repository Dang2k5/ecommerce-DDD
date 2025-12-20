package com.dang.sagamessages.message.payment;

import java.time.Instant;

public final class PaymentEvents {
    private PaymentEvents() {}

    public record PaymentCapturedEvent(
            String sagaId,
            String orderId,
            Instant occurredAt
    ) {}

    public record PaymentCaptureFailedEvent(
            String sagaId,
            String orderId,
            String reason,
            Instant occurredAt
    ) {}

    public record PaymentRefundedEvent(
            String sagaId,
            String orderId,
            Instant occurredAt
    ) {}

    public record PaymentRefundFailedEvent(
            String sagaId,
            String orderId,
            String reason,
            Instant occurredAt
    ) {}
}
