package com.dang.customerservice.interfaces;

import com.dang.customerservice.application.commands.*;
import com.dang.customerservice.application.dtos.CustomerResponse;
import com.dang.customerservice.application.service.CustomerApplicationService;
import com.dang.customerservice.application.service.CustomerQueryService;
import com.dang.customerservice.application.service.CustomerSearchCriteria;
import com.dang.customerservice.domain.model.valueobjects.CustomerStatus;
import jakarta.validation.Valid;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private final CustomerApplicationService app;
    private final CustomerQueryService query;

    public CustomerController(CustomerApplicationService app, CustomerQueryService query) {
        this.app = app;
        this.query = query;
    }

    // =========================
    // ME endpoints
    // =========================

    @PostMapping("/me")
    public ResponseEntity<CustomerResponse> createMyProfile(@RequestBody @Valid CreateCustomerCommand cmd,
                                                            UriComponentsBuilder uriBuilder) {
        var id = app.createMyProfile(cmd);
        URI location = uriBuilder.path("/api/customers/me").build().toUri();
        return ResponseEntity.created(location).body(query.getByCustomerId(id.value()));
    }

    @GetMapping("/me")
    public ResponseEntity<CustomerResponse> getMe() {
        return ResponseEntity.ok(query.getByIdentityUserId(getCurrentUserIdFromSecurityContext()));
    }

    @PutMapping("/me")
    public ResponseEntity<CustomerResponse> updateMe(@RequestBody @Valid UpdateCustomerCommand cmd) {
        app.updateMyProfile(cmd);
        return ResponseEntity.ok(query.getByIdentityUserId(getCurrentUserIdFromSecurityContext()));
    }

    @PostMapping("/me/addresses")
    public ResponseEntity<CustomerResponse> addAddress(@RequestBody @Valid AddAddressCommand cmd) {
        app.addMyAddress(cmd);
        return ResponseEntity.ok(query.getByIdentityUserId(getCurrentUserIdFromSecurityContext()));
    }

    @PutMapping("/me/addresses/{addressId}")
    public ResponseEntity<CustomerResponse> updateAddress(@PathVariable String addressId,
                                                          @RequestBody @Valid UpdateAddressCommand cmd) {
        app.updateMyAddress(addressId, cmd);
        return ResponseEntity.ok(query.getByIdentityUserId(getCurrentUserIdFromSecurityContext()));
    }

    @DeleteMapping("/me/addresses/{addressId}")
    public ResponseEntity<CustomerResponse> removeAddress(@PathVariable String addressId) {
        app.removeMyAddress(addressId);
        return ResponseEntity.ok(query.getByIdentityUserId(getCurrentUserIdFromSecurityContext()));
    }

    @PostMapping("/me/addresses/{addressId}/default-shipping")
    public ResponseEntity<CustomerResponse> setDefaultShipping(@PathVariable String addressId) {
        app.setMyDefaultShipping(addressId);
        return ResponseEntity.ok(query.getByIdentityUserId(getCurrentUserIdFromSecurityContext()));
    }

    // =========================
    // ADMIN endpoints
    // =========================

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @GetMapping("/admin/{customerId}")
    public ResponseEntity<CustomerResponse> getById(@PathVariable String customerId) {
        return ResponseEntity.ok(query.getByCustomerId(customerId));
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @GetMapping("/admin")
    public ResponseEntity<Page<CustomerResponse>> search(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String identityUserId,
            @RequestParam(required = false) CustomerStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.max(1, Math.min(size, 100)));
        var criteria = new CustomerSearchCriteria(name, email, identityUserId, status);
        return ResponseEntity.ok(query.search(criteria, pageable));
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PostMapping("/admin/{customerId}/activate")
    public ResponseEntity<Void> activate(@PathVariable String customerId) {
        app.activateCustomer(customerId);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PostMapping("/admin/{customerId}/deactivate")
    public ResponseEntity<Void> deactivate(@PathVariable String customerId) {
        app.deactivateCustomer(customerId);
        return ResponseEntity.noContent().build();
    }

    // ---- helper ----
    private String getCurrentUserIdFromSecurityContext() {
        var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) throw new IllegalStateException("Unauthenticated");
        Object p = auth.getPrincipal();
        if (p instanceof com.dang.customerservice.infrastructure.security.AuthenticatedUser u) return u.userId();
        throw new IllegalStateException("Unauthenticated");
    }
}
