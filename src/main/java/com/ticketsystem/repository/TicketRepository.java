package com.ticketsystem.repository;

import com.ticketsystem.model.Ticket;
import com.ticketsystem.model.User;
import com.ticketsystem.model.enums.TicketStatus;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Repository for {@link Ticket} entities with ticket-specific queries.
 */
public class TicketRepository extends AbstractRepository<Ticket, UUID> {

    public TicketRepository() {
        super(Ticket.class);
    }

    // ── Custom queries ────────────────────────────────────────────────────────

    /** Returns all tickets with the given status. */
    public List<Ticket> findByStatus(TicketStatus status) {
        try (Session session = sessionFactory.openSession()) {
            return session.createQuery(
                    "FROM Ticket t WHERE t.status = :status ORDER BY t.createdAt DESC",
                    Ticket.class)
                    .setParameter("status", status)
                    .list();
        }
    }

    /** Returns all tickets assigned to the given operator. */
    public List<Ticket> findByOperator(User operator) {
        try (Session session = sessionFactory.openSession()) {
            return session.createQuery(
                    "FROM Ticket t WHERE t.assignedOperator = :op ORDER BY t.createdAt DESC",
                    Ticket.class)
                    .setParameter("op", operator)
                    .list();
        }
    }

    /**
     * Returns all IN_PROGRESS tickets whose estimated completion time has passed —
     * these should be marked as DELAYED.
     */
    public List<Ticket> findOverdueInProgress() {
        try (Session session = sessionFactory.openSession()) {
            return session.createQuery(
                    "FROM Ticket t WHERE t.status = :status AND t.estimatedCompletionAt < :now",
                    Ticket.class)
                    .setParameter("status", TicketStatus.IN_PROGRESS)
                    .setParameter("now", LocalDateTime.now())
                    .list();
        }
    }

    /**
     * Fetches a ticket with its comments and logs eagerly loaded
     * (avoids LazyInitializationException in CLI context).
     */
    public java.util.Optional<Ticket> findByIdWithDetails(UUID id) {
        try (Session session = sessionFactory.openSession()) {
            // First load the ticket
            Ticket ticket = session.get(Ticket.class, id);
            if (ticket == null) return java.util.Optional.empty();
            // Force initialise lazy collections while session is still open
            ticket.getComments().size();
            ticket.getLogs().size();
            return java.util.Optional.of(ticket);
        }
    }

    /** Returns all tickets ordered by creation date descending. */
    @Override
    public List<Ticket> findAll() {
        try (Session session = sessionFactory.openSession()) {
            return session.createQuery(
                    "FROM Ticket t ORDER BY t.createdAt DESC", Ticket.class).list();
        }
    }

    /**
     * Bulk-updates a list of tickets to DELAYED status in a single transaction.
     */
    public int markAsDelayed(List<Ticket> tickets) {
        Transaction tx = null;
        try (Session session = sessionFactory.openSession()) {
            tx = session.beginTransaction();
            int count = 0;
            for (Ticket ticket : tickets) {
                Ticket managed = session.merge(ticket);
                managed.setStatus(TicketStatus.DELAYED);
                count++;
            }
            tx.commit();
            return count;
        } catch (Exception e) {
            rollback(tx);
            throw new RuntimeException("Błąd aktualizacji statusów: " + e.getMessage(), e);
        }
    }
}
