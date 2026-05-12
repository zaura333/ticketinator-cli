package com.ticketsystem.cli.workflows;

import com.ticketsystem.cli.Workflow;
import com.ticketsystem.model.Ticket;
import com.ticketsystem.service.LogService;
import com.ticketsystem.service.TicketService;
import com.ticketsystem.util.ConsoleUtil;
import com.ticketsystem.util.SessionManager;

import java.util.Optional;
import java.util.Scanner;

/**
 * Workflow B: Modyfikacja pól zlecenia (tytuł / opis).
 * Dostępne dla wszystkich — użytkownik wskazuje zlecenie po UUID.
 * Komentarze są obsługiwane oddzielnie przez Workflow E.
 */
public class WorkflowB implements Workflow {

    private final TicketService ticketService = new TicketService();
    private final LogService logService = new LogService();

    @Override
    public void execute(Scanner scanner) {
        ConsoleUtil.printHeader("B — MODYFIKACJA ZLECENIA (pola)");

        // ── Step 1: find ticket ───────────────────────────────────────────────
        String uuidInput = ConsoleUtil.readRequired(scanner, "Podaj UUID zlecenia");
        Optional<Ticket> ticketOpt = ticketService.findById(uuidInput);

        if (ticketOpt.isEmpty()) {
            ConsoleUtil.printError("Nie znaleziono zlecenia o podanym UUID.");
            ConsoleUtil.pressEnterToContinue(scanner);
            return;
        }

        Ticket ticket = ticketOpt.get();

        // ── Step 2: show current state ────────────────────────────────────────
        ConsoleUtil.printSection("Bieżące dane zlecenia");
        System.out.println(ticket.toDetailString());

        // Collect pending changes in local variables (not saved until confirmed)
        String pendingTitle = ticket.getTitle();
        String pendingDescription = ticket.getDescription();
        boolean anyChangeMade = false;

        // ── Step 3: edit loop ─────────────────────────────────────────────────
        boolean editing = true;
        while (editing) {
            ConsoleUtil.printSection("Co chcesz zmienić?");
            System.out.println("  [1] Tytuł       (bieżący: " + pendingTitle + ")");
            System.out.println("  [2] Opis         (bieżący: " + (pendingDescription != null ? pendingDescription : "—") + ")");
            System.out.println("  [3] Zakończ edycję i zapisz");
            System.out.println("  [4] Odrzuć zmiany i wyjdź");

            String choice = ConsoleUtil.readLine(scanner, "Wybór");

            switch (choice) {
                case "1" -> {
                    pendingTitle = ConsoleUtil.readRequired(scanner, "Nowy tytuł");
                    ConsoleUtil.printSuccess("Tytuł zaktualizowany lokalnie.");
                    anyChangeMade = true;
                }
                case "2" -> {
                    pendingDescription = ConsoleUtil.readOptional(scanner, "Nowy opis");
                    if (pendingDescription.isEmpty()) pendingDescription = null;
                    ConsoleUtil.printSuccess("Opis zaktualizowany lokalnie.");
                    anyChangeMade = true;
                }
                case "3" -> editing = false;
                case "4" -> {
                    ConsoleUtil.printInfo("Zmiany odrzucone. Zlecenie nie zostało zmodyfikowane.");
                    ConsoleUtil.pressEnterToContinue(scanner);
                    return;
                }
                default -> ConsoleUtil.printError("Nieprawidłowy wybór. Wpisz 1, 2, 3 lub 4.");
            }
        }

        // ── Step 4: save ──────────────────────────────────────────────────────
        if (!anyChangeMade) {
            ConsoleUtil.printInfo("Nie wprowadzono żadnych zmian.");
            ConsoleUtil.pressEnterToContinue(scanner);
            return;
        }

        try {
            String oldTitle = ticket.getTitle();
            String oldDesc  = ticket.getDescription();

            ticket.setTitle(pendingTitle);
            ticket.setDescription(pendingDescription);
            ticketService.updateTitle(ticket, pendingTitle);   // triggers save via update

            // Build log message for changed fields
            StringBuilder logMsg = new StringBuilder("Zmodyfikowano pola zlecenia:");
            if (!oldTitle.equals(pendingTitle)) {
                logMsg.append(" tytuł [").append(oldTitle).append(" → ").append(pendingTitle).append("]");
            }
            String safeOld = oldDesc != null ? oldDesc : "";
            String safeNew = pendingDescription != null ? pendingDescription : "";
            if (!safeOld.equals(safeNew)) {
                logMsg.append(" opis [zmieniony]");
            }

            logService.log(ticket, logMsg.toString());

            ConsoleUtil.printSuccess("Zlecenie zostało zaktualizowane.");

        } catch (Exception e) {
            ConsoleUtil.printError("Błąd zapisu: " + e.getMessage());
        }

        ConsoleUtil.pressEnterToContinue(scanner);
    }
}
