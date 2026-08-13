# CiboAmico — Report Audit Codice (Errori & Raccomandazioni)

**Progetto:** CiboAmico (ISPW, Tor Vergata, A.A. 2025/26)
**Scopo:** Verifica completa di errori nel codice, con consultazione NotebookLM (notebook ispw).
**Metodo:** `mvn clean verify` + audit statico + interrogazione NotebookLM (slide Falessi / esempi 30/30).

---

## ✅ Verifica complessiva (build & test)

| Metrica | Risultato |
|---|---|
| `mvn clean verify` | **BUILD SUCCESS** |
| Test eseguiti | **118, 0 falliti, 0 errori** |
| Quality gate JaCoCo (LINE ≥ 80%) | **PASSED** ("All coverage checks have been met") |
| Compilazione | Nessun errore di compilazione |
| Warning compile | 1 warning "unchecked" in `InventarioView` (falso positivo JavaFX `PropertyValueFactory`) |
| Riferimenti a classi obsolete | Nessuno (refactoring Observer pulito) |

**Il codice NON ha errori bloccanti**: compila, i test passano, il quality gate è rispettato.

---

## 🔴 Errori / Problemi da CORREGGERE (raccomandati da NotebookLM per il 30)

### 1. Codice morto: `PersistenceTypeEnum` (ALTA priorità)
- **File:** `src/main/java/it/uniroma2/ispw/ciboamico/enums/PersistenceTypeEnum.java`
- **Problema:** enum definito ma **mai usato** in tutto il progetto (solo referenziato da se stesso). `ApplicationModeManager` usa costanti `String MODE_JDBC/FS/DEMO` + `switch`, non l'enum.
- **Impatto:** SonarCloud lo segnala come **Code Smell** → compromette il Quality Gate. È la violazione "Codice Morto" citata da NotebookLM [43-47].
- **Fix:** Cancellare l'enum (o se vuoi usarlo, integrare `PersistenceTypeEnum` in `ApplicationModeManager` al posto delle costanti String).

### 2. `SessionManager` lazy non thread-safe (priorità media)
- **File:** `src/main/java/it/uniroma2/ispw/ciboamico/pattern/singleton/SessionManager.java`
- **Problema:** `getInstance()` usa `if (instance == null) instance = new SessionManager()` **senza `synchronized`**. In app a singolo thread è sicuro (non ci sono thread nel progetto), ma non è thread-safe in generale.
- **Impatto:** NotebookLM consiglia l'idioma **inner static class `Container`** per thread-safety intrinseca [12,13]. **Nota di coerenza:** `OrdineLazyFactory` usa già il pattern `Container`+`synchronized`; `SessionManager` è l'unico singleton lazy non sync → **incoerenza interna**.
- **Fix:** Usare l'idioma Holder `Container` (come `OrdineLazyFactory`), oppure aggiungere `synchronized` a `getInstance()`. *NB: questo annullerebbe lo "stile amico" se preferisci il lazy semplice — ma NotebookLM raccomanda il Container per il 30.*

### 3. Password hashate con salt fisso hardcoded (priorità media)
- **File:** `src/main/java/it/uniroma2/ispw/ciboamico/entity/Utente.java` (riga 19: `SALT = "ciboamico-salt"`)
- **Problema:** SHA-256 con **salt fisso scritto nel codice**. NFR-03 è soddisfatto ma con pattern debole per SonarCloud (Security Hotspot / Vulnerability).
- **Impatto:** NotebookLM consiglia: (a) salt **dinamico per-utente** salvato accanto all'hash, oppure (b) salt **in `config.properties`** fuori dal codice [5,7].
- **Fix (minimo):** spostare il salt in `config.properties` e leggerlo all'avvio; o generare salt per-utente.

### 4. Connessioni JDBC aperte e chiuse per ogni query (priorità media/bassa)
- **File:** `persistence/impl/jdbc/ConnectionManager.java` + tutti i `JDBCOrdineDAO/JDBCProdottoDAO/...`
- **Problema:** ogni metodo DAO apre/chide la connessione via `DriverManager.getConnection(...)` per singola query (`try (Connection conn = ConnectionManager.getConnection()...)`).
- **Impatto:** violazione #6 di NotebookLM [39-42] — apertura connessione costosissima. Inoltre la **password DB è hardcoded di default** (`root`/`root`) via system properties.
- **Fix consigliato:** connection manager **Singleton con pool** (una sessione per app); e leggere credenziali da `config.properties`, mai default hardcoded.

### 5. (Basso) DAO che ritornano `null`
- I DAO (`findByNome`, `findById`, `findByEmail`) ritornano `null` se non trovato. I controller gestiscono il null con eccezioni (OK).
- **Nota dal pattern BCE/GRASP:** alcuni critici suggeriscono `Optional<T>` o eccezione dedicata per evitare "null flow". Non è bloccante, ed è coerente con gli esempi. Facoltativo.

---

## 🟢 Cose già corrette / niente problemi
- **Refactoring Observer** completo e pulito: `OrdineEventPublisher` (singleton) + DTO `OrdineEvent`, niente residui di `OrdineSubject`/`onStatoCambiato`.
- `Ordine` è entity pura (nessun subject incorporato).
- `OrdineLazyFactory`: singleton configurata correttamente con `synchronized` + fail-fast se non configurata.
- Eccezioni custom coerenti (chaining con `Throwable cause`), nessun catch-and-rethrow.
- Nessun TODO/FIXME/HACK nel codice.
- Boundary gestiscono gli errori con messaggi user-friendly (niente exception non gestite).
- `System.out` solo nelle view CLI (legittimo).

---

## 🎯 Priorità di intervento (per prendere 30 all'orale)
1. **Cancellare `PersistenceTypeEnum`** (code smell → rompe SonarCloud) — 5 min.
2. **`SessionManager` → Container idiom** (thread-safety + coerenza con OrdineLazyFactory) — 5 min.
3. **Salt `"ciboamico-salt"` → spostare in `config.properties`** o salt per-utente (hotspot sicurezza) — 15 min.
4. (Opzionale) **ConnectionManager con pool** (solo se dimostri JDBC; DEMO/FS non lo richiede).

Dopo ogni fix: rilanciare `mvn clean verify` e poi SonarCloud per confermare Quality Gate 0 violazioni.

---

## Fonti NotebookLM (citate nel test)
Per dettagli vedere le fonti dell'ultima interrogazione (slide Falessi: connesioni JDBC [39-42], codice morto/SonarCloud [43-47], singleton Container [12,13], salt/hotspot [5,7]).

*Report generato automaticamente — verificare le correzioni prima della consegna.*
