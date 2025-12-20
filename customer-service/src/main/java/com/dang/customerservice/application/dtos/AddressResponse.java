package com.dang.customerservice.application.dtos;

import com.dang.customerservice.domain.model.entities.CustomerAddress;
import lombok.Getter;

@Getter
public class AddressResponse {
    private String addressId;
    private boolean defaultShipping;

    private String receiverName;
    private String receiverPhone;

    private String line1;
    private String line2;
    private String city;
    private String state;
    private String postalCode;
    private String country;

    private AddressResponse() {}

    public static AddressResponse from(CustomerAddress a) {
        AddressResponse r = new AddressResponse();
        r.addressId = a.getAddressId().value();
        r.defaultShipping = a.isDefaultShipping();

        r.receiverName = a.getDetails().receiverName();
        r.receiverPhone = a.getDetails().receiverPhone();
        r.line1 = a.getDetails().line1();
        r.line2 = a.getDetails().line2();
        r.city = a.getDetails().city();
        r.state = a.getDetails().state();
        r.postalCode = a.getDetails().postalCode();
        r.country = a.getDetails().country();
        return r;
    }
}
