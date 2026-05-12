package com.ticketsystem.cli.workflows;

import com.ticketsystem.cli.Workflow;
import com.ticketsystem.model.Ticket;
import com.ticketsystem.model.TicketLog;
import com.ticketsystem.service.LogService;
import com.ticketsystem.service.TicketService;
import com.ticketsystem.util.ConsoleUtil;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

/**
 * Workflow S: Przeglądanie logów audytowych zlecenia po UUID.
 * Dostępne wyłącznie dla administratorów.
 *
 * Kroki:
 * 1. Użytkownik podaje UUID zlecenia.
 * 2. System wyświetla nagłówek zlecenia.
 * 3. Użytkownik wybiera: wszystkie logi lub zakres dat.
 * 4. System wyświetla pasujące logi z datą, zdarzeniem i wykonawcą.
 */
public class WorkflowS implements Workflow {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final TicketService ticketService = new TicketService();
    private final LogService    logService    = new LogService();

    @Override
    public void execute(Scanner scanner) {
        ConsoleUtil.printHeader("S — LOGI AUDYTOWE ZLECENIA");

        // ── Step 1: find ticket ───────────────────────────────────────────────
        String uuidInput = ConsoleUtil.readRequired(scanner, "Podaj UUID zlecenia");
        Optional<Ticket> ticketOpt = ticketService.findById(uuidInput);

        if (ticketOpt.isEmpty()) {
            ConsoleUtil.printError("Nie znaleziono zlecenia o podanym UUID.");
            ConsoleUtil.pressEnterToContinue(scanner);
            return;
        }

        Ticket ticket = ticketOpt.get();

        // ── Step 2: ticket context ────────────────────────────────────────────
        ConsoleUtil.printSection("Zlecenie");
        ConsoleUtil.println("UUID:     " + ticket.getId());
        ConsoleUtil.println("Tytuł:    " + ticket.getTitle());
        ConsoleUtil.println("Status:   " + ticket.getStatus().getDisplayName());
        ConsoleUtil.println("Klient:   " + (ticket.getClient() != null
                ? ticket.getClient().getFullName() : "—"));
        ConsoleUtil.println("Operator: " + (ticket.getAssignedOperator() != null
                ? ticket.getAssignedOperator().getUsername() : "—"));

        // ── Step 3: choose filter mode ────────────────────────────────────────
        System.out.println();
        System.out.println("  Zakres logów:");
        System.out.println("  [1] Wszystkie logi");
        System.out.println("  [2] Podaj zakres dat");
        System.out.println("  [0] Anuluj");

        int mode = ConsoleUtil.readInt(scanner, "Wybór", 0, 2);

        if (mode == 0) {
            ConsoleUtil.printInfo("Operacja anulowana.");
            ConsoleUtil.pressEnterToContinue(scanner);
            return;
        }

        // ── Step 4: fetch logs ────────────────────────────────────────────────
        List<TicketLog> logs;
        String rangeLabel;

        if (mode == 1) {
            logs = logService.getLogsForTicket(ticket);
            rangeLabel = "wszystkie";
        } else {
            LocalDate fromDate = readDate(scanner, "Data od (yyyy-MM-dd)");
            if (fromDate == null) {
                ConsoleUtil.pressEnterToContinue(scanner);
                return;
            }

            LocalDate toDate = readDate(scanner, "Data do (yyyy-MM-dd)");
            if (toDate == null) {
                ConsoleUtil.pressEnterToContinue(scanner);
                return;
            }

            if (toDate.isBefore(fromDate)) {
                ConsoleUtil.printError("Data końcowa nie może być wcześniejsza niż data początkowa.");
                ConsoleUtil.pressEnterToContinue(scanner);
                return;
            }

            LocalDateTime from = fromDate.atStartOfDay();
            LocalDateTime to   = toDate.atTime(23, 59, 59);

            logs = logService.getLogsForTicketInRange(ticket, from, to);
            rangeLabel = fromDate.format(DATE_FMT) + " — " + toDate.format(DATE_FMT);
        }

        // ── Step 5: display ───────────────────────────────────────────────────
        ConsoleUtil.printSection("Logi audytowe (" + rangeLabel + ") — łącznie: " + logs.size());

        if (logs.isEmpty()) {
            ConsoleUtil.printInfo("Brak logów spełniających podane kryteria.");
        } else {
            for (TicketLog log : logs) {
                ConsoleUtil.println(log.toDisplayString());
            }
            System.out.println();
            System.out.println("  " + ConsoleUtil.THIN);
        }

        ConsoleUtil.pressEnterToContinue(scanner);
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    /**
     * Reads and parses a date in yyyy-MM-dd format.
     * Gives the user three attempts before aborting.
     */
    private LocalDate readDate(Scanner scanner, String prompt) {
        for (int attempt = 1; attempt <= 3; attempt++) {
            String input = ConsoleUtil.readRequired(scanner, prompt);
            try {
                return LocalDate.parse(input, DATE_FMT);
            } catch (DateTimeParseException e) {
                ConsoleUtil.printError("Nieprawidłowy format daty. Użyj formatu: yyyy-MM-dd (np. 2026-01-15).");
                if (attempt == 3) {
                    ConsoleUtil.printError("Przekroczono liczbę prób. Operacja anulowana.");
                }
            }
        }
        return null;
    }
}
