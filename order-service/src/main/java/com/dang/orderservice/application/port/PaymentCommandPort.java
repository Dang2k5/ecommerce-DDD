package com.dang.orderservice.application.port;

import com.dang.sagamessages.message.payment.PaymentCommands;

public interface PaymentCommandPort {
    void sendCapturePayment(PaymentCommands.CapturePaymentCommand command);
    void sendRefundPayment(PaymentCommands.RefundPaymentCommand command);
}