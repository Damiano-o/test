# CiboAmico 🧊

> **CiboAmico** — monitoraggio frigorifero/dispensa + suggerimento ricette intelligente + marketplace locale.
> Progetto finale **ISPW** — Tor Vergata, A.A. 2025-2026 (Prof. Falessi & De Angelis).

## 📦 Requisiti

- Java 17
- Maven 3.9+
- JavaFX 17.0.2 (dipendenze nel `pom.xml`)
- MySQL (opzionale, per modalità JDBC) — default: **DEMO in-memory**

## 🚀 Build & Run

```bash
mvn compile          # compila
mvn test             # esegue i test (92 test)
mvn jacoco:report    # report coverage (target/site/jacoco)
mvn verify           # test + quality gate JaCoCo ≥ 80%
mvn javafx:run       # avvia l'app GUI JavaFX (modalità DEMO con dati seed)
```

Doppia interfaccia (Abstract Factory `ViewFactory`):

```bash
mvn javafx:run              # GUI (default)
mvn exec:java -Dexec.mainClass="it.uniroma2.ispw.ciboamico.bootstrap.MainCLI"   # CLI
```

Credenziali demo (modalità DEMO): `mario@cibo.it`, `marco@cibo.it`,
`anna@cibo.it`, `admin@cibo.it` — password `password123`.

## 🏗️ Architettura

- **BCE** (Boundary-Control-Entity): 10 Boundary JavaFX ↔ 9 Controller (1:1 con UC) ↔ Entity
- **Bean/DTO**: unico canale Boundary→Control (mai Entity verso la GUI)
- **Pattern GoF**: Abstract Factory (DAO), Singleton (SessionManager, ApplicationModeManager), Lazy Initialization + caching (OrdineLazyFactory), Strategy (matching ricette), Facade (motore ricette), Observer (notifiche ordine), Adapter (OpenFoodFacts)
- **Persistenza doppia**: JDBC (MySQL) ↔ FS (JSON) ↔ Demo — switch via `ApplicationModeManager` (NFR-01)
- **Whole-Part**: `Utente` aggrega `Ruolo` (metamorfosi dinamica dei ruoli)

## 🧪 Test

**92 test JUnit 5 + Mockito — tutti verdi ✅**

```bash
mvn test          # 92 test
mvn verify        # test + quality gate JaCoCo ≥ 80% (attuale: 81.4%)
mvn jacoco:report # report HTML in target/site/jacoco
```

Copertura per area (esclusi DAO JDBC/boundary — richiedono MySQL/display):
- Strategy matching (T01-T04) · Facade · Ordine/BR-04 (T05-T07)
- Prodotto/BR-06 (T08-T09) · Autenticazione (T10-T12) · Inventario/scadenze (T16)
- Whole-part ruoli · Observer · Bean DTO · DAO Demo/FS roundtrip · Controller ×8

## 📁 Struttura

```
src/main/java/it/uniroma2/ispw/ciboamico/
├── bootstrap/     # Main, ApplicationModeManager
├── boundary/      # View JavaFX (colore + b/n)
├── control/       # 11 controller applicativi (1:1 UC)
├── entity/        # Utente+Ruoli, Prodotto, Ricetta, Ordine...
├── bean/          # DTO puri
├── persistence/   # dao/ + factory/ + impl/{jdbc,fs,demo}
└── pattern/       # singleton, facade, strategy, observer, adapter
```

## 📚 Documentazione

La documentazione completa (requisiti, analisi, design, testing, relazione) è nel vault Obsidian:
`01 - Corsi/ISPW/04_Progetto/`

## ✅ Verifica finale NotebookLM (2026-08-02)

12 punti verificati: **11 ✅ corretto, 1 ❌ bug corretto**:
- BUG: `OrdinaProdottoController` aveva ternary inutile (`compratore : compratore`)
  → fix: venditore risolto da `prodotto.getVenditore().getUtente()` (back-reference whole-part)
- Test regressione aggiunto: `OrdinaProdottoControllerTest` (3 test)

**Gap analysis prioritaria (prima della consegna)**:
1. 10 View JavaFX rimanenti (12h · Scene Builder) — solo LoginView implementata
2. Schema SQL MySQL `CiboAmico.sql` (3h · MySQL Workbench) — CREATE TABLE ×7
3. `OpenFoodFactsAdapter` + `JakartaMailAdapter` stub concreti (4h)
4. SonarCloud: token + quality gate (2h) — exclusion `boundary/**`
5. Relazione PDF LaTeX 9 capitoli (10h · Overleaf) — skill ieee-report
6. Video dimostrativo `.mp4` (2h · OBS)
