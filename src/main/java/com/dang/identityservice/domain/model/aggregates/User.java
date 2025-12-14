package com.dang.identityservice.domain.model.aggregates;

import com.dang.identityservice.domain.model.valueobjects.Email;
import com.dang.identityservice.domain.model.valueobjects.PasswordHash;
import com.dang.identityservice.domain.model.valueobjects.RoleId;
import com.dang.identityservice.domain.model.valueobjects.UserId;
import com.dang.identityservice.domain.model.valueobjects.UserStatus;
import com.dang.identityservice.domain.model.valueobjects.Username;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embedded;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AccessLevel;
import lombok.Getter;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "users")
@Getter
public class User {

    @EmbeddedId
    private UserId userId;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "username", nullable = false, unique = true, length = 50))
    private Username username;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "email", nullable = false, unique = true, length = 120))
    private Email email;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "password_hash", nullable = false, length = 255))
    private PasswordHash passwordHash;

    /**
     * Transitional column for backward compatibility with older schema.
     * Old versions used a boolean 'active' instead of enum status.
     */
    @Column(name = "active")
    @Getter(AccessLevel.NONE)
    private Boolean legacyActive;

    /**
     * New canonical status. NOTE: kept nullable for safe auto-migration (ddl-auto=update)
     * when the existing users table already has rows.
     *
     * One-time backfill is done via data.sql (see resources/data.sql).
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = true, length = 32)
    @Getter(AccessLevel.NONE)
    private UserStatus status;

    /**
     * Cross-aggregate reference: we only store RoleId(s) here.
     * The Role aggregate is loaded via repository when needed (application/query side).
     */
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id", referencedColumnName = "user_id")
    )
    @AttributeOverride(name = "value", column = @Column(name = "role_id", nullable = false, length = 36))
    @Getter(AccessLevel.NONE)
    private Set<RoleId> roleIds = new HashSet<>();

    protected User() {
    }

    private User(UserId id, Username username, Email email, PasswordHash hash) {
        this.userId = Objects.requireNonNull(id);
        this.username = Objects.requireNonNull(username);
        this.email = Objects.requireNonNull(email);
        this.passwordHash = Objects.requireNonNull(hash);
        this.status = UserStatus.ACTIVE;
        this.legacyActive = Boolean.TRUE;
    }

    public static User create(Username username, Email email, PasswordHash hash) {
        return new User(UserId.generate(), username, email, hash);
    }

    /**
     * Canonical getter: never returns null.
     * If DB row is from old schema (status is null), we derive from legacyActive.
     */
    public UserStatus getStatus() {
        if (status != null) return status;
        return (legacyActive == null || legacyActive) ? UserStatus.ACTIVE : UserStatus.DISABLED;
    }

    /**
     * DDD: expose immutable view so callers cannot mutate aggregate state bypassing invariants.
     */
    public Set<RoleId> getRoleIds() {
        return Set.copyOf(roleIds);
    }

    public boolean canLogin() {
        return getStatus().canLogin();
    }

    /**
     * Compatibility helper (old API used boolean active).
     */
    @Transient
    public boolean isActive() {
        return getStatus().isActive();
    }

    public void assignRole(RoleId roleId) {
        roleIds.add(Objects.requireNonNull(roleId));
    }

    public void removeRole(RoleId roleId) {
        roleIds.remove(Objects.requireNonNull(roleId));
    }

    public void changeStatus(UserStatus newStatus) {
        this.status = Objects.requireNonNull(newStatus);
    }

    public void deactivate() {
        this.status = UserStatus.DISABLED;
    }

    public void activate() {
        this.status = UserStatus.ACTIVE;
    }

    public void lock() {
        this.status = UserStatus.LOCKED;
    }

    public void markPendingActivation() {
        this.status = UserStatus.PENDING_ACTIVATION;
    }

    public void changePassword(PasswordHash newHash) {
        this.passwordHash = Objects.requireNonNull(newHash);
    }

    public void changeEmail(Email newEmail) {
        this.email = Objects.requireNonNull(newEmail);
    }

    /**
     * After loading existing rows, normalize null status into a non-null value so domain logic is stable.
     */
    @PostLoad
    private void postLoad() {
        if (this.status == null) {
            this.status = (legacyActive == null || legacyActive) ? UserStatus.ACTIVE : UserStatus.DISABLED;
        }
        if (this.legacyActive == null) {
            this.legacyActive = this.status.isActive();
        }
    }

    @PrePersist
    private void prePersist() {
        if (this.status == null) this.status = UserStatus.ACTIVE;
        this.legacyActive = this.status.isActive();
    }

    @PreUpdate
    private void preUpdate() {
        if (this.status == null) this.status = UserStatus.ACTIVE;
        this.legacyActive = this.status.isActive();
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof User other && Objects.equals(userId, other.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId);
    }
}
