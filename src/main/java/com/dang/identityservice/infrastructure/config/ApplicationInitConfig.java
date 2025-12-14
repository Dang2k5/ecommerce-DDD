package com.dang.identityservice.infrastructure.config;

import com.dang.identityservice.application.port.PasswordHasher;
import com.dang.identityservice.domain.model.aggregates.Role;
import com.dang.identityservice.domain.model.aggregates.User;
import com.dang.identityservice.domain.model.valueobjects.Email;
import com.dang.identityservice.domain.model.valueobjects.PasswordHash;
import com.dang.identityservice.domain.model.valueobjects.Username;
import com.dang.identityservice.domain.repository.RoleRepository;
import com.dang.identityservice.domain.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

@Configuration
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ApplicationInitConfig {

    PasswordHasher passwordHasher;

    static final String ADMIN_USERNAME = "admin";
    static final String ADMIN_PASSWORD = "admin";
    static final String ROLE_USER = "ROLE_USER";
    static final String ROLE_ADMIN = "ROLE_ADMIN";

    /**
     * Seed default data on startup:
     * - If admin user doesn't exist: create ROLE_USER, ROLE_ADMIN and user admin/admin
     * <p>
     * Safe to run on every startup because it checks existing data.
     */
    @Bean
    ApplicationRunner applicationRunner(UserRepository userRepository, RoleRepository roleRepository) {
        return args -> seedFirstRun(userRepository, roleRepository);
    }

    @Transactional
    void seedFirstRun(UserRepository userRepository, RoleRepository roleRepository) {
        log.info("[INIT] Starting default data initialization...");

        // 1) Ensure default roles exist (idempotent)
        Role userRole = roleRepository.findByCode(ROLE_USER)
                .orElseGet(() -> roleRepository.save(Role.create(ROLE_USER, "User role")));

        Role adminRole = roleRepository.findByCode(ROLE_ADMIN)
                .orElseGet(() -> roleRepository.save(Role.create(ROLE_ADMIN, "Admin role")));

        // 2) If admin exists -> nothing else to do
        boolean adminExisted = userRepository.findByUsername(Username.of(ADMIN_USERNAME)).isPresent();
        if (adminExisted) {
            log.info("[INIT] User '{}' already exists. Default roles ensured. Skipping bootstrap admin creation.", ADMIN_USERNAME);
            return;
        }

        // 3) Create bootstrap admin
        User admin = User.create(
                Username.of(ADMIN_USERNAME),
                Email.of("admin@local.com"),
                PasswordHash.of(passwordHasher.hash(ADMIN_PASSWORD))
        );

        // Assign roles to admin (choose your policy)
        admin.assignRole(adminRole.getRoleId());
        // admin.assignRole(userRole.getRoleId()); // bật nếu muốn admin có cả ROLE_USER

        userRepository.save(admin);

        log.warn("[INIT] Admin user '{}' created with default password '{}'. Please change it immediately.",
                ADMIN_USERNAME, ADMIN_PASSWORD);
        log.info("[INIT] Default data initialization completed.");
    }
}
