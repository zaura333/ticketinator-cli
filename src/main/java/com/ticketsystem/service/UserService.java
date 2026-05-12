package com.ticketsystem.service;

import com.ticketsystem.model.Administrator;
import com.ticketsystem.model.Operator;
import com.ticketsystem.model.User;
import com.ticketsystem.repository.UserRepository;
import com.ticketsystem.util.PasswordUtil;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Business logic for user management (Operators and Administrators).
 */
public class UserService {

    private final UserRepository userRepository;

    public UserService() {
        this.userRepository = new UserRepository();
    }

    // ── Create ────────────────────────────────────────────────────────────────

    public Operator createOperator(String username, String plainPassword) {
        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Użytkownik o nazwie '" + username + "' już istnieje.");
        }
        Operator operator = new Operator(username, PasswordUtil.hashPassword(plainPassword));
        return (Operator) userRepository.save(operator);
    }

    public Administrator createAdministrator(String username, String plainPassword) {
        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Użytkownik o nazwie '" + username + "' już istnieje.");
        }
        Administrator admin = new Administrator(username, PasswordUtil.hashPassword(plainPassword));
        return (Administrator) userRepository.save(admin);
    }

    // ── Read ──────────────────────────────────────────────────────────────────

    public Optional<User> findById(UUID id) {
        return userRepository.findById(id);
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public List<Operator> findAllOperators() {
        return userRepository.findAllOperators();
    }

    // ── Update ────────────────────────────────────────────────────────────────

    public User changePassword(User user, String newPlainPassword) {
        user.setPassword(PasswordUtil.hashPassword(newPlainPassword));
        return userRepository.update(user);
    }

    // ── Delete ────────────────────────────────────────────────────────────────

    public void deleteUser(User user) {
        userRepository.delete(user);
    }

    // ── Checks ────────────────────────────────────────────────────────────────

    public boolean usernameExists(String username) {
        return userRepository.existsByUsername(username);
    }

    /**
     * Saves a pre-built user (used during seeding).
     */
    public User saveUser(User user) {
        return userRepository.save(user);
    }
}
