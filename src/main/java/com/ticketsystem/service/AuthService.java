package com.ticketsystem.service;

import com.ticketsystem.model.User;
import com.ticketsystem.repository.UserRepository;
import com.ticketsystem.util.PasswordUtil;
import com.ticketsystem.util.SessionManager;

import java.util.Optional;

/**
 * Handles authentication (login / logout).
 */
public class AuthService {

    private final UserRepository userRepository;
    private final SessionManager sessionManager;

    public AuthService() {
        this.userRepository = new UserRepository();
        this.sessionManager = SessionManager.getInstance();
    }

    /**
     * Attempts to log in with the provided credentials.
     *
     * @param username plain username
     * @param password plain-text password
     * @return the authenticated {@link User} if successful
     * @throws SecurityException if credentials are invalid
     */
    public User login(String username, String password) {
        Optional<User> userOpt = userRepository.findByUsername(username);

        if (userOpt.isEmpty()) {
            throw new SecurityException("Nieprawidłowa nazwa użytkownika lub hasło.");
        }

        User user = userOpt.get();

        if (!PasswordUtil.checkPassword(password, user.getPassword())) {
            throw new SecurityException("Nieprawidłowa nazwa użytkownika lub hasło.");
        }

        sessionManager.setCurrentUser(user);
        return user;
    }

    /**
     * Logs out the currently authenticated user.
     */
    public void logout() {
        sessionManager.logout();
    }
}
