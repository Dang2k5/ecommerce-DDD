package com.dang.customerservice.application.service;

import com.dang.customerservice.application.dtos.CustomerResponse;
import com.dang.customerservice.application.exceptions.CustomerNotFoundException;
import com.dang.customerservice.domain.model.valueobjects.CustomerId;
import com.dang.customerservice.domain.model.valueobjects.UserId;
import com.dang.customerservice.domain.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CustomerQueryService {

    private final CustomerRepository customerRepository;

    public CustomerResponse getByCustomerId(String customerId) {
        var id = CustomerId.of(requireId(customerId));
        var c = customerRepository.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found: " + customerId));
        return CustomerResponse.from(c);
    }

    public CustomerResponse getByIdentityUserId(String userId) {
        var uid = UserId.of(requireId(userId));
        var c = customerRepository.findByIdentityUserId(uid)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found for userId: " + userId));
        return CustomerResponse.from(c);
    }

    public Page<CustomerResponse> search(CustomerSearchCriteria criteria, Pageable pageable) {
        UserId uid = criteria.normalizedIdentityUserId() == null ? null : UserId.of(criteria.normalizedIdentityUserId());

        return customerRepository.findByFilters(
                criteria.normalizedName(),
                criteria.normalizedEmail(),
                uid,
                criteria.getStatus(),
                pageable
        ).map(CustomerResponse::from);
    }

    private static String requireId(String id) {
        if (id == null || id.isBlank()) throw new IllegalArgumentException("Id cannot be null or empty");
        return id.strip();
    }
}
