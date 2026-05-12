package com.ticketsystem;

import com.ticketsystem.cli.CLI;
import com.ticketsystem.service.UserService;
import com.ticketsystem.util.HibernateUtil;

/**
 * Application entry point.
 *
 * Startup sequence:
 *  1. Initialise Hibernate SessionFactory (connects to MySQL, creates/updates schema).
 *  2. Seed default admin account if the users table is empty.
 *  3. Start the interactive CLI loop.
 *  4. On exit, shut down Hibernate gracefully.
 */
public class Main {

    public static void main(String[] args) {

        // ── 1. Hibernate init (connects to DB, runs hbm2ddl) ─────────────────
        try {
            HibernateUtil.getSessionFactory();
        } catch (ExceptionInInitializerError e) {
            System.err.println("\n══════════════════════════════════════════════════════════════");
            System.err.println("  BŁĄD POŁĄCZENIA Z BAZĄ DANYCH");
            System.err.println("  Uruchom MySQL: docker-compose up -d");
            System.err.println("  Następnie ponownie uruchom aplikację.");
            System.err.println("══════════════════════════════════════════════════════════════");
            System.exit(1);
        }

        // ── 2. Seed default data ──────────────────────────────────────────────
        DataSeeder seeder = new DataSeeder(new UserService());
        seeder.seed();

        // ── 3. Run CLI ────────────────────────────────────────────────────────
        CLI cli = new CLI();
        cli.run();

        // ── 4. Shutdown ───────────────────────────────────────────────────────
        HibernateUtil.shutdown();
    }
}
