package com.ticketsystem.repository;

import com.ticketsystem.model.Ticket;
import com.ticketsystem.model.TicketLog;
import org.hibernate.Session;

import java.util.List;
import java.util.UUID;

/**
 * Repository for {@link TicketLog} audit entries.
 */
public class LogRepository extends AbstractRepository<TicketLog, UUID> {

    public LogRepository() {
        super(TicketLog.class);
    }

    /** Returns all logs for a specific ticket, ordered by creation date ascending. */
    public List<TicketLog> findByTicket(Ticket ticket) {
        try (Session session = sessionFactory.openSession()) {
            return session.createQuery(
                    "FROM TicketLog l WHERE l.ticket = :ticket ORDER BY l.createdAt ASC",
                    TicketLog.class)
                    .setParameter("ticket", ticket)
                    .list();
        }
    }
}
