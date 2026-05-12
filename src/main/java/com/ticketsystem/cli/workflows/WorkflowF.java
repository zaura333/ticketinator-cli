package com.ticketsystem.cli.workflows;

import com.ticketsystem.cli.Workflow;
import com.ticketsystem.model.Ticket;
import com.ticketsystem.model.enums.TicketStatus;
import com.ticketsystem.service.TicketService;
import com.ticketsystem.util.ConsoleUtil;

import java.util.List;
import java.util.Scanner;

/**
 * Workflow F: Operator lub Administrator przegląda listę zleceń.
 * Możliwy filtr: wszystkie lub po wybranym statusie.
 */
public class WorkflowF implements Workflow {

    private final TicketService ticketService = new TicketService();

    @Override
    public void execute(Scanner scanner) {
        ConsoleUtil.printHeader("F — LISTA ZLECEŃ");

        // ── Step 1: choose filter ─────────────────────────────────────────────
        System.out.println("\n  Filtr:");
        System.out.println("  [1] Wszystkie zlecenia");
        System.out.println("  [2] Po statusie");
        System.out.println("  [0] Anuluj");

        int filter = ConsoleUtil.readInt(scanner, "Wybór", 0, 2);

        List<Ticket> tickets;
        String filterLabel;

        switch (filter) {
            case 0 -> {
                ConsoleUtil.printInfo("Operacja anulowana.");
                return;
            }
            case 1 -> {
                tickets = ticketService.findAll();
                filterLabel = "wszystkie";
            }
            case 2 -> {
                TicketStatus status = pickStatus(scanner);
                if (status == null) {
                    ConsoleUtil.printInfo("Operacja anulowana.");
                    return;
                }
                tickets = ticketService.findByStatus(status);
                filterLabel = "status: " + status.getDisplayName();
            }
            default -> {
                return;
            }
        }

        // ── Step 2: display ───────────────────────────────────────────────────
        printTicketTable(tickets, filterLabel);
        ConsoleUtil.pressEnterToContinue(scanner);
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private TicketStatus pickStatus(Scanner scanner) {
        System.out.println("\n  Wybierz status:");
        TicketStatus[] values = TicketStatus.values();
        for (int i = 0; i < values.length; i++) {
            System.out.printf("  [%d] %s%n", i + 1, values[i].getDisplayName());
        }
        System.out.println("  [0] Anuluj");

        int choice = ConsoleUtil.readInt(scanner, "Wybór", 0, values.length);
        if (choice == 0) return null;
        return values[choice - 1];
    }

    static void printTicketTable(List<Ticket> tickets, String filterLabel) {
        ConsoleUtil.printSection("Zlecenia (" + filterLabel + ") — łącznie: " + tickets.size());

        if (tickets.isEmpty()) {
            ConsoleUtil.printInfo("Brak zleceń spełniających kryteria.");
            return;
        }

        // Header
        System.out.printf("  %-36s  %-30s  %-15s  %s%n",
                "UUID", "Tytuł", "Status", "Klient");
        System.out.println("  " + "─".repeat(100));

        for (Ticket t : tickets) {
            System.out.println("  " + t.toListString());
        }

        System.out.println("  " + "─".repeat(100));
    }
}
