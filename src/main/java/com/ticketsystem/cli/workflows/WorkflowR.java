package com.ticketsystem.cli.workflows;

import com.ticketsystem.cli.Workflow;
import com.ticketsystem.model.Comment;
import com.ticketsystem.model.Ticket;
import com.ticketsystem.service.CommentService;
import com.ticketsystem.service.TicketService;
import com.ticketsystem.util.ConsoleUtil;

import java.util.List;
import java.util.Optional;
import java.util.Scanner;

/**
 * Workflow R: Przeglądanie komentarzy zlecenia po UUID.
 * Dostępne dla operatorów i administratorów.
 *
 * Kroki:
 * 1. Użytkownik podaje UUID zlecenia.
 * 2. System wyświetla nagłówek zlecenia (tytuł, status, klient).
 * 3. System wyświetla wszystkie komentarze z numerem, autorem, datą i treścią.
 */
public class WorkflowR implements Workflow {

    private final TicketService  ticketService  = new TicketService();
    private final CommentService commentService = new CommentService();

    @Override
    public void execute(Scanner scanner) {
        ConsoleUtil.printHeader("R — KOMENTARZE ZLECENIA");

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
        ConsoleUtil.println("UUID:    " + ticket.getId());
        ConsoleUtil.println("Tytuł:   " + ticket.getTitle());
        ConsoleUtil.println("Status:  " + ticket.getStatus().getDisplayName());
        ConsoleUtil.println("Klient:  " + (ticket.getClient() != null
                ? ticket.getClient().getFullName() : "—"));
        ConsoleUtil.println("Operator: " + (ticket.getAssignedOperator() != null
                ? ticket.getAssignedOperator().getUsername() : "—"));

        // ── Step 3: load and display comments ─────────────────────────────────
        List<Comment> comments = commentService.getCommentsForTicket(ticket);

        ConsoleUtil.printSection("Komentarze — łącznie: " + comments.size());

        if (comments.isEmpty()) {
            ConsoleUtil.printInfo("To zlecenie nie ma jeszcze żadnych komentarzy.");
        } else {
            for (int i = 0; i < comments.size(); i++) {
                System.out.println();
                System.out.println(comments.get(i).toDisplayString(i + 1));
            }
            System.out.println();
            System.out.println("  " + ConsoleUtil.THIN);
        }

        ConsoleUtil.pressEnterToContinue(scanner);
    }
}
