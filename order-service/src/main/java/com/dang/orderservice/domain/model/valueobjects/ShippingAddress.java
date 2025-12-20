package com.dang.orderservice.domain.model.valueobjects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class ShippingAddress implements Serializable {

    @Column(name = "ship_full_name", nullable = false, length = 120)
    private String fullName;

    @Column(name = "ship_phone", nullable = false, length = 30)
    private String phone;

    @Column(name = "ship_line1", nullable = false, length = 200)
    private String line1;

    @Column(name = "ship_line2", length = 200)
    private String line2;

    @Column(name = "ship_city", nullable = false, length = 120)
    private String city;

    @Column(name = "ship_state", length = 120)
    private String state;

    @Column(name = "ship_postal_code", length = 20)
    private String postalCode;

    @Column(name = "ship_country", nullable = false, length = 2)
    private String country;

    protected ShippingAddress() {}

    private ShippingAddress(
            String fullName,
            String phone,
            String line1,
            String line2,
            String city,
            String state,
            String postalCode,
            String country
    ) {
        this.fullName = required(fullName, "fullName");
        this.phone = required(phone, "phone");
        this.line1 = required(line1, "line1");
        this.line2 = normalize(line2);
        this.city = required(city, "city");
        this.state = normalize(state);
        this.postalCode = normalize(postalCode);
        this.country = required(country, "country").toUpperCase();
    }

    public static ShippingAddress of(
            String fullName,
            String phone,
            String line1,
            String line2,
            String city,
            String state,
            String postalCode,
            String country
    ) {
        return new ShippingAddress(fullName, phone, line1, line2, city, state, postalCode, country);
    }

    private static String required(String v, String f) {
        if (v == null || v.isBlank()) throw new IllegalArgumentException(f + " is required");
        return v.strip();
    }

    private static String normalize(String v) {
        return (v == null || v.isBlank()) ? null : v.strip();
    }

    public String fullName() { return fullName; }
    public String phone() { return phone; }
    public String line1() { return line1; }
    public String line2() { return line2; }
    public String city() { return city; }
    public String state() { return state; }
    public String postalCode() { return postalCode; }
    public String country() { return country; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ShippingAddress other)) return false;
        return Objects.equals(fullName, other.fullName)
                && Objects.equals(phone, other.phone)
                && Objects.equals(line1, other.line1)
                && Objects.equals(line2, other.line2)
                && Objects.equals(city, other.city)
                && Objects.equals(state, other.state)
                && Objects.equals(postalCode, other.postalCode)
                && Objects.equals(country, other.country);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fullName, phone, line1, line2, city, state, postalCode, country);
    }
}
