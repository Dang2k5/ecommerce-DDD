package com.dang.customerservice.application.service;

import com.dang.customerservice.application.commands.*;
import com.dang.customerservice.application.exceptions.CustomerAlreadyExistsException;
import com.dang.customerservice.application.exceptions.CustomerNotFoundException;
import com.dang.customerservice.application.port.CurrentUserPort;
import com.dang.customerservice.domain.model.aggregates.Customer;
import com.dang.customerservice.domain.model.valueobjects.*;
import com.dang.customerservice.domain.repository.CustomerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CustomerApplicationService {

    private final CustomerRepository customerRepository;
    private final CurrentUserPort currentUser;

    public CustomerApplicationService(CustomerRepository customerRepository, CurrentUserPort currentUser) {
        this.customerRepository = customerRepository;
        this.currentUser = currentUser;
    }

    // ===== “ME” use-cases =====
    public CustomerId createMyProfile(CreateCustomerCommand cmd) {
        UserId uid = requireCurrentUserId();
        String username = requireCurrentUsername();

        if (customerRepository.existsByIdentityUserId(uid)) {
            throw new CustomerAlreadyExistsException("Customer already exists for userId: " + uid.value());
        }

        CustomerProfile profile = CustomerProfile.of(cmd.getFullName(), cmd.getEmail(), cmd.getPhone());
        Customer c = Customer.create(uid, username, profile);
        customerRepository.save(c);
        return c.getCustomerId();
    }

    public void updateMyProfile(UpdateCustomerCommand cmd) {
        Customer c = getMyCustomerOrThrow();
        var current = c.getProfile();

        String fullName = pick(cmd.getFullName(), current.fullName());
        String email = pick(cmd.getEmail(), current.email().value());
        String phone = cmd.getPhone() == null ? (current.phone() == null ? null : current.phone().value()) : cmd.getPhone();

        c.updateProfile(CustomerProfile.of(fullName, email, phone));
        customerRepository.save(c);
    }

    public AddressId addMyAddress(AddAddressCommand cmd) {
        Customer c = getMyCustomerOrThrow();
        AddressDetails details = AddressDetails.of(
                cmd.getReceiverName(),
                cmd.getReceiverPhone(),
                cmd.getLine1(),
                cmd.getLine2(),
                cmd.getCity(),
                cmd.getState(),
                cmd.getPostalCode(),
                cmd.getCountry()
        );
        var a = c.addAddress(details, Boolean.TRUE.equals(cmd.getDefaultShipping()));
        customerRepository.save(c);
        return a.getAddressId();
    }

    public void updateMyAddress(String addressId, UpdateAddressCommand cmd) {
        Customer c = getMyCustomerOrThrow();
        AddressId aid = AddressId.of(requireId(addressId));

        // patch style (không bắt buộc gửi đủ field)
        var old = c.getAddresses().stream()
                .filter(x -> x.getAddressId().equals(aid))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Address not found: " + addressId))
                .getDetails();

        AddressDetails details = AddressDetails.of(
                pick(cmd.getReceiverName(), old.receiverName()),
                pick(cmd.getReceiverPhone(), old.receiverPhone()),
                pick(cmd.getLine1(), old.line1()),
                pick(cmd.getLine2(), old.line2()),
                pick(cmd.getCity(), old.city()),
                pick(cmd.getState(), old.state()),
                pick(cmd.getPostalCode(), old.postalCode()),
                pick(cmd.getCountry(), old.country())
        );

        c.updateAddress(aid, details, cmd.getDefaultShipping());
        customerRepository.save(c);
    }

    public void removeMyAddress(String addressId) {
        Customer c = getMyCustomerOrThrow();
        c.removeAddress(AddressId.of(requireId(addressId)));
        customerRepository.save(c);
    }

    public void setMyDefaultShipping(String addressId) {
        Customer c = getMyCustomerOrThrow();
        c.setDefaultShipping(AddressId.of(requireId(addressId)));
        customerRepository.save(c);
    }

    // ===== Admin use-cases =====
    public void activateCustomer(String customerId) {
        Customer c = getCustomerOrThrow(customerId);
        c.activate();
        customerRepository.save(c);
    }

    public void deactivateCustomer(String customerId) {
        Customer c = getCustomerOrThrow(customerId);
        c.deactivate();
        customerRepository.save(c);
    }

    // ===== Helpers =====
    @Transactional(readOnly = true)
    protected Customer getMyCustomerOrThrow() {
        UserId uid = requireCurrentUserId();
        return customerRepository.findByIdentityUserId(uid)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found for current user"));
    }

    @Transactional(readOnly = true)
    protected Customer getCustomerOrThrow(String customerId) {
        return customerRepository.findById(CustomerId.of(requireId(customerId)))
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found: " + customerId));
    }

    private UserId requireCurrentUserId() {
        String id = currentUser.userId();
        if (id == null || id.isBlank()) throw new IllegalStateException("Unauthenticated");
        return UserId.of(id);
    }

    private String requireCurrentUsername() {
        String u = currentUser.username();
        if (u == null || u.isBlank()) throw new IllegalStateException("Unauthenticated");
        return u;
    }

    private static String requireId(String raw) {
        if (raw == null || raw.isBlank()) throw new IllegalArgumentException("Id cannot be null/empty");
        return raw.strip();
    }

    private static String pick(String incoming, String current) {
        if (incoming == null) return current;
        String v = incoming.strip();
        return v.isEmpty() ? current : v;
    }
}
