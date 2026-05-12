package com.ticketsystem.model;

import com.ticketsystem.model.enums.UserRole;
import jakarta.persistence.*;

/**
 * Administrator — has full control over the system:
 * can manage users, modify/delete any ticket or comment,
 * and refresh delayed ticket statuses.
 */
@Entity
@DiscriminatorValue("ADMINISTRATOR")
public class Administrator extends User {

    // ── Constructors ──────────────────────────────────────────────────────────

    public Administrator() {}

    public Administrator(String username, String password) {
        super(username, password);
    }

    // ── Role ──────────────────────────────────────────────────────────────────

    @Override
    public UserRole getRole() {
        return UserRole.ADMINISTRATOR;
    }
}
