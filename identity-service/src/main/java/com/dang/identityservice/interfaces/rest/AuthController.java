package com.dang.identityservice.interfaces.rest;

import com.dang.identityservice.application.dto.ApiResponse;
import com.dang.identityservice.application.dto.auth.*;
import com.dang.identityservice.application.service.AuthApplicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthApplicationService auth;

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest req) {
        return ApiResponse.ok(auth.login(req));
    }

    @PostMapping("/refresh")
    public ApiResponse<LoginResponse> refresh(@Valid @RequestBody RefreshRequest req) {
        return ApiResponse.ok(auth.refresh(req));
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(@Valid @RequestBody LogoutRequest req) {
        auth.logout(req);
        return ApiResponse.ok(null);
    }

    @PostMapping("/register")
    public ApiResponse<RegisterResponse> register(@Valid @RequestBody RegisterRequest req) {
        return ApiResponse.ok(auth.register(req));
    }


    @PostMapping("/introspect")
    public ApiResponse<IntrospectResponse> introspect(@Valid @RequestBody IntrospectRequest req) {
        return ApiResponse.ok(auth.introspect(req));
    }
}
