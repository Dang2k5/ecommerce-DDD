package com.dang.customerservice.domain.model.valueobjects;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
@Access(AccessType.FIELD)
public class CustomerProfile implements Serializable {

    @Column(name = "full_name", nullable = false, length = 200)
    private String fullName;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "email", nullable = false, length = 254))
    private Email email;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "phone", length = 32))
    private PhoneNumber phone;

    protected CustomerProfile() {}

    private CustomerProfile(String fullName, Email email, PhoneNumber phone) {
        this.fullName = requireName(fullName);
        this.email = Objects.requireNonNull(email, "Email cannot be null");
        this.phone = phone;
    }

    public static CustomerProfile of(String fullName, String email, String phone) {
        return new CustomerProfile(fullName, Email.of(email), PhoneNumber.ofNullable(phone));
    }

    public String fullName() { return fullName; }
    public Email email() { return email; }
    public PhoneNumber phone() { return phone; }

    public boolean isEmpty() {
        return fullName == null || fullName.isBlank();
    }

    private static String requireName(String raw) {
        if (raw == null) throw new IllegalArgumentException("fullName cannot be null");
        String v = raw.strip();
        if (v.isEmpty()) throw new IllegalArgumentException("fullName cannot be empty");
        if (v.length() > 200) throw new IllegalArgumentException("fullName too long");
        return v;
    }
}
