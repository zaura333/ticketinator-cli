package com.ticketsystem.cli.workflows;

import com.ticketsystem.cli.Workflow;
import com.ticketsystem.model.Comment;
import com.ticketsystem.model.Ticket;
import com.ticketsystem.model.TicketLog;
import com.ticketsystem.service.CommentService;
import com.ticketsystem.service.LogService;
import com.ticketsystem.service.TicketService;
import com.ticketsystem.util.ConsoleUtil;
import com.ticketsystem.util.SessionManager;

import java.util.List;
import java.util.Optional;
import java.util.Scanner;

/**
 * Workflow P: Podejrzenie zlecenia po UUID.
 * Dostępne dla wszystkich. Wyświetla pełne dane zlecenia,
 * opcjonalnie komentarze i (dla zalogowanych) logi.
 */
public class WorkflowP implements Workflow {

    private final TicketService ticketService  = new TicketService();
    private final CommentService commentService = new CommentService();
    private final LogService logService         = new LogService();

    @Override
    public void execute(Scanner scanner) {
        ConsoleUtil.printHeader("P — PODEJRZENIE ZLECENIA");

        // ── Step 1: find ticket ───────────────────────────────────────────────
        String uuidInput = ConsoleUtil.readRequired(scanner, "Podaj UUID zlecenia");
        Optional<Ticket> ticketOpt = ticketService.findById(uuidInput);

        if (ticketOpt.isEmpty()) {
            ConsoleUtil.printError("Nie znaleziono zlecenia o podanym UUID.");
            ConsoleUtil.pressEnterToContinue(scanner);
            return;
        }

        Ticket ticket = ticketOpt.get();

        // ── Step 2: main details ──────────────────────────────────────────────
        ConsoleUtil.printSection("Szczegóły zlecenia");
        System.out.println(ticket.toDetailString());

        // ── Step 3: comments (optional) ───────────────────────────────────────
        List<Comment> comments = commentService.getCommentsForTicket(ticket);

        System.out.println();
        ConsoleUtil.println("Komentarze: " + comments.size());

        if (!comments.isEmpty()
                && ConsoleUtil.confirm(scanner, "Wyświetlić komentarze?")) {
            ConsoleUtil.printSection("Komentarze");
            for (int i = 0; i < comments.size(); i++) {
                System.out.println();
                System.out.println(comments.get(i).toDisplayString(i + 1));
            }
        }

        // ── Step 4: audit logs (only for logged-in users) ─────────────────────
        if (SessionManager.getInstance().isAtLeastOperator()) {
            List<TicketLog> logs = logService.getLogsForTicket(ticket);
            ConsoleUtil.println("Logi audytu: " + logs.size());

            if (!logs.isEmpty()
                    && ConsoleUtil.confirm(scanner, "Wyświetlić logi audytu?")) {
                ConsoleUtil.printSection("Logi");
                for (TicketLog log : logs) {
                    ConsoleUtil.println(log.toDisplayString());
                }
            }
        }

        ConsoleUtil.pressEnterToContinue(scanner);
    }
}
