# System Ticketowy

Prosty system ticketowy napisany w **Java 21** z interfejsem CLI, bazą danych **MySQL 8.4** (Docker) i ORM **Hibernate 6**.

---

## Wymagania

| Narzędzie | Wersja |
|-----------|--------|
| Java JDK  | 21+    |
| Maven     | 3.8+   |
| Docker & Docker Compose | dowolna aktualna |

---

## Szybki start

### 1. Uruchom bazę danych (Docker)

```bash
docker-compose up -d
```

Poczekaj ~10 sekund na pełną inicjalizację MySQL. Baza `ticketsystem` zostanie utworzona automatycznie.

Zatrzymanie bazy:
```bash
docker-compose down          # zatrzymaj, zachowaj dane
docker-compose down -v       # zatrzymaj i usuń dane (reset)
```

### 2. Zbuduj projekt

```bash
mvn clean package -q
```

Powstanie plik `target/ticket-system.jar` z wszystkimi zależnościami.

### 3. Uruchom aplikację

```bash
java -jar target/ticket-system.jar
```

Lub w IntelliJ: uruchom klasę `com.ticketsystem.Main`.

---

## Pierwsze uruchomienie

Przy pierwszym starcie aplikacja automatycznie tworzy konto administratora:

```
Użytkownik:  admin
Hasło:       admin123
```

> **Zmień hasło po pierwszym logowaniu** (funkcja w planach jako Workflow, na razie przez bezpośrednie uruchomienie WorkflowG i usunięcie starego konta).

---

## Dostępne operacje (Workflow A–P)

| Klawisz | Nazwa | Kto może |
|---------|-------|----------|
| **A** | Dodaj zlecenie | Wszyscy |
| **B** | Modyfikuj zlecenie (tytuł/opis) | Wszyscy |
| **E** | Dodaj komentarz do zlecenia | Wszyscy |
| **P** | Podejrzyj zlecenie po UUID | Wszyscy |
| **N** | Zaloguj się | Wszyscy |
| **C** | Rozpocznij zlecenie | Operator / Admin |
| **D** | Zmień status zlecenia | Operator / Admin |
| **F** | Przeglądaj listę zleceń | Operator / Admin |
| **K** | Moje zlecenia (przypisane) | Operator / Admin |
| **O** | Wyloguj się | Zalogowany |
| **G** | Dodaj operatora / administratora | Admin |
| **H** | Modyfikuj / usuń komentarz | Admin |
| **I** | Usuń użytkownika systemu | Admin |
| **J** | Odśwież statusy (oznacz opóźnione) | Admin |
| **L** | Dodaj klienta | Admin |
| **M** | Usuń klienta | Admin |
| **Q** | Wyjście | Wszyscy |

---

## Statusy zleceń

```
NEW (Nowe)
  └─► IN_PROGRESS (W realizacji)  ← operator[C]
         ├─► DELAYED (Opóźnione)  ← auto[J] gdy przekroczono czas
         │      └─► IN_PROGRESS / COMPLETED / CANCELLED  ← operator[D]
         └─► COMPLETED (Zakończone)  ← operator[D]
         └─► CANCELLED (Anulowane)   ← operator[D]
```

---

## Struktura projektu

```
ticket-system/
├── docker-compose.yml
├── pom.xml
└── src/main/java/com/ticketsystem/
    ├── Main.java
    ├── DataSeeder.java
    ├── model/
    │   ├── enums/            # TicketStatus, UserRole
    │   ├── Client.java
    │   ├── User.java          # abstrakcyjna baza (SINGLE_TABLE)
    │   ├── Operator.java
    │   ├── Administrator.java
    │   ├── Ticket.java
    │   ├── Comment.java
    │   └── TicketLog.java
    ├── repository/
    │   ├── GenericRepository.java   (interfejs)
    │   ├── AbstractRepository.java  (bazowa impl.)
    │   ├── TicketRepository.java
    │   ├── ClientRepository.java
    │   ├── UserRepository.java
    │   ├── CommentRepository.java
    │   └── LogRepository.java
    ├── service/
    │   ├── TicketService.java
    │   ├── ClientService.java
    │   ├── UserService.java
    │   ├── CommentService.java
    │   ├── LogService.java
    │   └── AuthService.java
    ├── cli/
    │   ├── Workflow.java       (interfejs)
    │   ├── CLI.java            (pętla główna + dispatcher)
    │   ├── MenuPrinter.java
    │   └── workflows/
    │       └── WorkflowA..P.java
    └── util/
        ├── HibernateUtil.java
        ├── SessionManager.java
        ├── PasswordUtil.java
        └── ConsoleUtil.java
```

---

## Schemat bazy danych (auto-generowany przez Hibernate)

| Tabela | Opis |
|--------|------|
| `clients` | Klienci (imię, nazwisko, UUID) |
| `users` | Operatorzy i administratorzy (SINGLE_TABLE: kolumna `role`) |
| `tickets` | Zlecenia — powiązane z klientem i operatorem |
| `comments` | Komentarze — relacja wiele-do-jednego z tickets |
| `ticket_logs` | Logi audytu — relacja wiele-do-jednego z tickets |

---

## Zasady bezpieczeństwa

- Hasła hashowane **BCrypt** (work factor 12) — nigdy plain text w bazie
- Brak możliwości usunięcia własnego konta przez administratora
- Operator może zmieniać status tylko **własnych** (przypisanych do niego) zleceń
- Logi audytu są **append-only** — nie można ich edytować przez CLI

---

## Rozszerzenia (sugestie na przyszłość)

- Workflow J jako **cron / scheduled task** (np. Spring Scheduler)
- Zmiana hasła przez zalogowanego użytkownika
- Eksport zleceń do CSV / PDF
- REST API jako alternatywny interfejs (Spring Boot)
- Testy jednostkowe / integracyjne (JUnit 5 + Testcontainers)
