package com.ticketsystem.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * A comment attached to a ticket.
 * Can be added by anyone (operator, admin, or anonymous user).
 * Relation: many Comments → one Ticket.
 */
@Entity
@Table(name = "comments")
public class Comment {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @Id
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "VARCHAR(36)")
    private UUID id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ticket_id", nullable = false)
    private Ticket ticket;

    /** Username (if logged in) or provided name (if anonymous). */
    @Column(name = "author_name", nullable = false, length = 100)
    private String authorName;

    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    @PrePersist
    protected void onCreate() {
        if (id == null) id = UUID.randomUUID();
        if (createdAt == null) createdAt = LocalDateTime.now();
    }

    // ── Constructors ──────────────────────────────────────────────────────────

    public Comment() {}

    public Comment(Ticket ticket, String authorName, String content) {
        this.ticket = ticket;
        this.authorName = authorName;
        this.content = content;
    }

    // ── Getters & Setters ─────────────────────────────────────────────────────

    public UUID getId() { return id; }

    public Ticket getTicket() { return ticket; }
    public void setTicket(Ticket ticket) { this.ticket = ticket; }

    public String getAuthorName() { return authorName; }
    public void setAuthorName(String authorName) { this.authorName = authorName; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public LocalDateTime getCreatedAt() { return createdAt; }

    // ── Display ───────────────────────────────────────────────────────────────

    public String toDisplayString(int index) {
        return String.format("[%d] %s (%s):%n    %s",
                index,
                authorName,
                createdAt != null ? createdAt.format(FORMATTER) : "—",
                content);
    }
}
