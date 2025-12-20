package com.dang.customerservice.domain.model.valueobjects;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
@Access(AccessType.FIELD)
public class UserId implements Serializable {
    @Column(name = "user_id", nullable = false, length = 36, updatable = false)
    private String value;

    protected UserId() {}

    private UserId(String raw) {
        this.value = CustomerId.normalizeUuid(raw, "User ID");
    }

    public static UserId of(String raw) { return new UserId(raw); }
    public String value() { return value; }

    @Override public boolean equals(Object o){ return (o instanceof UserId other) && Objects.equals(value, other.value); }
    @Override public int hashCode(){ return Objects.hash(value); }
    @Override public String toString(){ return value; }
}
