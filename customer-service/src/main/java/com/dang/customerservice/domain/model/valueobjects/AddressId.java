package com.dang.customerservice.domain.model.valueobjects;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
@Access(AccessType.FIELD)
public class AddressId implements Serializable {
    @Column(name = "address_id", nullable = false, length = 36, updatable = false)
    private String value;

    protected AddressId() {}

    private AddressId(String raw) {
        this.value = CustomerId.normalizeUuid(raw, "Address ID");
    }

    public static AddressId generate() { return new AddressId(UUID.randomUUID().toString()); }
    public static AddressId of(String raw) { return new AddressId(raw); }
    public String value() { return value; }

    @Override public boolean equals(Object o){ return (o instanceof AddressId other) && Objects.equals(value, other.value); }
    @Override public int hashCode(){ return Objects.hash(value); }
    @Override public String toString(){ return value; }
}