package com.ticketsystem.cli.workflows;

import com.ticketsystem.cli.Workflow;
import com.ticketsystem.model.User;
import com.ticketsystem.service.UserService;
import com.ticketsystem.util.ConsoleUtil;

import java.util.Scanner;

/**
 * Workflow G: Administrator dodaje nowego operatora (lub administratora).
 */
public class WorkflowG implements Workflow {

    private final UserService userService = new UserService();

    @Override
    public void execute(Scanner scanner) {
        ConsoleUtil.printHeader("G — DODANIE UŻYTKOWNIKA SYSTEMU");

        // ── Step 1: choose role ───────────────────────────────────────────────
        System.out.println("\n  Rola nowego użytkownika:");
        System.out.println("  [1] Operator");
        System.out.println("  [2] Administrator");
        System.out.println("  [0] Anuluj");

        int roleChoice = ConsoleUtil.readInt(scanner, "Wybór", 0, 2);
        if (roleChoice == 0) {
            ConsoleUtil.printInfo("Operacja anulowana.");
            return;
        }

        boolean isAdmin = (roleChoice == 2);
        String roleName = isAdmin ? "Administrator" : "Operator";

        // ── Step 2: username ──────────────────────────────────────────────────
        String username;
        while (true) {
            username = ConsoleUtil.readRequired(scanner, "Nazwa użytkownika");
            if (userService.usernameExists(username)) {
                ConsoleUtil.printError("Użytkownik '" + username + "' już istnieje. Podaj inną nazwę.");
            } else {
                break;
            }
        }

        // ── Step 3: password ──────────────────────────────────────────────────
        String password = ConsoleUtil.readPassword(scanner, "Hasło");
        if (password.length() < 6) {
            ConsoleUtil.printError("Hasło musi mieć co najmniej 6 znaków. Operacja anulowana.");
            ConsoleUtil.pressEnterToContinue(scanner);
            return;
        }

        // ── Step 4: confirm ───────────────────────────────────────────────────
        ConsoleUtil.println("Nowy użytkownik: " + username + " [" + roleName + "]");
        if (!ConsoleUtil.confirm(scanner, "Dodać użytkownika?")) {
            ConsoleUtil.printInfo("Operacja anulowana.");
            ConsoleUtil.pressEnterToContinue(scanner);
            return;
        }

        // ── Step 5: save ──────────────────────────────────────────────────────
        try {
            User created = isAdmin
                    ? userService.createAdministrator(username, password)
                    : userService.createOperator(username, password);

            ConsoleUtil.printSuccess("Użytkownik '" + created.getUsername()
                    + "' [" + created.getRole().getDisplayName() + "] został utworzony.");

        } catch (Exception e) {
            ConsoleUtil.printError("Błąd tworzenia użytkownika: " + e.getMessage());
        }

        ConsoleUtil.pressEnterToContinue(scanner);
    }
}
