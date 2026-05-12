package com.ticketsystem.cli.workflows;

import com.ticketsystem.cli.Workflow;
import com.ticketsystem.model.Ticket;
import com.ticketsystem.service.LogService;
import com.ticketsystem.service.TicketService;
import com.ticketsystem.util.ConsoleUtil;
import com.ticketsystem.util.SessionManager;

import java.util.List;
import java.util.Scanner;

/**
 * Workflow J: Administrator odświeża statusy zleceń — oznacza jako OPÓŹNIONE
 * wszystkie zlecenia IN_PROGRESS, których przewidywany czas zakończenia minął.
 *
 * Zaprojektowane tak, aby w przyszłości można było uruchomić jako daily job / cron.
 */
public class WorkflowJ implements Workflow {

    private final TicketService ticketService = new TicketService();
    private final LogService logService = new LogService();

    @Override
    public void execute(Scanner scanner) {
        ConsoleUtil.printHeader("J — ODŚWIEŻENIE STATUSÓW (OPÓŹNIENIA)");

        // ── Step 1: find overdue tickets ──────────────────────────────────────
        List<Ticket> overdue = ticketService.findOverdueInProgress();

        if (overdue.isEmpty()) {
            ConsoleUtil.printSuccess("Brak opóźnionych zleceń. Wszystkie zlecenia są na czas.");
            ConsoleUtil.pressEnterToContinue(scanner);
            return;
        }

        // ── Step 2: show what will be updated ────────────────────────────────
        ConsoleUtil.printSection("Zlecenia do oznaczenia jako OPÓŹNIONE (" + overdue.size() + ")");
        for (Ticket t : overdue) {
            ConsoleUtil.println("  UUID: " + t.getId()
                    + "  |  " + t.getTitle()
                    + "  |  Planowane: " + t.getEstimatedCompletionAt());
        }

        // ── Step 3: confirm ───────────────────────────────────────────────────
        if (!ConsoleUtil.confirm(scanner,
                "Oznaczyć " + overdue.size() + " zlecenie(ń) jako OPÓŹNIONE?")) {
            ConsoleUtil.printInfo("Operacja anulowana.");
            ConsoleUtil.pressEnterToContinue(scanner);
            return;
        }

        // ── Step 4: bulk update ───────────────────────────────────────────────
        try {
            int updated = ticketService.refreshDelayedStatuses();

            // Log each updated ticket
            String actor = SessionManager.getInstance().getActorName();
            // Re-fetch to get updated state for logging
            for (Ticket t : overdue) {
                logService.log(t,
                        "Status automatycznie zmieniony na OPÓŹNIONE "
                        + "(przekroczono przewidywany czas zakończenia). "
                        + "Wyzwolone przez: " + actor,
                        actor);
            }

            ConsoleUtil.printSuccess("Zaktualizowano " + updated + " zlecenie(ń) na status OPÓŹNIONE.");

        } catch (Exception e) {
            ConsoleUtil.printError("Błąd aktualizacji: " + e.getMessage());
        }

        ConsoleUtil.pressEnterToContinue(scanner);
    }
}
