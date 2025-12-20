package com.dang.customerservice.domain.model.aggregates;

import com.dang.customerservice.domain.model.entities.CustomerAddress;
import com.dang.customerservice.domain.model.valueobjects.*;
import jakarta.persistence.*;
import lombok.Getter;

import java.time.Instant;
import java.util.*;

@Entity
@Table(
        name = "customers",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_customers_identity_user", columnNames = "identity_user_id")
        }
)
@Getter
public class Customer {

    @EmbeddedId
    private CustomerId customerId;

    @Embedded
    private UserId identityUserId;

    @Column(name = "username", nullable = false, length = 100)
    private String username;

    @Embedded
    private CustomerProfile profile;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private CustomerStatus status;

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private final List<CustomerAddress> addresses = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Customer() {}

    // ===== Factory: tạo customer theo identityUserId (sub trong JWT) =====
    public static Customer create(UserId identityUserId, String username, CustomerProfile profile) {
        Customer c = new Customer();
        c.customerId = CustomerId.generate();
        c.identityUserId = Objects.requireNonNull(identityUserId, "identityUserId cannot be null");
        c.username = requireUsername(username);
        c.profile = Objects.requireNonNull(profile, "profile cannot be null");
        c.status = CustomerStatus.ACTIVE;
        c.createdAt = Instant.now();
        c.updatedAt = c.createdAt;
        return c;
    }

    // ===== Domain behaviors =====
    public void updateProfile(CustomerProfile newProfile) {
        this.profile = Objects.requireNonNull(newProfile, "profile cannot be null");
        touch();
    }

    public void changeUsername(String username) {
        this.username = requireUsername(username);
        touch();
    }

    public void activate() {
        if (this.status != CustomerStatus.ACTIVE) {
            this.status = CustomerStatus.ACTIVE;
            touch();
        }
    }

    public void deactivate() {
        if (this.status != CustomerStatus.INACTIVE) {
            this.status = CustomerStatus.INACTIVE;
            touch();
        }
    }

    // ===== Address management: invariant "only 1 defaultShipping" =====
    public CustomerAddress addAddress(AddressDetails details, boolean makeDefault) {
        boolean first = addresses.isEmpty();
        boolean defaultShipping = first || makeDefault;

        if (defaultShipping) unsetDefaultShipping();

        CustomerAddress a = CustomerAddress.create(details, defaultShipping);
        a.attachTo(this);
        addresses.add(a);

        touch();
        return a;
    }

    public void updateAddress(AddressId addressId, AddressDetails newDetails, Boolean setDefaultShipping) {
        CustomerAddress a = findAddressOrThrow(addressId);
        a.updateDetails(newDetails);

        if (setDefaultShipping != null && setDefaultShipping) {
            unsetDefaultShipping();
            a.setDefaultShipping(true);
        }

        // nếu user cố tình setDefaultShipping=false mà đây là default duy nhất -> giữ invariant:
        if (setDefaultShipping != null && !setDefaultShipping && a.isDefaultShipping() && addresses.size() > 1) {
            a.setDefaultShipping(false);
            // đảm bảo vẫn có 1 default
            ensureHasDefaultShipping();
        }

        touch();
    }

    public void removeAddress(AddressId addressId) {
        CustomerAddress a = findAddressOrThrow(addressId);
        boolean wasDefault = a.isDefaultShipping();

        a.detach();
        addresses.removeIf(x -> x.getAddressId().equals(addressId));

        if (wasDefault) {
            ensureHasDefaultShipping();
        }
        touch();
    }

    public void setDefaultShipping(AddressId addressId) {
        CustomerAddress a = findAddressOrThrow(addressId);
        unsetDefaultShipping();
        a.setDefaultShipping(true);
        touch();
    }

    public Optional<CustomerAddress> defaultShippingAddress() {
        return addresses.stream().filter(CustomerAddress::isDefaultShipping).findFirst();
    }

    public List<CustomerAddress> getAddresses() {
        return Collections.unmodifiableList(addresses);
    }

    // ===== Helpers =====
    private void unsetDefaultShipping() {
        for (CustomerAddress x : addresses) {
            if (x.isDefaultShipping()) x.setDefaultShipping(false);
        }
    }

    private void ensureHasDefaultShipping() {
        if (addresses.isEmpty()) return;
        boolean exists = addresses.stream().anyMatch(CustomerAddress::isDefaultShipping);
        if (!exists) {
            addresses.get(0).setDefaultShipping(true);
        }
    }

    private CustomerAddress findAddressOrThrow(AddressId addressId) {
        return addresses.stream()
                .filter(a -> a.getAddressId().equals(addressId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Address not found: " + addressId.value()));
    }

    private static String requireUsername(String raw) {
        if (raw == null) throw new IllegalArgumentException("username cannot be null");
        String v = raw.strip();
        if (v.isEmpty()) throw new IllegalArgumentException("username cannot be empty");
        if (v.length() > 100) throw new IllegalArgumentException("username too long");
        return v;
    }

    private void touch() { this.updatedAt = Instant.now(); }

    @Override public boolean equals(Object o){ return (o instanceof Customer other) && Objects.equals(customerId, other.customerId); }
    @Override public int hashCode(){ return Objects.hash(customerId); }
}
