package com.ticketsystem.cli.workflows;

import com.ticketsystem.cli.Workflow;
import com.ticketsystem.model.Ticket;
import com.ticketsystem.model.User;
import com.ticketsystem.model.enums.TicketStatus;
import com.ticketsystem.service.LogService;
import com.ticketsystem.service.TicketService;
import com.ticketsystem.util.ConsoleUtil;
import com.ticketsystem.util.SessionManager;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Optional;
import java.util.Scanner;

/**
 * Workflow C: Operator rozpoczyna zlecenie.
 * Wymaga zalogowania jako Operator lub Administrator.
 *
 * Kroki:
 * 1. Operator podaje UUID zlecenia.
 * 2. System waliduje (zlecenie musi być w statusie NOWE).
 * 3. Operator podaje przewidywany czas zakończenia.
 * 4. System przypisuje operatora, ustawia status IN_PROGRESS i startedAt.
 */
public class WorkflowC implements Workflow {

    private static final DateTimeFormatter INPUT_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final TicketService ticketService = new TicketService();
    private final LogService logService = new LogService();

    @Override
    public void execute(Scanner scanner) {
        ConsoleUtil.printHeader("C — OPERATOR ROZPOCZYNA ZLECENIE");

        User currentUser = SessionManager.getInstance().getCurrentUser();

        // ── Step 1: find ticket ───────────────────────────────────────────────
        String uuidInput = ConsoleUtil.readRequired(scanner, "Podaj UUID zlecenia do rozpoczęcia");
        Optional<Ticket> ticketOpt = ticketService.findById(uuidInput);

        if (ticketOpt.isEmpty()) {
            ConsoleUtil.printError("Nie znaleziono zlecenia o podanym UUID.");
            ConsoleUtil.pressEnterToContinue(scanner);
            return;
        }

        Ticket ticket = ticketOpt.get();

        // ── Step 2: validate status ───────────────────────────────────────────
        if (ticket.getStatus() != TicketStatus.NEW) {
            ConsoleUtil.printError("Można rozpocząć tylko zlecenia ze statusem NOWE.");
            ConsoleUtil.printInfo("Bieżący status: " + ticket.getStatus().getDisplayName());
            ConsoleUtil.pressEnterToContinue(scanner);
            return;
        }

        // Show ticket summary
        ConsoleUtil.printSection("Zlecenie do rozpoczęcia");
        ConsoleUtil.println("UUID:    " + ticket.getId());
        ConsoleUtil.println("Tytuł:   " + ticket.getTitle());
        ConsoleUtil.println("Klient:  " + (ticket.getClient() != null ? ticket.getClient().getFullName() : "—"));
        ConsoleUtil.println("Status:  " + ticket.getStatus().getDisplayName());

        // ── Step 3: estimated completion time ─────────────────────────────────
        LocalDateTime estimatedCompletion = readEstimatedCompletion(scanner);
        if (estimatedCompletion == null) {
            ConsoleUtil.printInfo("Operacja anulowana.");
            ConsoleUtil.pressEnterToContinue(scanner);
            return;
        }

        // ── Step 4: confirm ───────────────────────────────────────────────────
        ConsoleUtil.println("Przewidywane zakończenie: " + estimatedCompletion.format(INPUT_FMT));
        if (!ConsoleUtil.confirm(scanner, "Rozpocząć zlecenie i przypisać je do siebie?")) {
            ConsoleUtil.printInfo("Operacja anulowana.");
            ConsoleUtil.pressEnterToContinue(scanner);
            return;
        }

        // ── Step 5: save ──────────────────────────────────────────────────────
        try {
            ticket = ticketService.startTicket(ticket, currentUser, estimatedCompletion);

            logService.log(ticket,
                    "Zlecenie rozpoczęte przez operatora: " + currentUser.getUsername()
                    + ". Przewidywane zakończenie: " + estimatedCompletion.format(INPUT_FMT));

            ConsoleUtil.printSuccess("Zlecenie zostało rozpoczęte pomyślnie!");
            ConsoleUtil.printInfo("Operator: " + currentUser.getUsername());
            ConsoleUtil.printInfo("Status:   " + ticket.getStatus().getDisplayName());
            ConsoleUtil.printInfo("Rozpoczęto: " + ticket.getStartedAt().format(INPUT_FMT));

        } catch (Exception e) {
            ConsoleUtil.printError("Błąd: " + e.getMessage());
        }

        ConsoleUtil.pressEnterToContinue(scanner);
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private LocalDateTime readEstimatedCompletion(Scanner scanner) {
        ConsoleUtil.println("Format daty: yyyy-MM-dd HH:mm  (np. 2025-12-31 17:00)");

        for (int attempt = 0; attempt < 3; attempt++) {
            String input = ConsoleUtil.readRequired(scanner, "Przewidywany czas zakończenia");
            try {
                LocalDateTime dt = LocalDateTime.parse(input, INPUT_FMT);
                if (dt.isBefore(LocalDateTime.now())) {
                    ConsoleUtil.printError("Data musi być w przyszłości.");
                    continue;
                }
                return dt;
            } catch (DateTimeParseException e) {
                ConsoleUtil.printError("Nieprawidłowy format daty. Użyj: yyyy-MM-dd HH:mm");
            }
        }

        ConsoleUtil.printError("Przekroczono liczbę prób podania daty.");
        return null;
    }
}
