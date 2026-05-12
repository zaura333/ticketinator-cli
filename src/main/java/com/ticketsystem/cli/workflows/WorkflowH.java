package com.ticketsystem.cli.workflows;

import com.ticketsystem.cli.Workflow;
import com.ticketsystem.model.Comment;
import com.ticketsystem.model.Ticket;
import com.ticketsystem.service.CommentService;
import com.ticketsystem.service.LogService;
import com.ticketsystem.service.TicketService;
import com.ticketsystem.util.ConsoleUtil;
import com.ticketsystem.util.SessionManager;

import java.util.List;
import java.util.Optional;
import java.util.Scanner;

/**
 * Workflow H: Administrator modyfikuje lub usuwa komentarz.
 *
 * Kroki:
 * 1. Admin podaje UUID zlecenia.
 * 2. System wyświetla komentarze (ponumerowane).
 * 3. Admin wybiera komentarz i akcję: [1] Edytuj [2] Usuń.
 * 4. System wykonuje operację i loguje zdarzenie.
 */
public class WorkflowH implements Workflow {

    private final TicketService ticketService = new TicketService();
    private final CommentService commentService = new CommentService();
    private final LogService logService = new LogService();

    @Override
    public void execute(Scanner scanner) {
        ConsoleUtil.printHeader("H — MODYFIKACJA / USUNIĘCIE KOMENTARZA (ADMIN)");

        // ── Step 1: find ticket ───────────────────────────────────────────────
        String uuidInput = ConsoleUtil.readRequired(scanner, "Podaj UUID zlecenia");
        Optional<Ticket> ticketOpt = ticketService.findById(uuidInput);

        if (ticketOpt.isEmpty()) {
            ConsoleUtil.printError("Nie znaleziono zlecenia o podanym UUID.");
            ConsoleUtil.pressEnterToContinue(scanner);
            return;
        }

        Ticket ticket = ticketOpt.get();
        List<Comment> comments = commentService.getCommentsForTicket(ticket);

        // ── Step 2: show comments ─────────────────────────────────────────────
        ConsoleUtil.printSection("Komentarze zlecenia: " + ticket.getTitle());

        if (comments.isEmpty()) {
            ConsoleUtil.printInfo("Brak komentarzy dla tego zlecenia.");
            ConsoleUtil.pressEnterToContinue(scanner);
            return;
        }

        for (int i = 0; i < comments.size(); i++) {
            System.out.println();
            System.out.println(comments.get(i).toDisplayString(i + 1));
        }

        // ── Step 3: pick comment ──────────────────────────────────────────────
        System.out.println();
        int pick = ConsoleUtil.readInt(scanner, "Numer komentarza do edycji/usunięcia (0 = anuluj)",
                0, comments.size());

        if (pick == 0) {
            ConsoleUtil.printInfo("Operacja anulowana.");
            ConsoleUtil.pressEnterToContinue(scanner);
            return;
        }

        Comment selected = comments.get(pick - 1);

        // ── Step 4: choose action ─────────────────────────────────────────────
        System.out.println("\n  Akcja:");
        System.out.println("  [1] Edytuj treść komentarza");
        System.out.println("  [2] Usuń komentarz");
        System.out.println("  [0] Anuluj");

        int action = ConsoleUtil.readInt(scanner, "Wybór", 0, 2);

        switch (action) {
            case 0 -> {
                ConsoleUtil.printInfo("Operacja anulowana.");
            }
            case 1 -> editComment(scanner, ticket, selected);
            case 2 -> deleteComment(scanner, ticket, selected);
        }

        ConsoleUtil.pressEnterToContinue(scanner);
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private void editComment(Scanner scanner, Ticket ticket, Comment comment) {
        ConsoleUtil.printSection("Edycja komentarza");
        ConsoleUtil.println("Bieżąca treść: " + comment.getContent());

        String newContent = ConsoleUtil.readRequired(scanner, "Nowa treść");

        if (!ConsoleUtil.confirm(scanner, "Zapisać zmiany?")) {
            ConsoleUtil.printInfo("Anulowano.");
            return;
        }

        try {
            commentService.updateContent(comment, newContent);
            logService.log(ticket, "Administrator " + SessionManager.getInstance().getActorName()
                    + " edytował komentarz ID: " + comment.getId());
            ConsoleUtil.printSuccess("Komentarz zaktualizowany.");
        } catch (Exception e) {
            ConsoleUtil.printError("Błąd edycji: " + e.getMessage());
        }
    }

    private void deleteComment(Scanner scanner, Ticket ticket, Comment comment) {
        ConsoleUtil.printWarning("Usunięcie komentarza jest nieodwracalne!");
        ConsoleUtil.println("Treść: " + comment.getContent());

        if (!ConsoleUtil.confirm(scanner, "Potwierdzasz usunięcie?")) {
            ConsoleUtil.printInfo("Anulowano.");
            return;
        }

        try {
            commentService.deleteComment(comment);
            logService.log(ticket, "Administrator " + SessionManager.getInstance().getActorName()
                    + " usunął komentarz ID: " + comment.getId());
            ConsoleUtil.printSuccess("Komentarz został usunięty.");
        } catch (Exception e) {
            ConsoleUtil.printError("Błąd usunięcia: " + e.getMessage());
        }
    }
}
