package com.dang.productservice.domain.model.valueobjects;

import jakarta.persistence.Access;
import jakarta.persistence.AccessType;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;
import java.util.Objects;

@Embeddable
@Access(AccessType.FIELD)
public class Money implements Serializable {

    @Column(name = "amount", nullable = false, precision = 19, scale = 6)
    private BigDecimal amount;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    protected Money() {
        // for JPA
    }

    private Money(BigDecimal amount, String currency) {
        Currency cur = normalizeAndValidateCurrency(currency);
        this.amount = normalizeAndValidateAmount(amount, cur);
        this.currency = cur.getCurrencyCode();
    }

    public static Money of(BigDecimal amount, String currency) {
        return new Money(amount, currency);
    }

    public static Money zero(String currency) {
        return new Money(BigDecimal.ZERO, currency);
    }

    public BigDecimal amount() {
        return amount;
    }

    public String currency() {
        return currency;
    }

    public Money add(Money other) {
        Money o = requireNonNull(other);
        requireSameCurrency(o);
        return new Money(this.amount.add(o.amount), this.currency);
    }

    public Money subtract(Money other) {
        Money o = requireNonNull(other);
        requireSameCurrency(o);
        return new Money(this.amount.subtract(o.amount), this.currency);
    }

    public Money multiply(int quantity) {
        if (quantity < 0) {
            throw new IllegalArgumentException("Quantity cannot be negative");
        }
        return new Money(this.amount.multiply(BigDecimal.valueOf(quantity)), this.currency);
    }

    public boolean isZero() {
        return amount.signum() == 0;
    }

    public boolean isPositive() {
        return amount.signum() > 0;
    }

    // Nếu domain của bạn CHO PHÉP số âm (ví dụ refund/discount), hãy giữ method này.
    public boolean isNegative() {
        return amount.signum() < 0;
    }

    private static Money requireNonNull(Money other) {
        if (other == null) {
            throw new IllegalArgumentException("Money cannot be null");
        }
        return other;
    }

    private void requireSameCurrency(Money other) {
        if (!this.currency.equals(other.currency)) {
            throw new IllegalArgumentException(
                    "Currency mismatch: " + this.currency + " vs " + other.currency
            );
        }
    }

    private static Currency normalizeAndValidateCurrency(String raw) {
        if (raw == null) {
            throw new IllegalArgumentException("Currency cannot be null");
        }

        String normalized = raw.strip().toUpperCase();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("Currency cannot be empty");
        }

        try {
            return Currency.getInstance(normalized);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid currency code: " + normalized);
        }
    }

    private static BigDecimal normalizeAndValidateAmount(BigDecimal raw, Currency currency) {
        if (raw == null) {
            throw new IllegalArgumentException("Amount cannot be null");
        }

        // Nếu domain bạn KHÔNG cho phép âm, mở comment check này:
        // if (raw.signum() < 0) throw new IllegalArgumentException("Amount cannot be negative");

        int scale = Math.max(currency.getDefaultFractionDigits(), 0);
        return raw.setScale(scale, RoundingMode.HALF_EVEN);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Money other)) return false;
        return Objects.equals(amount, other.amount) &&
                Objects.equals(currency, other.currency);
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
