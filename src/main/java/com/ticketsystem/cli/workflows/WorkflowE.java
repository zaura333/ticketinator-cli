package com.ticketsystem.cli.workflows;

import com.ticketsystem.cli.Workflow;
import com.ticketsystem.model.Comment;
import com.ticketsystem.model.Ticket;
import com.ticketsystem.service.CommentService;
import com.ticketsystem.service.LogService;
import com.ticketsystem.service.TicketService;
import com.ticketsystem.util.ConsoleUtil;
import com.ticketsystem.util.SessionManager;

import java.util.Optional;
import java.util.Scanner;

/**
 * Workflow E: Dodanie komentarza do zlecenia.
 * Dostępne dla wszystkich — bez możliwości edycji innych pól zlecenia.
 *
 * Kroki:
 * 1. Użytkownik podaje UUID zlecenia.
 * 2. System wyświetla tytuł i status.
 * 3. Użytkownik podaje treść komentarza (i opcjonalnie imię, jeśli niezalogowany).
 * 4. System zapisuje komentarz.
 */
public class WorkflowE implements Workflow {

    private final TicketService ticketService = new TicketService();
    private final CommentService commentService = new CommentService();
    private final LogService logService = new LogService();

    @Override
    public void execute(Scanner scanner) {
        ConsoleUtil.printHeader("E — DODANIE KOMENTARZA DO ZLECENIA");

        // ── Step 1: find ticket ───────────────────────────────────────────────
        String uuidInput = ConsoleUtil.readRequired(scanner, "Podaj UUID zlecenia");
        Optional<Ticket> ticketOpt = ticketService.findById(uuidInput);

        if (ticketOpt.isEmpty()) {
            ConsoleUtil.printError("Nie znaleziono zlecenia o podanym UUID.");
            ConsoleUtil.pressEnterToContinue(scanner);
            return;
        }

        Ticket ticket = ticketOpt.get();

        // ── Step 2: show context ──────────────────────────────────────────────
        ConsoleUtil.printSection("Zlecenie");
        ConsoleUtil.println("Tytuł:   " + ticket.getTitle());
        ConsoleUtil.println("Status:  " + ticket.getStatus().getDisplayName());
        ConsoleUtil.println("Klient:  " + (ticket.getClient() != null
                ? ticket.getClient().getFullName() : "—"));

        // ── Step 3: determine author name ─────────────────────────────────────
        SessionManager session = SessionManager.getInstance();
        String authorName;

        if (session.isLoggedIn()) {
            authorName = session.getCurrentUser().getUsername();
            ConsoleUtil.printInfo("Komentarz zostanie dodany jako: " + authorName);
        } else {
            ConsoleUtil.printSection("Dane autora");
            String input = ConsoleUtil.readOptional(scanner, "Twoje imię i nazwisko");
            authorName = input.isEmpty() ? "Anonim" : input;
        }

        // ── Step 4: comment content ───────────────────────────────────────────
        ConsoleUtil.printSection("Treść komentarza");
        String content = ConsoleUtil.readRequired(scanner, "Komentarz");

        // ── Step 5: confirm ───────────────────────────────────────────────────
        if (!ConsoleUtil.confirm(scanner, "Dodać komentarz?")) {
            ConsoleUtil.printInfo("Komentarz nie został dodany.");
            ConsoleUtil.pressEnterToContinue(scanner);
            return;
        }

        // ── Step 6: save ──────────────────────────────────────────────────────
        try {
            Comment comment = commentService.addComment(ticket, authorName, content);

            logService.log(ticket,
                    "Dodano komentarz przez: " + authorName,
                    session.getActorName());

            ConsoleUtil.printSuccess("Komentarz został dodany pomyślnie.");
            ConsoleUtil.printInfo("ID komentarza: " + comment.getId());

        } catch (Exception e) {
            ConsoleUtil.printError("Błąd dodawania komentarza: " + e.getMessage());
        }

        ConsoleUtil.pressEnterToContinue(scanner);
    }
}
