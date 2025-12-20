package com.dang.customerservice.application.service;

import com.dang.customerservice.domain.model.valueobjects.CustomerStatus;
import lombok.*;

@Getter @Setter
@NoArgsConstructor
public class CustomerSearchCriteria {

    private String name;
    private String email;
    private String identityUserId;
    private CustomerStatus status;

    public CustomerSearchCriteria(String name, String email, String identityUserId, CustomerStatus status) {
        this.name = normalize(name);
        this.email = normalize(email);
        this.identityUserId = normalize(identityUserId);
        this.status = status;
    }

    public String normalizedName() { return normalize(name); }
    public String normalizedEmail() { return normalize(email); }
    public String normalizedIdentityUserId() { return normalize(identityUserId); }

    private static String normalize(String s) {
        if (s == null) return null;
        String v = s.strip();
        return v.isEmpty() ? null : v;
    }
}
