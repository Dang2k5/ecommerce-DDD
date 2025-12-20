package com.dang.paymentservice.application.port;

public interface CurrentUserPort {
    String currentUserId();
    boolean isAdmin();
}