package com.ticketsystem.model;

import com.ticketsystem.model.enums.UserRole;
import jakarta.persistence.*;
import java.util.UUID;

/**
 * Abstract base class for all system users (Operator and Administrator).
 * Uses SINGLE_TABLE inheritance — all users stored in the {@code users} table,
 * distinguished by the {@code role} discriminator column.
 */
@Entity
@Table(name = "users")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "role", discriminatorType = DiscriminatorType.STRING, length = 20)
public abstract class User {

    @Id
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "VARCHAR(36)")
    private UUID id;

    @PrePersist
    protected void generateId() {
        if (id == null) id = UUID.randomUUID();
    }

    @Column(name = "username", unique = true, nullable = false, length = 100)
    private String username;

    @Column(name = "password", nullable = false, length = 60)
    private String password; // BCrypt hash — always 60 chars

    // ── Constructors ──────────────────────────────────────────────────────────

    protected User() {}

    protected User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    // ── Abstract methods ──────────────────────────────────────────────────────

    /**
     * Returns the role of this user. Implemented by concrete subclasses.
     */
    public abstract UserRole getRole();

    // ── Getters & Setters ─────────────────────────────────────────────────────

    public UUID getId() { return id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    // ── Display ───────────────────────────────────────────────────────────────

    @Override
    public String toString() {
        return String.format("%s [%s]", username, getRole().getDisplayName());
    }
}
