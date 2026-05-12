package com.ticketsystem.cli.workflows;

import com.ticketsystem.cli.Workflow;
import com.ticketsystem.model.User;
import com.ticketsystem.service.UserService;
import com.ticketsystem.util.ConsoleUtil;
import com.ticketsystem.util.SessionManager;

import java.util.List;
import java.util.Optional;
import java.util.Scanner;

/**
 * Workflow I: Administrator usuwa operatora lub innego administratora.
 * Administrator nie może usunąć własnego konta.
 */
public class WorkflowI implements Workflow {

    private final UserService userService = new UserService();

    @Override
    public void execute(Scanner scanner) {
        ConsoleUtil.printHeader("I — USUNIĘCIE UŻYTKOWNIKA SYSTEMU (ADMIN)");

        // ── Step 1: list all users ────────────────────────────────────────────
        List<User> users = userService.findAll();
        User self = SessionManager.getInstance().getCurrentUser();

        if (users.isEmpty()) {
            ConsoleUtil.printInfo("Brak użytkowników w systemie.");
            ConsoleUtil.pressEnterToContinue(scanner);
            return;
        }

        ConsoleUtil.printSection("Użytkownicy systemu");
        int index = 1;
        for (User user : users) {
            String selfTag = user.getId().equals(self.getId()) ? " (Ty)" : "";
            System.out.printf("  [%d] %-20s [%s]%s%n",
                    index++, user.getUsername(), user.getRole().getDisplayName(), selfTag);
        }

        // ── Step 2: pick user ─────────────────────────────────────────────────
        int pick = ConsoleUtil.readInt(scanner,
                "Numer użytkownika do usunięcia (0 = anuluj)", 0, users.size());

        if (pick == 0) {
            ConsoleUtil.printInfo("Operacja anulowana.");
            ConsoleUtil.pressEnterToContinue(scanner);
            return;
        }

        User target = users.get(pick - 1);

        // ── Step 3: guard — cannot delete self ────────────────────────────────
        if (target.getId().equals(self.getId())) {
            ConsoleUtil.printError("Nie możesz usunąć własnego konta.");
            ConsoleUtil.pressEnterToContinue(scanner);
            return;
        }

        // ── Step 4: confirm ───────────────────────────────────────────────────
        ConsoleUtil.printWarning("Usunięcie użytkownika jest nieodwracalne!");
        ConsoleUtil.println("Użytkownik: " + target.getUsername()
                + " [" + target.getRole().getDisplayName() + "]");

        if (!ConsoleUtil.confirm(scanner, "Potwierdzasz usunięcie?")) {
            ConsoleUtil.printInfo("Operacja anulowana.");
            ConsoleUtil.pressEnterToContinue(scanner);
            return;
        }

        // ── Step 5: delete ────────────────────────────────────────────────────
        try {
            userService.deleteUser(target);
            ConsoleUtil.printSuccess("Użytkownik '" + target.getUsername() + "' został usunięty.");
        } catch (Exception e) {
            ConsoleUtil.printError("Błąd usunięcia: " + e.getMessage());
        }

        ConsoleUtil.pressEnterToContinue(scanner);
    }
}
