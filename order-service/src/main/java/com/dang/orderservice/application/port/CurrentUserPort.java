package com.dang.orderservice.application.port;

public interface CurrentUserPort {
    String currentUserId();
    boolean isAdmin();
}