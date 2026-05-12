package com.ticketsystem.cli.workflows;

import com.ticketsystem.cli.Workflow;
import com.ticketsystem.model.Ticket;
import com.ticketsystem.model.User;
import com.ticketsystem.model.enums.TicketStatus;
import com.ticketsystem.service.LogService;
import com.ticketsystem.service.TicketService;
import com.ticketsystem.util.ConsoleUtil;
import com.ticketsystem.util.SessionManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

/**
 * Workflow D: Operator/Administrator zmienia status zlecenia.
 *
 * Reguły przejść statusów:
 *  - Operator: może zmieniać tylko zlecenia przypisane do siebie.
 *              IN_PROGRESS / DELAYED → COMPLETED, CANCELLED
 *  - Administrator: może zmieniać dowolne zlecenie na dowolny status.
 */
public class WorkflowD implements Workflow {

    private final TicketService ticketService = new TicketService();
    private final LogService logService = new LogService();

    @Override
    public void execute(Scanner scanner) {
        ConsoleUtil.printHeader("D — ZMIANA STATUSU ZLECENIA");

        SessionManager session = SessionManager.getInstance();
        User currentUser = session.getCurrentUser();
        boolean isAdmin = session.isAdministrator();

        // ── Step 1: find ticket ───────────────────────────────────────────────
        String uuidInput = ConsoleUtil.readRequired(scanner, "Podaj UUID zlecenia");
        Optional<Ticket> ticketOpt = ticketService.findById(uuidInput);

        if (ticketOpt.isEmpty()) {
            ConsoleUtil.printError("Nie znaleziono zlecenia o podanym UUID.");
            ConsoleUtil.pressEnterToContinue(scanner);
            return;
        }

        Ticket ticket = ticketOpt.get();

        // ── Step 2: access control for operators ──────────────────────────────
        if (!isAdmin) {
            User assignedOp = ticket.getAssignedOperator();
            if (assignedOp == null || !assignedOp.getId().equals(currentUser.getId())) {
                ConsoleUtil.printError("Możesz zmieniać status tylko zleceń przypisanych do Ciebie.");
                ConsoleUtil.pressEnterToContinue(scanner);
                return;
            }
        }

        // ── Step 3: show current status and available transitions ─────────────
        ConsoleUtil.printSection("Zlecenie: " + ticket.getTitle());
        ConsoleUtil.println("Bieżący status: " + ticket.getStatus().getDisplayName());

        List<TicketStatus> available = getAvailableTransitions(ticket.getStatus(), isAdmin);

        if (available.isEmpty()) {
            ConsoleUtil.printInfo("Brak dostępnych przejść statusu dla tego zlecenia.");
            ConsoleUtil.pressEnterToContinue(scanner);
            return;
        }

        System.out.println("\n  Dostępne nowe statusy:");
        for (int i = 0; i < available.size(); i++) {
            System.out.printf("  [%d] %s%n", i + 1, available.get(i).getDisplayName());
        }
        System.out.println("  [0] Anuluj");

        int choice = ConsoleUtil.readInt(scanner, "Wybór", 0, available.size());
        if (choice == 0) {
            ConsoleUtil.printInfo("Operacja anulowana.");
            ConsoleUtil.pressEnterToContinue(scanner);
            return;
        }

        TicketStatus newStatus = available.get(choice - 1);

        // ── Step 4: confirm ───────────────────────────────────────────────────
        if (!ConsoleUtil.confirm(scanner,
                "Zmienić status na '" + newStatus.getDisplayName() + "'?")) {
            ConsoleUtil.printInfo("Operacja anulowana.");
            ConsoleUtil.pressEnterToContinue(scanner);
            return;
        }

        // ── Step 5: save ──────────────────────────────────────────────────────
        try {
            TicketStatus oldStatus = ticket.getStatus();
            ticket = ticketService.changeStatus(ticket, newStatus);

            logService.log(ticket,
                    "Status zmieniony: " + oldStatus.getDisplayName()
                    + " → " + newStatus.getDisplayName()
                    + " przez: " + currentUser.getUsername());

            ConsoleUtil.printSuccess("Status zmieniony na: " + ticket.getStatus().getDisplayName());
            if (ticket.getCompletedAt() != null) {
                ConsoleUtil.printInfo("Czas zakończenia: " + ticket.getCompletedAt());
            }

        } catch (Exception e) {
            ConsoleUtil.printError("Błąd: " + e.getMessage());
        }

        ConsoleUtil.pressEnterToContinue(scanner);
    }

    // ── Transition rules ──────────────────────────────────────────────────────

    private List<TicketStatus> getAvailableTransitions(TicketStatus current, boolean isAdmin) {
        List<TicketStatus> transitions = new ArrayList<>();

        if (isAdmin) {
            // Admin can set any status except the current one
            for (TicketStatus s : TicketStatus.values()) {
                if (s != current) transitions.add(s);
            }
        } else {
            // Operator: restricted transitions
            switch (current) {
                case IN_PROGRESS, DELAYED -> {
                    transitions.add(TicketStatus.COMPLETED);
                    transitions.add(TicketStatus.CANCELLED);
                }
                // NEW → only startable via Workflow C
                // COMPLETED, CANCELLED → terminal states for operator
                default -> { /* no transitions */ }
            }
        }

        return transitions;
    }
}
