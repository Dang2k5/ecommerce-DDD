package com.dang.customerservice.domain.model.valueobjects;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
@Access(AccessType.FIELD)
public class PhoneNumber implements Serializable {
    @Column(name = "phone", length = 32)
    private String value;

    protected PhoneNumber() {}

    private PhoneNumber(String raw) {
        this.value = normalize(raw);
    }

    public static PhoneNumber ofNullable(String raw) {
        if (raw == null) return null;
        String v = raw.strip();
        return v.isEmpty() ? null : new PhoneNumber(v);
    }

    public String value() { return value; }

    private static String normalize(String raw) {
        String v = raw.strip();
        if (v.isEmpty()) throw new IllegalArgumentException("Phone cannot be empty");
        // very practical rule: digits + optional leading '+', length 8..20
        String t = v.startsWith("+") ? v.substring(1) : v;
        if (!t.chars().allMatch(Character::isDigit)) throw new IllegalArgumentException("Invalid phone: " + raw);
        if (t.length() < 8 || t.length() > 20) throw new IllegalArgumentException("Invalid phone length: " + raw);
        return v;
    }

    @Override public boolean equals(Object o){ return (o instanceof PhoneNumber other) && Objects.equals(value, other.value); }
    @Override public int hashCode(){ return Objects.hash(value); }
    @Override public String toString(){ return value; }
}
