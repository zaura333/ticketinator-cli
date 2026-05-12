package com.ticketsystem.repository;

import com.ticketsystem.model.Comment;
import com.ticketsystem.model.Ticket;
import org.hibernate.Session;

import java.util.List;
import java.util.UUID;

/**
 * Repository for {@link Comment} entities.
 */
public class CommentRepository extends AbstractRepository<Comment, UUID> {

    public CommentRepository() {
        super(Comment.class);
    }

    /** Returns all comments for a specific ticket, ordered by creation date. */
    public List<Comment> findByTicket(Ticket ticket) {
        try (Session session = sessionFactory.openSession()) {
            return session.createQuery(
                    "FROM Comment c WHERE c.ticket = :ticket ORDER BY c.createdAt ASC",
                    Comment.class)
                    .setParameter("ticket", ticket)
                    .list();
        }
    }
}
