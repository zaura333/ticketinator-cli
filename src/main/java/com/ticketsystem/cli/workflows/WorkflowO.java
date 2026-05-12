package com.ticketsystem.cli.workflows;

import com.ticketsystem.cli.Workflow;
import com.ticketsystem.service.AuthService;
import com.ticketsystem.util.ConsoleUtil;
import com.ticketsystem.util.SessionManager;

import java.util.Scanner;

/**
 * Workflow O: Wylogowanie bieżącego użytkownika.
 */
public class WorkflowO implements Workflow {

    private final AuthService authService = new AuthService();

    @Override
    public void execute(Scanner scanner) {
        ConsoleUtil.printHeader("O — WYLOGOWANIE");

        String username = SessionManager.getInstance().getCurrentUser().getUsername();

        if (!ConsoleUtil.confirm(scanner, "Wylogować się?")) {
            ConsoleUtil.printInfo("Anulowano.");
            ConsoleUtil.pressEnterToContinue(scanner);
            return;
        }

        authService.logout();
        ConsoleUtil.printSuccess("Wylogowano pomyślnie. Do widzenia, " + username + "!");
        ConsoleUtil.pressEnterToContinue(scanner);
    }
}
