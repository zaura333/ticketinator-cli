package com.ticketsystem.cli.workflows;

import com.ticketsystem.cli.Workflow;
import com.ticketsystem.model.Client;
import com.ticketsystem.service.ClientService;
import com.ticketsystem.util.ConsoleUtil;

import java.util.List;
import java.util.Optional;
import java.util.Scanner;

/**
 * Workflow M: Administrator usuwa klienta.
 * Administrator może wyszukać klienta po ID lub fragmencie nazwiska.
 */
public class WorkflowM implements Workflow {

    private final ClientService clientService = new ClientService();

    @Override
    public void execute(Scanner scanner) {
        ConsoleUtil.printHeader("M — USUNIĘCIE KLIENTA (ADMIN)");

        // ── Step 1: find client ───────────────────────────────────────────────
        System.out.println("\n  Jak wyszukać klienta?");
        System.out.println("  [1] Po UUID");
        System.out.println("  [2] Po fragmencie imienia/nazwiska");
        System.out.println("  [0] Anuluj");

        int searchMethod = ConsoleUtil.readInt(scanner, "Wybór", 0, 2);

        Client target = null;

        switch (searchMethod) {
            case 0 -> {
                ConsoleUtil.printInfo("Operacja anulowana.");
                return;
            }
            case 1 -> target = findById(scanner);
            case 2 -> target = findByName(scanner);
        }

        if (target == null) {
            ConsoleUtil.pressEnterToContinue(scanner);
            return;
        }

        // ── Step 2: confirm ───────────────────────────────────────────────────
        ConsoleUtil.printSection("Klient do usunięcia");
        ConsoleUtil.println("ID:        " + target.getId());
        ConsoleUtil.println("Imię:      " + target.getFirstName());
        ConsoleUtil.println("Nazwisko:  " + target.getLastName());
        ConsoleUtil.printWarning("Usunięcie klienta jest nieodwracalne!");
        ConsoleUtil.printWarning("Powiązane zlecenia mogą stracić referencję do klienta.");

        if (!ConsoleUtil.confirm(scanner, "Potwierdzasz usunięcie klienta?")) {
            ConsoleUtil.printInfo("Operacja anulowana.");
            ConsoleUtil.pressEnterToContinue(scanner);
            return;
        }

        // ── Step 3: delete ────────────────────────────────────────────────────
        try {
            clientService.deleteClient(target);
            ConsoleUtil.printSuccess("Klient '" + target.getFullName() + "' został usunięty.");
        } catch (Exception e) {
            ConsoleUtil.printError("Błąd usunięcia: " + e.getMessage());
        }

        ConsoleUtil.pressEnterToContinue(scanner);
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private Client findById(Scanner scanner) {
        String idInput = ConsoleUtil.readRequired(scanner, "Podaj UUID klienta");
        Optional<Client> opt = clientService.findById(idInput);
        if (opt.isEmpty()) {
            ConsoleUtil.printError("Nie znaleziono klienta o podanym UUID.");
            return null;
        }
        return opt.get();
    }

    private Client findByName(Scanner scanner) {
        String query = ConsoleUtil.readRequired(scanner, "Fragment imienia lub nazwiska");
        List<Client> results = clientService.searchByName(query);

        if (results.isEmpty()) {
            ConsoleUtil.printError("Nie znaleziono klientów pasujących do: " + query);
            return null;
        }

        ConsoleUtil.printSection("Wyniki wyszukiwania (" + results.size() + ")");
        for (int i = 0; i < results.size(); i++) {
            Client c = results.get(i);
            System.out.printf("  [%d] %-20s %-20s  %s%n",
                    i + 1, c.getFirstName(), c.getLastName(), c.getId());
        }
        System.out.println("  [0] Anuluj");

        int pick = ConsoleUtil.readInt(scanner, "Wybór", 0, results.size());
        if (pick == 0) return null;
        return results.get(pick - 1);
    }
}
