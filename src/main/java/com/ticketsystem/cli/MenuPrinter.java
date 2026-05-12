package com.ticketsystem.cli;

import com.ticketsystem.util.ConsoleUtil;
import com.ticketsystem.util.SessionManager;

/**
 * Renders the main CLI menu.
 * Available options change depending on whether the user is anonymous,
 * an Operator, or an Administrator.
 */
public class MenuPrinter {

    private static final String TITLE = "SYSTEM TICKETOWY v1.0";

    public void print() {
        SessionManager session = SessionManager.getInstance();

        System.out.println("\n" + ConsoleUtil.SEP);
        System.out.println("  " + TITLE);
        System.out.println("  " + session.getStatusLine());
        System.out.println(ConsoleUtil.SEP);

        // ── Available to everyone ─────────────────────────────────────────────
        System.out.println("\n  --- Operacje ogólne (dostępne dla wszystkich) ---");
        System.out.println("  [A] Dodaj zlecenie");
        System.out.println("  [B] Modyfikuj zlecenie (tytuł / opis)");
        System.out.println("  [E] Dodaj komentarz do zlecenia");
        System.out.println("  [P] Podejrzyj zlecenie po UUID");

        if (!session.isLoggedIn()) {
            System.out.println("\n  --- Konto ---");
            System.out.println("  [N] Zaloguj się (Operator / Administrator)");
        }

        // ── Operator + Admin ──────────────────────────────────────────────────
        if (session.isAtLeastOperator()) {
            System.out.println("\n  --- Operacje operatora ---");
            System.out.println("  [C] Rozpocznij zlecenie");
            System.out.println("  [D] Zmień status zlecenia");
            System.out.println("  [F] Przeglądaj zlecenia (wszystkie lub po statusie)");
            System.out.println("  [K] Moje zlecenia (przypisane do mnie)");
            System.out.println("  [R] Przeglądaj komentarze zlecenia po UUID");
        }

        // ── Admin only ────────────────────────────────────────────────────────
        if (session.isAdministrator()) {
            System.out.println("\n  --- Administracja ---");
            System.out.println("  [G] Dodaj operatora");
            System.out.println("  [H] Modyfikuj / usuń komentarz");
            System.out.println("  [I] Usuń operatora lub administratora");
            System.out.println("  [J] Odśwież statusy (oznacz opóźnione)");
            System.out.println("  [L] Dodaj klienta");
            System.out.println("  [M] Usuń klienta");
            System.out.println("  [S] Przeglądaj logi audytowe zlecenia");
        }

        // ── Account ───────────────────────────────────────────────────────────
        if (session.isLoggedIn()) {
            System.out.println("\n  --- Konto ---");
            System.out.println("  [N] Zaloguj się jako inny użytkownik");
            System.out.println("  [O] Wyloguj się");
        }

        System.out.println("\n  [Q] Wyjście");
        System.out.println(ConsoleUtil.THIN);
        System.out.print("  Wybierz opcję: ");
    }
}
