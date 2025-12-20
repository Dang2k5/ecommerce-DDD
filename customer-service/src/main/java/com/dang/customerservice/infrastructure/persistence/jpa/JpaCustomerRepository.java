package com.dang.customerservice.infrastructure.persistence.jpa;

import com.dang.customerservice.domain.model.aggregates.Customer;
import com.dang.customerservice.domain.model.valueobjects.CustomerId;
import com.dang.customerservice.domain.model.valueobjects.CustomerStatus;
import com.dang.customerservice.domain.model.valueobjects.UserId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Locale;
import java.util.Optional;

@Repository
public interface JpaCustomerRepository extends JpaRepository<Customer, CustomerId> {

    Optional<Customer> findByIdentityUserId(UserId userId);

    boolean existsByIdentityUserId(UserId userId);

    @Query("""
        select c
        from Customer c
        where
            (:nameLike is null or lower(c.profile.fullName) like :nameLike)
        and (:emailLike is null or lower(c.profile.email.value) like :emailLike)
        and (:userId is null or c.identityUserId = :userId)
        and (:status is null or c.status = :status)
    """)
    Page<Customer> findByFiltersLike(@Param("nameLike") String nameLike,
                                     @Param("emailLike") String emailLike,
                                     @Param("userId") UserId userId,
                                     @Param("status") CustomerStatus status,
                                     Pageable pageable);

    default Page<Customer> findByFilters(String name,
                                         String email,
                                         UserId userId,
                                         CustomerStatus status,
                                         Pageable pageable) {
        return findByFiltersLike(toLikePattern(name), toLikePattern(email), userId, status, pageable);
    }

    private static String toLikePattern(String raw) {
        if (raw == null) return null;
        String v = raw.strip();
        if (v.isEmpty()) return null;
        return "%" + v.toLowerCase(Locale.ROOT) + "%";
    }
}
