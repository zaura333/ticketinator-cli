package com.ticketsystem.repository;

import com.ticketsystem.model.Operator;
import com.ticketsystem.model.User;
import org.hibernate.Session;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for {@link User} entities (Operators and Administrators).
 */
public class UserRepository extends AbstractRepository<User, UUID> {

    public UserRepository() {
        super(User.class);
    }

    /** Finds a user by username (case-sensitive, as usernames are unique). */
    public Optional<User> findByUsername(String username) {
        try (Session session = sessionFactory.openSession()) {
            return session.createQuery(
                    "FROM User u WHERE u.username = :username", User.class)
                    .setParameter("username", username)
                    .uniqueResultOptional();
        }
    }

    /** Returns all operators (discriminator = 'OPERATOR'). */
    public List<Operator> findAllOperators() {
        try (Session session = sessionFactory.openSession()) {
            return session.createQuery(
                    "FROM Operator o ORDER BY o.username", Operator.class).list();
        }
    }

    /** Returns all users ordered by username. */
    @Override
    public List<User> findAll() {
        try (Session session = sessionFactory.openSession()) {
            return session.createQuery(
                    "FROM User u ORDER BY u.username", User.class).list();
        }
    }

    /** Checks whether a username is already taken. */
    public boolean existsByUsername(String username) {
        try (Session session = sessionFactory.openSession()) {
            Long count = session.createQuery(
                    "SELECT COUNT(u) FROM User u WHERE u.username = :username", Long.class)
                    .setParameter("username", username)
                    .uniqueResult();
            return count != null && count > 0;
        }
    }
}
