package com.ticketsystem.cli.workflows;

import com.ticketsystem.cli.Workflow;
import com.ticketsystem.model.Ticket;
import com.ticketsystem.model.User;
import com.ticketsystem.service.TicketService;
import com.ticketsystem.util.ConsoleUtil;
import com.ticketsystem.util.SessionManager;

import java.util.List;
import java.util.Scanner;

/**
 * Workflow K: Operator przegląda swoje zlecenia (przypisane do niego).
 */
public class WorkflowK implements Workflow {

    private final TicketService ticketService = new TicketService();

    @Override
    public void execute(Scanner scanner) {
        ConsoleUtil.printHeader("K — MOJE ZLECENIA");

        User currentUser = SessionManager.getInstance().getCurrentUser();

        List<Ticket> tickets = ticketService.findByOperator(currentUser);

        ConsoleUtil.printSection("Zlecenia przypisane do: " + currentUser.getUsername()
                + " — łącznie: " + tickets.size());

        if (tickets.isEmpty()) {
            ConsoleUtil.printInfo("Nie masz żadnych przypisanych zleceń.");
            ConsoleUtil.pressEnterToContinue(scanner);
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
        ConsoleUtil.pressEnterToContinue(scanner);
    }
}
