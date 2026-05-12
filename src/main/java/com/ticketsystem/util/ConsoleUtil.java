package com.ticketsystem.util;

import java.util.Scanner;

/**
 * Helper utilities for consistent CLI output formatting and input reading.
 */
public final class ConsoleUtil {

    public static final String SEP  = "═".repeat(62);
    public static final String THIN = "─".repeat(62);

    private ConsoleUtil() {}

    // ── Output helpers ────────────────────────────────────────────────────────

    public static void printHeader(String title) {
        System.out.println("\n" + SEP);
        System.out.println("  " + title);
        System.out.println(SEP);
    }

    public static void printSection(String title) {
        System.out.println("\n" + THIN);
        System.out.println("  " + title);
        System.out.println(THIN);
    }

    public static void printSuccess(String message) {
        System.out.println("✓ " + message);
    }

    public static void printError(String message) {
        System.out.println("✗ BŁĄD: " + message);
    }

    public static void printInfo(String message) {
        System.out.println("  → " + message);
    }

    public static void printWarning(String message) {
        System.out.println("⚠ UWAGA: " + message);
    }

    public static void println(String message) {
        System.out.println("  " + message);
    }

    // ── Input helpers ─────────────────────────────────────────────────────────

    /**
     * Prompts the user and reads a non-empty line. Repeats until input is provided.
     */
    public static String readRequired(Scanner scanner, String prompt) {
        while (true) {
            System.out.print("  " + prompt + ": ");
            String input = scanner.nextLine().trim();
            if (!input.isEmpty()) {
                return input;
            }
            printError("Pole jest wymagane. Podaj wartość.");
        }
    }

    /**
     * Prompts the user and reads an optional line. Returns empty string if skipped.
     */
    public static String readOptional(Scanner scanner, String prompt) {
        System.out.print("  " + prompt + " (opcjonalne — Enter aby pominąć): ");
        return scanner.nextLine().trim();
    }

    /**
     * Reads a line without additional validation.
     */
    public static String readLine(Scanner scanner, String prompt) {
        System.out.print("  " + prompt + ": ");
        return scanner.nextLine().trim();
    }

    /**
     * Reads an integer in the given range. Repeats until valid input is provided.
     */
    public static int readInt(Scanner scanner, String prompt, int min, int max) {
        while (true) {
            System.out.print("  " + prompt + " [" + min + "-" + max + "]: ");
            String input = scanner.nextLine().trim();
            try {
                int value = Integer.parseInt(input);
                if (value >= min && value <= max) {
                    return value;
                }
                printError("Podaj liczbę między " + min + " a " + max + ".");
            } catch (NumberFormatException e) {
                printError("Nieprawidłowa wartość. Podaj liczbę całkowitą.");
            }
        }
    }

    /**
     * Asks a yes/no question. Accepts 't', 'tak', 'y', 'yes' (case-insensitive).
     */
    public static boolean confirm(Scanner scanner, String question) {
        System.out.print("  " + question + " (t/n): ");
        String input = scanner.nextLine().trim().toLowerCase();
        return input.equals("t") || input.equals("tak")
                || input.equals("y") || input.equals("yes");
    }

    /**
     * Reads a password (plain text — terminal echo suppression not available in all IDEs).
     */
    public static String readPassword(Scanner scanner, String prompt) {
        System.out.print("  " + prompt + ": ");
        return scanner.nextLine().trim();
    }

    /**
     * Pauses until the user presses Enter.
     */
    public static void pressEnterToContinue(Scanner scanner) {
        System.out.print("\n  [Naciśnij Enter, aby kontynuować...]");
        scanner.nextLine();
    }
}
