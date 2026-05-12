package com.ticketsystem.util;

import com.ticketsystem.model.Administrator;
import com.ticketsystem.model.Operator;
import com.ticketsystem.model.User;

/**
 * In-memory session holder for the currently authenticated user.
 * A single-threaded CLI app doesn't need JWT or cookies — this singleton is sufficient.
 */
public final class SessionManager {

    private static SessionManager instance;

    private User currentUser;

    private SessionManager() {}

    // ── Singleton ─────────────────────────────────────────────────────────────

    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    // ── Session state ─────────────────────────────────────────────────────────

    public User getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }

    public boolean isOperator() {
        return currentUser instanceof Operator;
    }

    public boolean isAdministrator() {
        return currentUser instanceof Administrator;
    }

    /** Returns true if logged in with at least Operator-level access. */
    public boolean isAtLeastOperator() {
        return isLoggedIn();
    }

    public void logout() {
        this.currentUser = null;
    }

    // ── Display ───────────────────────────────────────────────────────────────

    public String getStatusLine() {
        if (!isLoggedIn()) {
            return "Niezalogowany";
        }
        return String.format("Zalogowany jako: %s [%s]",
                currentUser.getUsername(),
                currentUser.getRole().getDisplayName());
    }

    /** Returns the username to use in logs — "anonim" if not logged in. */
    public String getActorName() {
        return isLoggedIn() ? currentUser.getUsername() : "anonim";
    }
}
