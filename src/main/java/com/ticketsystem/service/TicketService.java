package com.ticketsystem.service;

import com.ticketsystem.model.Client;
import com.ticketsystem.model.Ticket;
import com.ticketsystem.model.User;
import com.ticketsystem.model.enums.TicketStatus;
import com.ticketsystem.repository.TicketRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Core business logic for ticket lifecycle management.
 */
public class TicketService {

    private final TicketRepository ticketRepository;

    public TicketService() {
        this.ticketRepository = new TicketRepository();
    }

    // ── Create ────────────────────────────────────────────────────────────────

    public Ticket createTicket(String title, String description, Client client) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Tytuł zlecenia nie może być pusty.");
        }
        Ticket ticket = new Ticket(title, description, client);
        return ticketRepository.save(ticket);
    }

    // ── Read ──────────────────────────────────────────────────────────────────

    public Optional<Ticket> findById(UUID id) {
        return ticketRepository.findById(id);
    }

    /** Finds ticket by ID and eagerly loads comments and logs. */
    public Optional<Ticket> findByIdWithDetails(UUID id) {
        return ticketRepository.findByIdWithDetails(id);
    }

    public Optional<Ticket> findById(String idString) {
        try {
            UUID uuid = UUID.fromString(idString.trim());
            return ticketRepository.findByIdWithDetails(uuid);
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    public List<Ticket> findAll() {
        return ticketRepository.findAll();
    }

    public List<Ticket> findByStatus(TicketStatus status) {
        return ticketRepository.findByStatus(status);
    }

    public List<Ticket> findByOperator(User operator) {
        return ticketRepository.findByOperator(operator);
    }

    // ── Update fields ─────────────────────────────────────────────────────────

    public Ticket updateTitle(Ticket ticket, String newTitle) {
        if (newTitle == null || newTitle.isBlank()) {
            throw new IllegalArgumentException("Tytuł nie może być pusty.");
        }
        ticket.setTitle(newTitle);
        return ticketRepository.update(ticket);
    }

    public Ticket updateDescription(Ticket ticket, String newDescription) {
        ticket.setDescription(newDescription);
        return ticketRepository.update(ticket);
    }

    // ── Operator workflows ────────────────────────────────────────────────────

    /**
     * Operator starts a ticket: assigns themselves, sets status to IN_PROGRESS,
     * records startedAt and the estimated completion time.
     */
    public Ticket startTicket(Ticket ticket, User operator, LocalDateTime estimatedCompletion) {
        if (ticket.getStatus() != TicketStatus.NEW) {
            throw new IllegalStateException(
                    "Można rozpocząć tylko zlecenia ze statusem NOWE. " +
                    "Bieżący status: " + ticket.getStatus().getDisplayName());
        }
        ticket.setAssignedOperator(operator);
        ticket.setStatus(TicketStatus.IN_PROGRESS);
        ticket.setStartedAt(LocalDateTime.now());
        ticket.setEstimatedCompletionAt(estimatedCompletion);
        return ticketRepository.update(ticket);
    }

    /**
     * Changes the status of a ticket.
     * When changing to COMPLETED, sets completedAt automatically.
     */
    public Ticket changeStatus(Ticket ticket, TicketStatus newStatus) {
        ticket.setStatus(newStatus);
        if (newStatus == TicketStatus.COMPLETED && ticket.getCompletedAt() == null) {
            ticket.setCompletedAt(LocalDateTime.now());
        }
        return ticketRepository.update(ticket);
    }

    // ── Admin workflows ───────────────────────────────────────────────────────

    /**
     * Scans all IN_PROGRESS tickets and marks overdue ones as DELAYED.
     *
     * @return number of tickets updated
     */
    public int refreshDelayedStatuses() {
        List<Ticket> overdue = ticketRepository.findOverdueInProgress();
        if (overdue.isEmpty()) return 0;
        return ticketRepository.markAsDelayed(overdue);
    }

    public List<Ticket> findOverdueInProgress() {
        return ticketRepository.findOverdueInProgress();
    }

    // ── Delete ────────────────────────────────────────────────────────────────

    public void deleteTicket(Ticket ticket) {
        ticketRepository.delete(ticket);
    }
}
