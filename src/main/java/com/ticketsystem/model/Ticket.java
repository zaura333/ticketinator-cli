package com.ticketsystem.model;

import com.ticketsystem.model.enums.TicketStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Central entity of the system — a support ticket.
 * Lifecycle: NEW → IN_PROGRESS → COMPLETED / CANCELLED
 *                             ↕
 *                          DELAYED (set automatically when past estimated time)
 */
@Entity
@Table(name = "tickets")
public class Ticket {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @Id
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "VARCHAR(36)")
    private UUID id;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private TicketStatus status = TicketStatus.NEW;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "client_id")
    private Client client;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "operator_id")
    private User assignedOperator;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "estimated_completion_at")
    private LocalDateTime estimatedCompletionAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL,
               fetch = FetchType.LAZY, orphanRemoval = true)
    @OrderBy("createdAt ASC")
    private List<Comment> comments = new ArrayList<>();

    @OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL,
               fetch = FetchType.LAZY, orphanRemoval = true)
    @OrderBy("createdAt ASC")
    private List<TicketLog> logs = new ArrayList<>();

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    @PrePersist
    protected void onCreate() {
        if (id == null) id = UUID.randomUUID();
        if (createdAt == null) createdAt = LocalDateTime.now();
    }

    // ── Constructors ──────────────────────────────────────────────────────────

    public Ticket() {}

    public Ticket(String title, String description, Client client) {
        this.title = title;
        this.description = description;
        this.client = client;
        this.status = TicketStatus.NEW;
    }

    // ── Business helpers ──────────────────────────────────────────────────────

    public boolean isAssigned() {
        return assignedOperator != null;
    }

    public boolean isOverdue() {
        return estimatedCompletionAt != null
                && LocalDateTime.now().isAfter(estimatedCompletionAt)
                && status == TicketStatus.IN_PROGRESS;
    }

    // ── Getters & Setters ─────────────────────────────────────────────────────

    public UUID getId() { return id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public TicketStatus getStatus() { return status; }
    public void setStatus(TicketStatus status) { this.status = status; }

    public Client getClient() { return client; }
    public void setClient(Client client) { this.client = client; }

    public User getAssignedOperator() { return assignedOperator; }
    public void setAssignedOperator(User assignedOperator) { this.assignedOperator = assignedOperator; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getStartedAt() { return startedAt; }
    public void setStartedAt(LocalDateTime startedAt) { this.startedAt = startedAt; }

    public LocalDateTime getEstimatedCompletionAt() { return estimatedCompletionAt; }
    public void setEstimatedCompletionAt(LocalDateTime estimatedCompletionAt) {
        this.estimatedCompletionAt = estimatedCompletionAt;
    }

    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }

    public List<Comment> getComments() { return comments; }
    public void setComments(List<Comment> comments) { this.comments = comments; }

    public List<TicketLog> getLogs() { return logs; }
    public void setLogs(List<TicketLog> logs) { this.logs = logs; }

    // ── Display ───────────────────────────────────────────────────────────────

    private String fmt(LocalDateTime dt) {
        return dt != null ? dt.format(FORMATTER) : "—";
    }

    public String toDetailString() {
        return String.format("""
                UUID:              %s
                Tytuł:             %s
                Opis:              %s
                Status:            %s
                Klient:            %s
                Operator:          %s
                Dodano:            %s
                Rozpoczęto:        %s
                Planowane zak.:    %s
                Faktyczne zak.:    %s""",
                id,
                title,
                description != null ? description : "—",
                status.getDisplayName(),
                client != null ? client.getFullName() + " (ID: " + client.getId() + ")" : "—",
                assignedOperator != null ? assignedOperator.getUsername() : "—",
                fmt(createdAt),
                fmt(startedAt),
                fmt(estimatedCompletionAt),
                fmt(completedAt));
    }

    public String toListString() {
        return String.format("%-36s  %-30s  %-15s  %s",
                id,
                title.length() > 30 ? title.substring(0, 27) + "..." : title,
                status.getDisplayName(),
                client != null ? client.getFullName() : "—");
    }
}
