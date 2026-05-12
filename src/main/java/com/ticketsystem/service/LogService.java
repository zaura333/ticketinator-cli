package com.ticketsystem.service;

import com.ticketsystem.model.Ticket;
import com.ticketsystem.model.TicketLog;
import com.ticketsystem.repository.LogRepository;
import com.ticketsystem.util.SessionManager;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service for writing and reading ticket audit logs.
 * Every significant event should be recorded here so there is a full audit trail.
 */
public class LogService {

    private final LogRepository logRepository;

    public LogService() {
        this.logRepository = new LogRepository();
    }

    // ── Write ─────────────────────────────────────────────────────────────────

    /**
     * Records an event for the given ticket.
     * The actor is taken from the current session automatically.
     */
    public TicketLog log(Ticket ticket, String event) {
        String actor = SessionManager.getInstance().getActorName();
        TicketLog entry = new TicketLog(ticket, event, actor);
        return logRepository.save(entry);
    }

    /**
     * Records an event for the given ticket with an explicit actor name.
     * Useful when the session is not yet established (e.g., ticket creation by anonymous user).
     */
    public TicketLog log(Ticket ticket, String event, String actor) {
        TicketLog entry = new TicketLog(ticket, event, actor);
        return logRepository.save(entry);
    }

    // ── Read ──────────────────────────────────────────────────────────────────

    public List<TicketLog> getLogsForTicket(Ticket ticket) {
        return logRepository.findByTicket(ticket);
    }

    /**
     * Returns logs for a ticket within the given date-time range (inclusive).
     */
    public List<TicketLog> getLogsForTicketInRange(Ticket ticket,
                                                    LocalDateTime from,
                                                    LocalDateTime to) {
        return logRepository.findByTicketAndDateRange(ticket, from, to);
    }
}
