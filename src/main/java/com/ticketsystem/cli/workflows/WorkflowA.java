package com.ticketsystem.cli.workflows;

import com.ticketsystem.cli.Workflow;
import com.ticketsystem.model.Client;
import com.ticketsystem.model.Ticket;
import com.ticketsystem.service.ClientService;
import com.ticketsystem.service.LogService;
import com.ticketsystem.service.TicketService;
import com.ticketsystem.util.ConsoleUtil;
import com.ticketsystem.util.SessionManager;

import java.util.Optional;
import java.util.Scanner;

/**
 * Workflow A: Dodanie zlecenia.
 * Dostępne dla wszystkich (zalogowanych i anonimowych).
 *
 * Kroki:
 * 1. Klient poda swoje ID (jeśli już istnieje) LUB tworzy nowy rekord klienta.
 * 2. Wypełnia tytuł i opis zlecenia.
 * 3. Potwierdza — system zapisuje i zwraca UUID zlecenia.
 */
public class WorkflowA implements Workflow {

    private final ClientService clientService = new ClientService();
    private final TicketService ticketService = new TicketService();
    private final LogService logService = new LogService();

    @Override
    public void execute(Scanner scanner) {
        ConsoleUtil.printHeader("A — DODANIE ZLECENIA");

        // ── Step 1: identify client ───────────────────────────────────────────
        Client client = resolveClient(scanner);
        if (client == null) {
            ConsoleUtil.printInfo("Operacja anulowana.");
            return;
        }

        ConsoleUtil.printSuccess("Klient: " + client.getFullName() + " (ID: " + client.getId() + ")");

        // ── Step 2: ticket fields ─────────────────────────────────────────────
        ConsoleUtil.printSection("Dane zlecenia");
        String title = ConsoleUtil.readRequired(scanner, "Tytuł zlecenia");
        String description = ConsoleUtil.readOptional(scanner, "Opis zlecenia");

        // ── Step 3: confirm ───────────────────────────────────────────────────
        System.out.println();
        ConsoleUtil.println("Podsumowanie:");
        ConsoleUtil.println("  Klient:  " + client.getFullName());
        ConsoleUtil.println("  Tytuł:   " + title);
        ConsoleUtil.println("  Opis:    " + (description.isEmpty() ? "—" : description));

        if (!ConsoleUtil.confirm(scanner, "Potwierdzasz dodanie zlecenia?")) {
            ConsoleUtil.printInfo("Zlecenie nie zostało dodane.");
            return;
        }

        // ── Step 4: save ──────────────────────────────────────────────────────
        try {
            Ticket ticket = ticketService.createTicket(
                    title,
                    description.isEmpty() ? null : description,
                    client);

            String actor = SessionManager.getInstance().getActorName();
            logService.log(ticket, "Zlecenie zostało dodane przez: " + actor, actor);

            System.out.println();
            ConsoleUtil.printSuccess("Zlecenie zostało dodane pomyślnie!");
            ConsoleUtil.printInfo("UUID zlecenia (zachowaj go!): " + ticket.getId());
            ConsoleUtil.printInfo("Status: " + ticket.getStatus().getDisplayName());

        } catch (Exception e) {
            ConsoleUtil.printError("Nie udało się dodać zlecenia: " + e.getMessage());
        }

        ConsoleUtil.pressEnterToContinue(scanner);
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private Client resolveClient(Scanner scanner) {
        ConsoleUtil.printSection("Identyfikacja klienta");
        ConsoleUtil.println("Jeżeli masz już swoje ID klienta, możesz je podać.");
        ConsoleUtil.println("Jeżeli nie, zostanie utworzony nowy rekord klienta.");

        boolean hasId = ConsoleUtil.confirm(scanner, "Czy posiadasz ID klienta?");

        if (hasId) {
            return findExistingClient(scanner);
        } else {
            return createNewClient(scanner);
        }
    }

    private Client findExistingClient(Scanner scanner) {
        String idInput = ConsoleUtil.readRequired(scanner, "Podaj UUID klienta");
        Optional<Client> clientOpt = clientService.findById(idInput);

        if (clientOpt.isPresent()) {
            return clientOpt.get();
        }

        ConsoleUtil.printError("Nie znaleziono klienta o podanym ID.");

        if (ConsoleUtil.confirm(scanner, "Czy chcesz utworzyć nowy rekord klienta zamiast?")) {
            return createNewClient(scanner);
        }

        return null;
    }

    private Client createNewClient(Scanner scanner) {
        ConsoleUtil.println("Podaj dane klienta:");
        String firstName = ConsoleUtil.readRequired(scanner, "Imię");
        String lastName  = ConsoleUtil.readRequired(scanner, "Nazwisko");

        try {
            Client client = clientService.createClient(firstName, lastName);
            ConsoleUtil.printSuccess("Nowy klient utworzony. ID klienta: " + client.getId());
            ConsoleUtil.printWarning("Zapisz swoje ID klienta, aby móc przypisać przyszłe zlecenia!");
            return client;
        } catch (Exception e) {
            ConsoleUtil.printError("Błąd tworzenia klienta: " + e.getMessage());
            return null;
        }
    }
}
