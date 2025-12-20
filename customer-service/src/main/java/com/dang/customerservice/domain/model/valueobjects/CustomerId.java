package com.dang.customerservice.domain.model.valueobjects;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
@Access(AccessType.FIELD)
public class CustomerId implements Serializable {
    @Column(name = "customer_id", nullable = false, length = 36, updatable = false)
    private String value;

    protected CustomerId() {}

    private CustomerId(String raw) {
        this.value = normalizeUuid(raw, "Customer ID");
    }

    public static CustomerId generate() { return new CustomerId(UUID.randomUUID().toString()); }
    public static CustomerId of(String raw) { return new CustomerId(raw); }
    public String value() { return value; }

    static String normalizeUuid(String raw, String label) {
        if (raw == null) throw new IllegalArgumentException(label + " cannot be null");
        String v = raw.strip();
        if (v.isEmpty()) throw new IllegalArgumentException(label + " cannot be empty");
        return v;
    }

    @Override public boolean equals(Object o){ return (o instanceof CustomerId other) && Objects.equals(value, other.value); }
    @Override public int hashCode(){ return Objects.hash(value); }
    @Override public String toString(){ return value; }
}
