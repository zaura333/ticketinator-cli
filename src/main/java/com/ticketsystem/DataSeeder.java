package com.ticketsystem;

import com.ticketsystem.model.*;
import com.ticketsystem.model.enums.TicketStatus;
import com.ticketsystem.service.*;
import com.ticketsystem.util.ConsoleUtil;
import com.ticketsystem.util.PasswordUtil;

import java.time.LocalDateTime;

/**
 * Seeds the database with initial data on first application start.
 *
 * <p>Two-phase seeding:
 * <ol>
 *   <li><b>Default admin</b> — runs only when the users table is completely empty.</li>
 *   <li><b>Demo data</b> — runs only when the clients table is empty.
 *       Creates 2 operators, 5 clients, 9 tickets (mixed statuses),
 *       25 comments, and ~10 audit log entries.</li>
 * </ol>
 */
public class DataSeeder {

    private final UserService    userService;
    private final ClientService  clientService;
    private final TicketService  ticketService;
    private final CommentService commentService;
    private final LogService     logService;

    public DataSeeder(UserService userService) {
        this.userService    = userService;
        this.clientService  = new ClientService();
        this.ticketService  = new TicketService();
        this.commentService = new CommentService();
        this.logService     = new LogService();
    }

    // ── Entry point ───────────────────────────────────────────────────────────

    public void seed() {
        seedDefaultAdmin();
        seedDemoData();
    }

    // ── Phase 1: default admin ────────────────────────────────────────────────

    private void seedDefaultAdmin() {
        if (!userService.findAll().isEmpty()) {
            return; // Already seeded — skip
        }

        System.out.println("\n" + ConsoleUtil.SEP);
        System.out.println("  PIERWSZE URUCHOMIENIE — Tworzenie domyślnego konta administratora");
        System.out.println(ConsoleUtil.SEP);

        try {
            Administrator admin = new Administrator(
                    "admin",
                    PasswordUtil.hashPassword("admin123")
            );
            userService.saveUser(admin);

            System.out.println("  ✓ Konto administratora zostało utworzone.");
            System.out.println("  ┌─────────────────────────────────────────┐");
            System.out.println("  │  Użytkownik:  admin                     │");
            System.out.println("  │  Hasło:       admin123                  │");
            System.out.println("  │  ZMIEŃ HASŁO po pierwszym logowaniu!    │");
            System.out.println("  └─────────────────────────────────────────┘");
            System.out.println(ConsoleUtil.SEP);

        } catch (Exception e) {
            System.err.println("BŁĄD podczas seedowania danych administratora: " + e.getMessage());
        }
    }

    // ── Phase 2: demo data ────────────────────────────────────────────────────

    private void seedDemoData() {
        if (!clientService.findAll().isEmpty()) {
            return; // Demo data already present — skip
        }

        System.out.println("\n" + ConsoleUtil.SEP);
        System.out.println("  SEEDOWANIE DANYCH DEMO");
        System.out.println(ConsoleUtil.SEP);

        try {
            // ── Users (2 operators) ───────────────────────────────────────────
            Operator opJan  = (Operator) userService.createOperator("jan.kowalski",  "haslo123");
            Operator opAnna = (Operator) userService.createOperator("anna.nowak",    "haslo123");
            System.out.println("  ✓ Użytkownicy (2 operatorów) utworzeni.");

            // ── Clients (5) ───────────────────────────────────────────────────
            Client cMarek    = clientService.createClient("Marek",     "Wiśniewski");
            Client cKasia    = clientService.createClient("Katarzyna", "Zielińska");
            Client cTomasz   = clientService.createClient("Tomasz",    "Dąbrowski");
            Client cAgnieszka = clientService.createClient("Agnieszka","Lewandowska");
            Client cPiotr    = clientService.createClient("Piotr",     "Wójcik");
            System.out.println("  ✓ Klienci (5) utworzeni.");

            // ── Tickets (9) ───────────────────────────────────────────────────
            // 1 — NEW
            Ticket t1 = ticketService.createTicket(
                    "Brak dostępu do systemu CRM",
                    "Klient nie może zalogować się do systemu CRM od wczoraj. " +
                    "Komunikat błędu: 'Invalid credentials'. Hasło było niedawno zmienione przez administratora.",
                    cMarek);

            // 2 — NEW
            Ticket t2 = ticketService.createTicket(
                    "Problem z drukarką sieciową",
                    "Drukarka HP LaserJet w dziale księgowości przestała być widoczna w sieci. " +
                    "Inne urządzenia działają poprawnie. Wymagana diagnostyka.",
                    cKasia);

            // 3 — NEW
            Ticket t3 = ticketService.createTicket(
                    "Aktualizacja oprogramowania antywirusowego",
                    "Prośba o zaktualizowanie oprogramowania antywirusowego na stacjach roboczych " +
                    "w dziale sprzedaży (12 stanowisk). Obecna wersja jest przeterminowana.",
                    cTomasz);

            // 4 — IN_PROGRESS (operator: jan.kowalski)
            Ticket t4 = ticketService.createTicket(
                    "Awaria serwera pocztowego",
                    "Serwer pocztowy nie odpowiada od godziny 08:30. Pracownicy nie mogą " +
                    "odbierać ani wysyłać wiadomości e-mail. Priorytet wysoki.",
                    cAgnieszka);
            t4 = ticketService.startTicket(t4, opJan,
                    LocalDateTime.now().plusDays(1));

            // 5 — IN_PROGRESS (operator: anna.nowak)
            Ticket t5 = ticketService.createTicket(
                    "Konfiguracja VPN dla pracowników zdalnych",
                    "Trzy nowe osoby w dziale IT potrzebują skonfigurowanego dostępu VPN " +
                    "do sieci firmowej. Konta Active Directory już istnieją.",
                    cPiotr);
            t5 = ticketService.startTicket(t5, opAnna,
                    LocalDateTime.now().plusDays(3));

            // 6 — DELAYED (operator: jan.kowalski, estimated time in the past)
            Ticket t6 = ticketService.createTicket(
                    "Migracja danych do nowego systemu ERP",
                    "Przeniesienie danych historycznych (faktury, zamówienia, kartoteki) " +
                    "ze starego systemu Symfonia do nowego systemu ERP. Zakres: 5 lat wstecz.",
                    cMarek);
            t6 = ticketService.startTicket(t6, opJan,
                    LocalDateTime.now().minusDays(2)); // estimated in the past → DELAYED
            ticketService.refreshDelayedStatuses();    // triggers DELAYED status update

            // 7 — COMPLETED (operator: anna.nowak)
            Ticket t7 = ticketService.createTicket(
                    "Instalacja systemu Windows 11",
                    "Wymiana systemu operacyjnego na trzech stanowiskach graficznych " +
                    "z Windows 10 na Windows 11. Wymagana migracja ustawień użytkowników.",
                    cKasia);
            t7 = ticketService.startTicket(t7, opAnna,
                    LocalDateTime.now().plusDays(1));
            t7 = ticketService.changeStatus(t7, TicketStatus.COMPLETED);

            // 8 — COMPLETED (operator: jan.kowalski)
            Ticket t8 = ticketService.createTicket(
                    "Wymiana uszkodzonej klawiatury i myszy",
                    "Klient zgłasza uszkodzoną klawiaturę (kilka klawiszy nie reaguje) " +
                    "oraz mysz (scroll nie działa). Prośba o wymianę sprzętu.",
                    cTomasz);
            t8 = ticketService.startTicket(t8, opJan,
                    LocalDateTime.now().plusHours(4));
            t8 = ticketService.changeStatus(t8, TicketStatus.COMPLETED);

            // 9 — CANCELLED
            Ticket t9 = ticketService.createTicket(
                    "Zamówienie dodatkowych monitorów",
                    "Prośba o zamówienie 4 monitorów 27\" 4K do nowych stanowisk. " +
                    "Budżet zatwierdzony przez kierownictwo.",
                    cAgnieszka);
            t9 = ticketService.changeStatus(t9, TicketStatus.CANCELLED);

            System.out.println("  ✓ Zgłoszenia (9) utworzone.");

            // ── Comments (25) ─────────────────────────────────────────────────

            // Ticket 1 — NEW (2 comments)
            commentService.addComment(t1, "Marek Wiśniewski",
                    "Hasło zostało zmienione przez dział IT w piątek wieczorem. " +
                    "Od soboty rano nie mogę się zalogować.");
            commentService.addComment(t1, "jan.kowalski",
                    "Sprawdzę ustawienia konta w Active Directory. Proszę o chwilę cierpliwości.");

            // Ticket 2 — NEW (2 comments)
            commentService.addComment(t2, "Katarzyna Zielińska",
                    "Drukarka była sprawna do wczoraj do godziny 15:00. " +
                    "Po restarcie serwera wydruku przestała być widoczna.");
            commentService.addComment(t2, "anna.nowak",
                    "Sprawdzę konfigurację serwera wydruku i adres IP drukarki. " +
                    "Czy był wykonywany restart infrastruktury sieciowej?");

            // Ticket 3 — NEW (1 comment)
            commentService.addComment(t3, "Tomasz Dąbrowski",
                    "Proszę o wykonanie aktualizacji w godzinach wieczornych, " +
                    "aby nie przerywać pracy pracowników.");

            // Ticket 4 — IN_PROGRESS (4 comments)
            commentService.addComment(t4, "Agnieszka Lewandowska",
                    "Sytuacja jest krytyczna — oczekujemy na ważne oferty handlowe. " +
                    "Kiedy szacowane przywrócenie usługi?");
            commentService.addComment(t4, "jan.kowalski",
                    "Zdiagnozowałem problem — uszkodzony dysk w RAID. " +
                    "Trwa odbudowa macierzy. Szacowany czas: 3-4 godziny.");
            commentService.addComment(t4, "Agnieszka Lewandowska",
                    "Dziękuję za informację. Poinformuję pracowników.");
            commentService.addComment(t4, "jan.kowalski",
                    "Odbudowa RAID zakończona w 60%. Serwer powinien wrócić do działania " +
                    "zgodnie z planem.");

            // Ticket 5 — IN_PROGRESS (3 comments)
            commentService.addComment(t5, "Piotr Wójcik",
                    "Nowi pracownicy zaczną pracę w poniedziałek — " +
                    "VPN jest im niezbędny od pierwszego dnia.");
            commentService.addComment(t5, "anna.nowak",
                    "Skonfigurowano certyfikaty dla dwóch z trzech kont. " +
                    "Trzecie konto ma problem z polityką haseł — wyjaśniam z działem HR.");
            commentService.addComment(t5, "Piotr Wójcik",
                    "Dobrze wiedzieć, dziękuję za aktualizację statusu.");

            // Ticket 6 — DELAYED (4 comments)
            commentService.addComment(t6, "Marek Wiśniewski",
                    "Kiedy planowane zakończenie migracji? Zarząd pyta o harmonogram.");
            commentService.addComment(t6, "jan.kowalski",
                    "Napotkałem problemy z formatem danych z 2019 roku — " +
                    "faktury mają inną strukturę niż dokumentacja zakłada. Analizuję.");
            commentService.addComment(t6, "admin",
                    "Zlecenie przekroczyło planowany czas realizacji. " +
                    "Proszę o raport z postępu prac i nowy termin zakończenia.");
            commentService.addComment(t6, "jan.kowalski",
                    "Znalazłem skrypt konwertujący stary format. " +
                    "Nowy szacowany czas zakończenia: 3 dni robocze.");

            // Ticket 7 — COMPLETED (4 comments)
            commentService.addComment(t7, "Katarzyna Zielińska",
                    "Czy podczas instalacji zostanie zachowana wersja Adobe Creative Suite?");
            commentService.addComment(t7, "anna.nowak",
                    "Tak, przed instalacją wykonam kopię zapasową profilu użytkownika " +
                    "oraz listę zainstalowanego oprogramowania.");
            commentService.addComment(t7, "anna.nowak",
                    "Instalacja zakończona na wszystkich 3 stanowiskach. " +
                    "Adobe CC działa poprawnie. Ustawienia użytkowników zostały przeniesione.");
            commentService.addComment(t7, "Katarzyna Zielińska",
                    "Wszystko działa świetnie! Bardzo szybka realizacja, dziękuję.");

            // Ticket 8 — COMPLETED (3 comments)
            commentService.addComment(t8, "jan.kowalski",
                    "Sprzęt zamieniony. Nowa klawiatura i mysz pobrane z magazynu.");
            commentService.addComment(t8, "Tomasz Dąbrowski",
                    "Potwierdzam odbiór nowego sprzętu. Wszystko działa poprawnie.");
            commentService.addComment(t8, "jan.kowalski",
                    "Zlecenie zamknięte. Stary sprzęt przekazany do utylizacji.");

            // Ticket 9 — CANCELLED (2 comments)
            commentService.addComment(t9, "admin",
                    "Zamówienie wstrzymane decyzją zarządu z powodu rewizji budżetu IT. " +
                    "Zlecenie zostaje anulowane.");
            commentService.addComment(t9, "Agnieszka Lewandowska",
                    "Rozumiem. Złożymy wniosek ponownie w następnym kwartale.");

            System.out.println("  ✓ Komentarze (25) dodane.");

            // ── Audit logs (~10) ──────────────────────────────────────────────

            // Ticket 4 — server outage (2 logs)
            logService.log(t4, "Zgłoszenie przyjęte i przekazane do realizacji.", "admin");
            logService.log(t4, "Operator jan.kowalski rozpoczął realizację zlecenia.", "jan.kowalski");

            // Ticket 5 — VPN setup (2 logs)
            logService.log(t5, "Zgłoszenie przyjęte i przekazane do realizacji.", "admin");
            logService.log(t5, "Operator anna.nowak rozpoczął realizację zlecenia.", "anna.nowak");

            // Ticket 6 — ERP migration, delayed (3 logs)
            logService.log(t6, "Zgłoszenie przyjęte i przekazane do realizacji.", "admin");
            logService.log(t6, "Operator jan.kowalski rozpoczął realizację zlecenia.", "jan.kowalski");
            logService.log(t6, "Status zmieniony na OPÓŹNIONE — przekroczono planowany termin zakończenia.", "system");

            // Ticket 7 — Windows 11 install, completed (2 logs)
            logService.log(t7, "Operator anna.nowak rozpoczął realizację zlecenia.", "anna.nowak");
            logService.log(t7, "Zlecenie zakończone pomyślnie. Status zmieniony na ZAKOŃCZONE.", "anna.nowak");

            // Ticket 8 — hardware swap, completed (2 logs)
            logService.log(t8, "Operator jan.kowalski rozpoczął realizację zlecenia.", "jan.kowalski");
            logService.log(t8, "Zlecenie zakończone pomyślnie. Status zmieniony na ZAKOŃCZONE.", "jan.kowalski");

            System.out.println("  ✓ Logi audytowe (12) zapisane.");

            // ── Summary ───────────────────────────────────────────────────────
            System.out.println(ConsoleUtil.SEP);
            System.out.println("  DANE DEMO ZAŁADOWANE POMYŚLNIE");
            System.out.println("  ┌──────────────────────────────────────────────────────┐");
            System.out.println("  │  Konta operatorów:                                   │");
            System.out.println("  │    jan.kowalski  / haslo123                          │");
            System.out.println("  │    anna.nowak    / haslo123                          │");
            System.out.println("  │                                                      │");
            System.out.println("  │  Dane: 5 klientów, 9 zgłoszeń, 25 komentarzy,       │");
            System.out.println("  │        12 logów audytowych                           │");
            System.out.println("  └──────────────────────────────────────────────────────┘");
            System.out.println(ConsoleUtil.SEP);

        } catch (Exception e) {
            System.err.println("BŁĄD podczas seedowania danych demo: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
