package com.dang.customerservice.application.dtos;

import com.dang.customerservice.domain.model.aggregates.Customer;
import lombok.Getter;

import java.util.List;

@Getter
public class CustomerResponse {

    private String customerId;
    private String identityUserId;
    private String username;

    private String fullName;
    private String email;
    private String phone;

    private String status;
    private String defaultShippingAddressId;

    private List<AddressResponse> addresses;

    private CustomerResponse() {}

    public static CustomerResponse from(Customer c) {
        CustomerResponse r = new CustomerResponse();
        r.customerId = c.getCustomerId().value();
        r.identityUserId = c.getIdentityUserId().value();
        r.username = c.getUsername();

        r.fullName = c.getProfile().fullName();
        r.email = c.getProfile().email().value();
        r.phone = c.getProfile().phone() == null ? null : c.getProfile().phone().value();

        r.status = c.getStatus().name();
        r.defaultShippingAddressId = c.defaultShippingAddress()
                .map(a -> a.getAddressId().value())
                .orElse(null);

        r.addresses = c.getAddresses().stream().map(AddressResponse::from).toList();
        return r;
    }
}
