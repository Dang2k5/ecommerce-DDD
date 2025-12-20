package com.dang.customerservice.domain.model.valueobjects;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

@Embeddable
@Access(AccessType.FIELD)
public class Email implements Serializable {
    private static final Pattern EMAIL =
            Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$", Pattern.CASE_INSENSITIVE);

    @Column(name = "email", nullable = false, length = 254)
    private String value;

    protected Email() {}

    private Email(String raw) {
        this.value = normalize(raw);
    }

    public static Email of(String raw) { return new Email(raw); }
    public String value() { return value; }

    private static String normalize(String raw) {
        if (raw == null) throw new IllegalArgumentException("Email cannot be null");
        String v = raw.strip().toLowerCase(Locale.ROOT);
        if (v.isEmpty()) throw new IllegalArgumentException("Email cannot be empty");
        if (!EMAIL.matcher(v).matches()) throw new IllegalArgumentException("Invalid email: " + raw);
        return v;
    }

    @Override public boolean equals(Object o){ return (o instanceof Email other) && Objects.equals(value, other.value); }
    @Override public int hashCode(){ return Objects.hash(value); }
    @Override public String toString(){ return value; }
}