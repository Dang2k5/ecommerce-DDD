package com.dang.customerservice.domain.model.entities;

import com.dang.customerservice.domain.model.aggregates.Customer;
import com.dang.customerservice.domain.model.valueobjects.AddressDetails;
import com.dang.customerservice.domain.model.valueobjects.AddressId;
import jakarta.persistence.*;
import lombok.Getter;

import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "customer_addresses")
@Getter
public class CustomerAddress {

    @EmbeddedId
    private AddressId addressId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Embedded
    private AddressDetails details;

    @Column(name = "is_default_shipping", nullable = false)
    private boolean defaultShipping;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected CustomerAddress() {}

    public static CustomerAddress create(AddressDetails details, boolean defaultShipping) {
        CustomerAddress a = new CustomerAddress();
        a.addressId = AddressId.generate();
        a.details = Objects.requireNonNull(details, "AddressDetails cannot be null");
        a.defaultShipping = defaultShipping;
        a.createdAt = Instant.now();
        a.updatedAt = a.createdAt;
        return a;
    }

    public void attachTo(Customer customer) {
        this.customer = customer;
    }

    public void detach() {
        this.customer = null;
    }

    public void updateDetails(AddressDetails newDetails) {
        this.details = Objects.requireNonNull(newDetails, "AddressDetails cannot be null");
        touch();
    }

    public void setDefaultShipping(boolean v) {
        this.defaultShipping = v;
        touch();
    }

    private void touch() {
        this.updatedAt = Instant.now();
    }

    @Override public boolean equals(Object o){ return (o instanceof CustomerAddress other) && Objects.equals(addressId, other.addressId); }
    @Override public int hashCode(){ return Objects.hash(addressId); }
}
