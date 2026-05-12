package com.ticketsystem.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * Audit log entry linked to a specific ticket via its UUID.
 * Every significant event (ticket created, status changed, comment added, etc.)
 * generates a TicketLog record.
 * Relation: many TicketLogs → one Ticket.
 */
@Entity
@Table(name = "ticket_logs")
public class TicketLog {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Id
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "VARCHAR(36)")
    private UUID id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ticket_id", nullable = false)
    private Ticket ticket;

    @Column(name = "event", columnDefinition = "TEXT", nullable = false)
    private String event;

    /** Username of the person who triggered this event, or "anonim". */
    @Column(name = "performed_by", nullable = false, length = 100)
    private String performedBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    @PrePersist
    protected void onCreate() {
        if (id == null) id = UUID.randomUUID();
        if (createdAt == null) createdAt = LocalDateTime.now();
    }

    // ── Constructors ──────────────────────────────────────────────────────────

    public TicketLog() {}

    public TicketLog(Ticket ticket, String event, String performedBy) {
        this.ticket = ticket;
        this.event = event;
        this.performedBy = performedBy;
    }

    // ── Getters & Setters ─────────────────────────────────────────────────────

    public UUID getId() { return id; }

    public Ticket getTicket() { return ticket; }
    public void setTicket(Ticket ticket) { this.ticket = ticket; }

    public String getEvent() { return event; }
    public void setEvent(String event) { this.event = event; }

    public String getPerformedBy() { return performedBy; }
    public void setPerformedBy(String performedBy) { this.performedBy = performedBy; }

    public LocalDateTime getCreatedAt() { return createdAt; }

    // ── Display ───────────────────────────────────────────────────────────────

    public String toDisplayString() {
        return String.format("[%s] %s — przez: %s",
                createdAt != null ? createdAt.format(FORMATTER) : "—",
                event,
                performedBy);
    }
}
