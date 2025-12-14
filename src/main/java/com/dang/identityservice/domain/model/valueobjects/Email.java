package com.dang.identityservice.domain.model.valueobjects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;
import java.util.regex.Pattern;

@Embeddable
public class Email implements Serializable {
    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    @Column(name = "email", nullable = false, unique = true, length = 120)
    private String value;

    protected Email() {
    }

    private Email(String raw) {
        this.value = normalize(raw);
    }

    public static Email of(String raw) {
        return new Email(raw);
    }

    public String value() {
        return value;
    }

    private static String normalize(String raw) {
        if (raw == null) throw new IllegalArgumentException("Email null");
        String v = raw.strip().toLowerCase();
        if (v.isEmpty()) throw new IllegalArgumentException("Email empty");
        if (!EMAIL.matcher(v).matches()) throw new IllegalArgumentException("Invalid email");
        return v;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Email other && Objects.equals(value, other.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
