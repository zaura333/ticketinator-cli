package com.ticketsystem.model;

import com.ticketsystem.model.enums.UserRole;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Operator — can start tickets, change their statuses, add comments,
 * and view the full ticket list.
 */
@Entity
@DiscriminatorValue("OPERATOR")
public class Operator extends User {

    @OneToMany(mappedBy = "assignedOperator", fetch = FetchType.LAZY)
    private List<Ticket> assignedTickets = new ArrayList<>();

    // ── Constructors ──────────────────────────────────────────────────────────

    public Operator() {}

    public Operator(String username, String password) {
        super(username, password);
    }

    // ── Role ──────────────────────────────────────────────────────────────────

    @Override
    public UserRole getRole() {
        return UserRole.OPERATOR;
    }

    // ── Getters & Setters ─────────────────────────────────────────────────────

    public List<Ticket> getAssignedTickets() { return assignedTickets; }
    public void setAssignedTickets(List<Ticket> assignedTickets) { this.assignedTickets = assignedTickets; }
}
