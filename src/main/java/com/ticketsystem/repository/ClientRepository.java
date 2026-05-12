package com.ticketsystem.repository;

import com.ticketsystem.model.Client;
import org.hibernate.Session;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for {@link Client} entities.
 */
public class ClientRepository extends AbstractRepository<Client, UUID> {

    public ClientRepository() {
        super(Client.class);
    }

    /** Finds clients by first and last name (case-insensitive). */
    public List<Client> findByFullName(String firstName, String lastName) {
        try (Session session = sessionFactory.openSession()) {
            return session.createQuery(
                    "FROM Client c WHERE LOWER(c.firstName) = LOWER(:fn) AND LOWER(c.lastName) = LOWER(:ln)",
                    Client.class)
                    .setParameter("fn", firstName)
                    .setParameter("ln", lastName)
                    .list();
        }
    }

    /** Searches clients by partial name match (for admin lookup). */
    public List<Client> searchByName(String query) {
        try (Session session = sessionFactory.openSession()) {
            String pattern = "%" + query.toLowerCase() + "%";
            return session.createQuery(
                    "FROM Client c WHERE LOWER(c.firstName) LIKE :q OR LOWER(c.lastName) LIKE :q",
                    Client.class)
                    .setParameter("q", pattern)
                    .list();
        }
    }
}
