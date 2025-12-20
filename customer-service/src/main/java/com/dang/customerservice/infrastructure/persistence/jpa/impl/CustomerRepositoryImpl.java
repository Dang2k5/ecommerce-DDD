package com.dang.customerservice.infrastructure.persistence.jpa.impl;

import com.dang.customerservice.domain.model.aggregates.Customer;
import com.dang.customerservice.domain.model.valueobjects.CustomerId;
import com.dang.customerservice.domain.model.valueobjects.CustomerStatus;
import com.dang.customerservice.domain.model.valueobjects.UserId;
import com.dang.customerservice.domain.repository.CustomerRepository;
import com.dang.customerservice.infrastructure.persistence.jpa.JpaCustomerRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class CustomerRepositoryImpl implements CustomerRepository {

    private final JpaCustomerRepository jpa;

    public CustomerRepositoryImpl(JpaCustomerRepository jpa) {
        this.jpa = jpa;
    }

    @Override public Customer save(Customer customer) { return jpa.save(customer); }

    @Override public Optional<Customer> findById(CustomerId id) { return jpa.findById(id); }

    @Override public Optional<Customer> findByIdentityUserId(UserId userId) { return jpa.findByIdentityUserId(userId); }

    @Override public boolean existsByIdentityUserId(UserId userId) { return jpa.existsByIdentityUserId(userId); }

    @Override
    public Page<Customer> findByFilters(String name, String email, UserId userId, CustomerStatus status, Pageable pageable) {
        return jpa.findByFilters(name, email, userId, status, pageable);
    }
}