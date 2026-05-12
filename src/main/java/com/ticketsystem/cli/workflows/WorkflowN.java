package com.ticketsystem.cli.workflows;

import com.ticketsystem.cli.Workflow;
import com.ticketsystem.model.User;
import com.ticketsystem.service.AuthService;
import com.ticketsystem.util.ConsoleUtil;
import com.ticketsystem.util.SessionManager;

import java.util.Scanner;

/**
 * Workflow N: Logowanie operatora lub administratora.
 * Jeśli sesja jest już aktywna, użytkownik jest informowany i może zalogować się ponownie.
 */
public class WorkflowN implements Workflow {

    private final AuthService authService = new AuthService();

    @Override
    public void execute(Scanner scanner) {
        ConsoleUtil.printHeader("N — LOGOWANIE");

        SessionManager session = SessionManager.getInstance();

        if (session.isLoggedIn()) {
            ConsoleUtil.printInfo("Jesteś już zalogowany jako: " + session.getCurrentUser().getUsername());
            if (!ConsoleUtil.confirm(scanner, "Zalogować się jako inny użytkownik?")) {
                ConsoleUtil.pressEnterToContinue(scanner);
                return;
            }
            authService.logout();
        }

        // ── Credentials ───────────────────────────────────────────────────────
        String username = ConsoleUtil.readRequired(scanner, "Nazwa użytkownika");
        String password  = ConsoleUtil.readPassword(scanner, "Hasło");

        // ── Authenticate ──────────────────────────────────────────────────────
        try {
            User user = authService.login(username, password);
            ConsoleUtil.printSuccess("Zalogowano pomyślnie!");
            ConsoleUtil.printInfo("Witaj, " + user.getUsername()
                    + " [" + user.getRole().getDisplayName() + "]");
        } catch (SecurityException e) {
            ConsoleUtil.printError(e.getMessage());
        }

        ConsoleUtil.pressEnterToContinue(scanner);
    }
}
