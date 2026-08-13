# CiboAmico — Report Validazione Design Patterns

**Progetto:** CiboAmico (ISPW, Tor Vergata, A.A. 2025/26)
**Scopo:** Verifica che i pattern applicati al codice rispettino la teoria del corso (De Angelis, `TutteLezioni_ISPW.pdf`) e i codici di esempio delle lezioni (`CodiciProfessore`).
**Metodo:** lettura integrale della teoria (pattern GoF, BCE, MVC/MVP, Facade) + confronto con i codici d'esempio delle lezioni 29-30 (GoF1), 33-34 (GoF2) e con l'approccio del pattern di riferimento (progetto amico).
**Data:** 2026-08-11

---

## ✅ Verifica complessiva

| Metrica | Risultato |
|---|---|
| Pattern analizzati | Abstract Factory, Singleton, Observer, Facade, MVC controller grafico/applicativo, (Strategy) |
| Coerenza con la teoria | ✅ Tutti i pattern principali coerenti con le slide De Angelis |
| Coerenza con i codici d'esempio | ✅ Stessa struttura dei codici delle lezioni |
| Build & test | **121 test verdi, 0 falliti** |
| Quality gate JaCoCo (LINE ≥ 80%) | **PASSED** |

**Il codice risulta globalmente allineato alla teoria e agli esempi del professore.**

---

## 📐 Struttura architettonica finale (MVC — variante "disaccoppiati" + Facade)

```
View (puro layout JavaFX/CLI)
   └──> Controller Grafico (coordina View ↔ applicativo, converte formati, testabile, no-UI)
           └──> Facade (interfaccia unificata verso il sottosistema di business)
                   └──> Controller Applicativi (logica di business, state-less)
                           └──> Entity / DAO (persistenza)
```

Questa è **esattamente** la struttura del pattern di riferimento: il controller grafico comunica **solo tramite Facade** ("Facade: comunica solo con `CreaOrdineFacade`"), e il controller applicativo è separato e state-less.

---

## 1. Abstract Factory — ✅ Coerente

**Teoria (lezioni 29-30):** *"fornire un'interfaccia per la creazione di famiglie di oggetti correlati"*; la factory generale "più generale come classe astratta, eventualmente anche come **interfaccia**"; le concrete factory generano una famiglia coerente; preferite istanze uniche.

**Codice d'esempio (Prof.):** `WidgetFactory` (abstract/interface) + concrete `MotifWidgetFactory`/`PMWidgetFactory` che implementano `createWindow()`/`createScrollBar()`; combinazione con Singleton in `BetterClient` (`getWidgetFactory()` lazy).

| Concetto GoF | Esempio prof. | CiboAmico | Esito |
|---|---|---|---|
| AbstractFactory | `WidgetFactory` | `DAOFactory` (**interfaccia**) | ✅ |
| ConcreteFactory | `MotifWidgetFactory`, `PMWidgetFactory` | `JDBCDAOFactory`, `FSDAOFactory`, `DemoDAOFactory` (`implements DAOFactory`) | ✅ |
| AbstractProduct | `Window`, `ScrollBar` | DAO (`OrdineDAO`, `ProdottoDAO`, `UtenteDAO`, `BuonoDAO`) | ✅ |
| Client dipende solo da interfacce | `Client` | `ApplicationModeManager`, controller | ✅ |
| AF + Singleton (istanza unica) | `getWidgetFactory()` lazy | `ApplicationModeManager.getDAOFactory()` (istanza cache) | ✅ |

**Nota:** le concrete factory `implements` l'interfaccia `DAOFactory` (non `extends` abstract class), fedele all'opzione "anche come interfaccia".

---

## 2. Singleton — ✅ Coerente

**Teoria (lezioni 29-30):** *"assicurare che una classe abbia una sola istanza";* per l'implementazione: *"costruttore privato, variabile di classe privata, metodo di classe che restituisce la variabile di classe"*; *"i Client possono accedere a un singleton soltanto attraverso l'operazione getSingletonInstance"*.

**Codice d'esempio (Prof.):** `SingletonClass` (lazy `if (instance == null) instance = new ...`) e `LazySingletonClass` (inner static class `LazyContainer`).

| Singleton CiboAmico | Costruttore | Lazy | Thread-safe | Uso esterno |
|---|---|---|---|---|
| `SessionManager` | `private` | `if (instance == null)` | `synchronized getInstance()` | ✅ solo `getInstance()` |
| `ApplicationModeManager` | `private` | `if (instance == null)` | `synchronized getInstance()` | ✅ solo `getInstance()` |
| `OrdineEventPublisher` | `private` | `if (instance == null)` | `synchronized getInstance()` | ✅ solo `getInstance()` |

Tutti e tre seguono il **`SingletonClass`** del prof (lazy + synchronized), costruttore privato, accesso esclusivo via `getInstance()`.

---

## 3. Observer — ✅ Coerente

**Teoria (lezioni 33-34):** *"definire una dipendenza uno-a-molti ... se un oggetto cambia stato, tutti gli oggetti dipendenti sono notificati";* Subject (conosce lista di Observer conformi a interfaccia, NON le classi concrete); Observer (interfaccia di notifica); ConcreteObserver.

**Codice d'esempio (Prof.):** `Subject` (attach/detach/notify, notifica su **copia** della lista per sicurezza), `Observer` interface, `ChatClient` (ConcreteSubject).

| Concetto GoF | Esempio prof. | CiboAmico | Esito |
|---|---|---|---|
| Subject | `Subject` | `OrdineEventPublisher` (singleton) | ✅ |
| Observer (interfaccia) | `Observer` | `OrdineEventListener` | ✅ |
| ConcreteObserver | `ComeBackObserver`, `OnOffObserver` | `VenditoreNotifier`, `UtenteNotifier` | ✅ |
| Notifica su copia lista | `new ArrayList<>(observers)` | `new ArrayList<>(listeners)` | ✅ |
| Broadcast a tutti gli observer | sì | sì | ✅ |

**Coerenza con la memoria (variante "push con DTO"):** la notifica passa il DTO `OrdineEvent` in sola lettura, mai l'entità dominio → disaccoppiamento totale, conforme al GoF (Observer non conosce ConcreteObserver).

---

## 4. Facade — ✅ Coerente

**Teoria (lezioni 33-34):** *"fornire una interfaccia unificata ad un insieme di interfacce di un sottosistema";* *"i Client comunicano con il sistema attraverso l'interfaccia comune esportata (Façade); i Client NON hanno in alcun modo accesso agli oggetti dei sottosistemi";* *"un client per realizzare una singola operazione logica deve accedere a più classi del sottosistema"*.

**Codice d'esempio (Prof.):** `FullCompilerFacade` (incapsula `Scanner/Parser/Assembler/Linker` privati, orchestra la sequenza), `MinimalClient` (chiama solo la Facade).

| Concetto Facade | Esempio prof. | CiboAmico | Esito |
|---|---|---|---|
| Incapsula sottosistema | `scanner/parser/assembler/linker` privati | `controller` privato + sottocontroller | ✅ |
| Client → solo Facade | `MinimalClient` usa `ParseCheckFacade` | View/controller grafico usano `OrdinaProdottoFacade`/`AutenticazioneFacade` | ✅ |
| Orchestra la sequenza | `compile()` | `processaPagamento()`, `applicaBuono()` | ✅ |

**Decisione architetturale (per la relazione):** il Facade **non è eliminato** in presenza del controller grafico; coesiste come interfaccia verso il sottosistema di business (stessa scelta del progetto di riferimento).

---

## 5. MVC: controller grafico e applicativo disaccoppiati — ✅ Coerente

**Teoria (lezioni 37-38):** due ruoli distinct:
- **controller grafico**: *"coordina l'interazione tra View ed il Controller Applicativo ... converte formati esterno in interno ... realizza la mappa tra input utente e processi ... crea/seleziona le istanze di View"*
- **controller applicativo**: *"implementa la logica di controllo dell'applicazione ... raffina il Controller in BCE ... state-less, non ha memoria"*
- Variante **disaccoppiati**: *"il controller grafico viene associato a parte della view; il controller grafico invoca operazioni sul controller applicativo"*

**Codice di riferimento (progetto amico):** `LoginGraphicControl` (grafico: FXML, eventi, Alert, instanzia `LoginControl`) e `CreaOrdineGUIController` (grafico che parla solo al `CreaOrdineFacade`); controller applicativi separati (`CreaOrdineController`, `LoginControl`).

| Concetto MVC | CiboAmico | Esito |
|---|---|---|
| Controller grafico | `MarketplaceGraphicController`, `PaymentGraphicController` | ✅ |
| Controller applicativo | `OrdinaProdottoController`, `PagamentoController`, `ApplicaBuonoPromozionaleController` | ✅ |
| View (puro layout) | `MarketplaceView`, `PaymentView`, `LoginView` | ✅ |
| Filiera grafico → Facade → applicativo | sì | ✅ |
| Controller applicativo state-less | sì (nessuno stato in-memory multi-utente) | ✅ |

---

## 6. Strategy (bonus) — ✅ Coerente

**Teoria:** lo State è "Strategy Pattern (dove la strategia rappresenta gli stati)"; lo Strategy GoF definisce algoritmi intercambiabili.

**CiboAmico:** `ScontoStrategy` (interfaccia) con concrete `ScontoPercentualeStrategy`, `ScontoImportoFissoStrategy` e `ScontoStrategyFactory` (selezione per tipo, come factory del prof). Pattern applicato correttamente. **Non è tra i pattern citati nelle sezioni principali della relazione** (governato dal buono promozionale), resta come dettaglio di implementazione.

---

## 🔴 Note / raccomandazioni residue (non bloccanti)

1. **`ScontoStrategyFactory`** usa un `switch` su String per selezionare la strategia. È lecito (come `WidgetFactory.getFactory` del prof), ma potrebbe usare l'enum per robustezza. Bassa priorità.
2. **Testing controller grafico**: i controller grafici sono testabili in isolamento (8 test dedicati) — vantaggio dichiarato della variante disaccoppiata. ✅

---

## Verdetto finale

**Il codice di CiboAmico applica correttamente i pattern insegnati (Abstract Factory, Singleton, Observer, Facade) con la variante MVC disaccoppiata (controller grafico separato + Facade + controller applicativo), coerente con la teoria di De Angelis, i codici d'esempio delle lezioni e il progetto di riferimento.**

**121 test verdi, quality gate JaCoCo superato. Nessuna correzione bloccante.**

---

## 📊 Verifica schemi (figure) nella relazione

Gli schemi `dl_*_hr.png` usati nella relazione (disegnati a mano con draw.io) sono stati verificati contro la teoria:

| Schema | File | Conformità teorica | Note |
|---|---|---|---|
| **Abstract Factory** | `dl_factory_hr.png` | ✅ Conforme | `DAOFactory` (interface) + concrete `implements`; `..|>` realizzazione; `<<create>>` verso i product; `..>` dependency |
| **Singleton** | `dl_singleton_hr.png` | ✅ Conforme (1 refuso) | `getInstance()` sottolineato (static), costruttore privato. Refuso: attributo `instance` non marcato `{static}`/sottolineato (regola UML: variabile di classe va evidenziata). Non bloccante |
| **Observer** | `dl_observer_hr.png` | ✅ Conforme | Subject (`OrdineEventPublisher`, `<MODULE>singleton>>`) con `List<OrdineEventListener>` (observer astratto); ConcreteObserver (`VenditoreNotifier`/`UtenteNotifier`) `..|>` interfaccia; broadcast; DTO `OrdineEvent` |
| **Facade** | `dl_facade_hr.png` | ✅ Conforme | Boundary (View) → Controller Grafico → Facade → Controller applicativi → Bean; Facade incapsula sottosistema |
| **VOPC** | `vopc_uc04.png` | ✅ Conforme (BCE puro) | SOLO Boundary/Control/Entity (rimossi Facade/grafico/bean); 3 control (base + 2 extend, rapporto 1:1 per UC); entity condivise |

**Unico refuso teorico**: attributo `instance` nel Singleton andrebbe marcato come membro di classe (`{static}`/sottolineato) per fedeltà UML. Se si vuole correggere nell'immagine, si aggiorna il `.drawio` sorgente e si rigenera l'`_hr.png`.

