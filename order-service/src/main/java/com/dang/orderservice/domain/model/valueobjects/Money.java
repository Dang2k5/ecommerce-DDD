package com.dang.orderservice.domain.model.valueobjects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;

@Embeddable
public class Money implements Serializable {

    @Column(name = "amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(name = "currency", nullable = false, length = 8)
    private String currency;

    protected Money() {}

    private Money(BigDecimal amount, String currency) {
        if (amount == null) throw new IllegalArgumentException("amount is required");
        if (amount.signum() < 0) throw new IllegalArgumentException("amount must be >= 0");
        if (currency == null || currency.isBlank()) throw new IllegalArgumentException("currency is required");

        this.amount = amount;
        this.currency = currency.strip().toUpperCase();
    }

    public static Money of(BigDecimal amount, String currency) {
        return new Money(amount, currency);
    }

    public static Money zero(String currency) {
        return new Money(BigDecimal.ZERO, currency);
    }

    public Money add(Money other) {
        requireSameCurrency(other);
        return new Money(this.amount.add(other.amount), this.currency);
    }

    public Money multiply(int quantity) {
        if (quantity <= 0) throw new IllegalArgumentException("quantity must be > 0");
        return new Money(this.amount.multiply(BigDecimal.valueOf(quantity)), this.currency);
    }

    private void requireSameCurrency(Money other) {
        if (!this.currency.equals(other.currency)) {
            throw new IllegalArgumentException("Currency mismatch");
        }
    }

    public BigDecimal amount() {
        return amount;
    }

    public String currency() {
        return currency;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Money other)) return false;
        return Objects.equals(amount, other.amount)
                && Objects.equals(currency, other.currency);
    }

    @Override
    public int hashCode() {
        return Objects.hash(amount, currency);
    }

    @Override
    public String toString() {
        return amount + " " + currency;
    }
}
