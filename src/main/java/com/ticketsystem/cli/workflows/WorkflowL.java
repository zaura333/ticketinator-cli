package com.ticketsystem.cli.workflows;

import com.ticketsystem.cli.Workflow;
import com.ticketsystem.model.Client;
import com.ticketsystem.service.ClientService;
import com.ticketsystem.util.ConsoleUtil;

import java.util.Scanner;

/**
 * Workflow L: Administrator dodaje klienta ręcznie (bez tworzenia zlecenia).
 * Przydatne przy importowaniu klientów lub wstępnej konfiguracji systemu.
 */
public class WorkflowL implements Workflow {

    private final ClientService clientService = new ClientService();

    @Override
    public void execute(Scanner scanner) {
        ConsoleUtil.printHeader("L — DODANIE KLIENTA (ADMIN)");

        // ── Step 1: data ──────────────────────────────────────────────────────
        String firstName = ConsoleUtil.readRequired(scanner, "Imię klienta");
        String lastName  = ConsoleUtil.readRequired(scanner, "Nazwisko klienta");

        // ── Step 2: confirm ───────────────────────────────────────────────────
        ConsoleUtil.println("Klient: " + firstName + " " + lastName);
        if (!ConsoleUtil.confirm(scanner, "Dodać klienta?")) {
            ConsoleUtil.printInfo("Operacja anulowana.");
            ConsoleUtil.pressEnterToContinue(scanner);
            return;
        }

        // ── Step 3: save ──────────────────────────────────────────────────────
        try {
            Client client = clientService.createClient(firstName, lastName);
            ConsoleUtil.printSuccess("Klient dodany pomyślnie.");
            ConsoleUtil.printInfo("ID klienta: " + client.getId());
            ConsoleUtil.printInfo("Imię:       " + client.getFirstName());
            ConsoleUtil.printInfo("Nazwisko:   " + client.getLastName());
        } catch (Exception e) {
            ConsoleUtil.printError("Błąd dodawania klienta: " + e.getMessage());
        }

        ConsoleUtil.pressEnterToContinue(scanner);
    }
}
