package com.dang.customerservice.domain.model.valueobjects;

import jakarta.persistence.*;
import java.io.Serializable;

@Embeddable
@Access(AccessType.FIELD)
public class AddressDetails implements Serializable {

    @Column(name = "receiver_name", nullable = false, length = 200)
    private String receiverName;

    @Column(name = "receiver_phone", length = 32)
    private String receiverPhone;

    @Column(name = "line1", nullable = false, length = 255)
    private String line1;

    @Column(name = "line2", length = 255)
    private String line2;

    @Column(name = "city", nullable = false, length = 120)
    private String city;

    @Column(name = "state", length = 120)
    private String state;

    @Column(name = "postal_code", length = 20)
    private String postalCode;

    @Column(name = "country", nullable = false, length = 2)
    private String country; // ISO-2: VN, TH, ...

    protected AddressDetails() {}

    private AddressDetails(String receiverName,
                           String receiverPhone,
                           String line1,
                           String line2,
                           String city,
                           String state,
                           String postalCode,
                           String country) {
        this.receiverName = req(receiverName, "receiverName", 200);
        this.receiverPhone = normalizeOptional(receiverPhone);
        this.line1 = req(line1, "line1", 255);
        this.line2 = normalizeOptional(line2);
        this.city = req(city, "city", 120);
        this.state = normalizeOptional(state);
        this.postalCode = normalizeOptional(postalCode);
        this.country = req(country, "country", 2).toUpperCase();
        if (this.country.length() != 2) throw new IllegalArgumentException("country must be ISO-2 (e.g. VN)");
    }

    public static AddressDetails of(String receiverName,
                                    String receiverPhone,
                                    String line1,
                                    String line2,
                                    String city,
                                    String state,
                                    String postalCode,
                                    String country) {
        return new AddressDetails(receiverName, receiverPhone, line1, line2, city, state, postalCode, country);
    }

    public String receiverName() { return receiverName; }
    public String receiverPhone() { return receiverPhone; }
    public String line1() { return line1; }
    public String line2() { return line2; }
    public String city() { return city; }
    public String state() { return state; }
    public String postalCode() { return postalCode; }
    public String country() { return country; }

    private static String req(String raw, String field, int max) {
        if (raw == null) throw new IllegalArgumentException(field + " cannot be null");
        String v = raw.strip();
        if (v.isEmpty()) throw new IllegalArgumentException(field + " cannot be empty");
        if (v.length() > max) throw new IllegalArgumentException(field + " too long");
        return v;
    }

    private static String normalizeOptional(String raw) {
        if (raw == null) return null;
        String v = raw.strip();
        return v.isEmpty() ? null : v;
    }
}
