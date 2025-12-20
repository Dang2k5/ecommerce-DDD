package com.dang.customerservice.domain.repository;

import com.dang.customerservice.domain.model.aggregates.Customer;
import com.dang.customerservice.domain.model.valueobjects.CustomerId;
import com.dang.customerservice.domain.model.valueobjects.CustomerStatus;
import com.dang.customerservice.domain.model.valueobjects.UserId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface CustomerRepository {
    Customer save(Customer customer);

    Optional<Customer> findById(CustomerId id);

    Optional<Customer> findByIdentityUserId(UserId userId);

    boolean existsByIdentityUserId(UserId userId);

    Page<Customer> findByFilters(String name,
                                 String email,
                                 UserId userId,
                                 CustomerStatus status,
                                 Pageable pageable);
}
