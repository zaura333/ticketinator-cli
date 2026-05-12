package com.ticketsystem.cli;

import com.ticketsystem.cli.workflows.*;
import com.ticketsystem.util.ConsoleUtil;
import com.ticketsystem.util.SessionManager;

import java.util.Scanner;

/**
 * Main CLI controller.
 * Renders the menu and dispatches the selected option to the appropriate workflow.
 * Access control is checked here — restricted workflows print an error if the user
 * lacks the required role.
 */
public class CLI {

    private final Scanner scanner;
    private final MenuPrinter menuPrinter;
    private final SessionManager session;

    // ── Workflow instances ────────────────────────────────────────────────────

    private final WorkflowA workflowA = new WorkflowA();
    private final WorkflowB workflowB = new WorkflowB();
    private final WorkflowC workflowC = new WorkflowC();
    private final WorkflowD workflowD = new WorkflowD();
    private final WorkflowE workflowE = new WorkflowE();
    private final WorkflowF workflowF = new WorkflowF();
    private final WorkflowG workflowG = new WorkflowG();
    private final WorkflowH workflowH = new WorkflowH();
    private final WorkflowI workflowI = new WorkflowI();
    private final WorkflowJ workflowJ = new WorkflowJ();
    private final WorkflowK workflowK = new WorkflowK();
    private final WorkflowL workflowL = new WorkflowL();
    private final WorkflowM workflowM = new WorkflowM();
    private final WorkflowN workflowN = new WorkflowN();
    private final WorkflowO workflowO = new WorkflowO();
    private final WorkflowP workflowP = new WorkflowP();

    public CLI() {
        this.scanner = new Scanner(System.in);
        this.menuPrinter = new MenuPrinter();
        this.session = SessionManager.getInstance();
    }

    // ── Main loop ─────────────────────────────────────────────────────────────

    public void run() {
        System.out.println("\n" + ConsoleUtil.SEP);
        System.out.println("  Witaj w Systemie Ticketowym!");
        System.out.println("  Użyj liter w nawiasach [] aby wybrać opcję.");
        System.out.println(ConsoleUtil.SEP);

        while (true) {
            menuPrinter.print();
            String input = scanner.nextLine().trim().toUpperCase();

            if (input.isEmpty()) continue;

            char choice = input.charAt(0);

            if (choice == 'Q') {
                System.out.println("\n  Do widzenia!");
                break;
            }

            dispatch(choice);
        }
    }

    // ── Dispatcher ────────────────────────────────────────────────────────────

    private void dispatch(char choice) {
        switch (choice) {
            // ── Anyone ───────────────────────────────────────────────────────
            case 'A' -> workflowA.execute(scanner);
            case 'B' -> workflowB.execute(scanner);
            case 'E' -> workflowE.execute(scanner);
            case 'P' -> workflowP.execute(scanner);
            case 'N' -> workflowN.execute(scanner);

            // ── Operator + Admin ─────────────────────────────────────────────
            case 'C' -> requireAtLeastOperator(() -> workflowC.execute(scanner));
            case 'D' -> requireAtLeastOperator(() -> workflowD.execute(scanner));
            case 'F' -> requireAtLeastOperator(() -> workflowF.execute(scanner));
            case 'K' -> requireAtLeastOperator(() -> workflowK.execute(scanner));
            case 'O' -> requireLoggedIn(() -> workflowO.execute(scanner));

            // ── Admin only ───────────────────────────────────────────────────
            case 'G' -> requireAdmin(() -> workflowG.execute(scanner));
            case 'H' -> requireAdmin(() -> workflowH.execute(scanner));
            case 'I' -> requireAdmin(() -> workflowI.execute(scanner));
            case 'J' -> requireAdmin(() -> workflowJ.execute(scanner));
            case 'L' -> requireAdmin(() -> workflowL.execute(scanner));
            case 'M' -> requireAdmin(() -> workflowM.execute(scanner));

            default -> ConsoleUtil.printError("Nieznana opcja: '" + choice + "'. Wybierz opcję z menu.");
        }
    }

    // ── Access control helpers ────────────────────────────────────────────────

    private void requireLoggedIn(Runnable action) {
        if (!session.isLoggedIn()) {
            ConsoleUtil.printError("Ta operacja wymaga zalogowania. Wybierz [N] aby się zalogować.");
            return;
        }
        action.run();
    }

    private void requireAtLeastOperator(Runnable action) {
        if (!session.isAtLeastOperator()) {
            ConsoleUtil.printError("Ta operacja jest dostępna tylko dla operatorów i administratorów.");
            return;
        }
        action.run();
    }

    private void requireAdmin(Runnable action) {
        if (!session.isAdministrator()) {
            ConsoleUtil.printError("Ta operacja jest dostępna tylko dla administratorów.");
            return;
        }
        action.run();
    }
}
