# Report — Refactor architettura controller + validazione

**Data:** 10-08-2026
**Progetto:** CiboAmico (ISPW, Tor Vergata, UC-04 Ordina un Prodotto, UC-11 Autenticazione)

Questo report documenta tre interventi di ingegnerizzazione:
1. Introduzione dei pattern **Builder** e **Facade** per il checkout di UC-04.
2. Scomposizione dei controller in **applicativo + grafico**.
3. Centralizzazione della **validazione input** + **gestione errori user-friendly**.

---

## 1. Pattern Builder e Facade (UC-04)

- **Builder**: `RichiestaOrdine` (immutabile) + `RichiestaOrdineBuilder` (fluent, valida in `build()`).
- **Facade**: `OrdinaProdottoFacade.checkout()` orchestra buono(4a)→pagamento(6/6a)→submit.

---

## 2. Controller applicativo + grafico (UC-04 e UC-11)

```
View (JavaFX / CLI)
   │  → usano il controller GRAFICO (comporre input, gestire messaggi)
   ▼
Controller Grafico     (control/grafico/)
   │  → delegano la logica di business al controller APPLICATIVO
   ▼
Controller Applicativo (control/applicativo/) — SOLO logica: DAO, validazioni, sessioni
   │
   ▼
DAOFactory → DAO
```

| Classi | Ruolo |
|---|---|
| `OrdinaProdottoControllerApplicativo` / `AutenticazioneControllerApplicativo` | logica di business UC-04 / UC-11 |
| `OrdinaProdottoControllerGrafico` / `AutenticazioneControllerGrafico` | gestione schermate |
| `OrdinaProdottoFacade` | orchestratore checkout |
| 6 view aggiornate | Marketplace, Payment, Login (JavaFX) + 3 CLI |

Vetusti eliminati: `control/OrdinaProdottoController.java`, `control/AutenticazioneController.java`.

---

## 3. Validazione input e gestione errori

### Validator (`pattern/validator/`)
- `Validator<T>` (interfaccia funzionale)
- `CredenzialiValidator` — email valida + password non vuota
- `PagamentoValidator` — campi carta + CVV a 3 cifre
- `OrdineValidator` — prodotto selezionato

### Extractor (`pattern/extractor/`)
- `PagamentoExtractor` — costruisce e valida `PaymentInfoBean` dall'input grezzo.

### Eccezioni arricchite
- `BusinessValidationException` (e `AutenticazioneException`) espongono:
  `getUserMessage()` / `getTechnicalMessage()` / `getErrorCode()` / `getTimestamp()`.
- Le view mostrano `getUserMessage()` (testi chiari); il log conserva il dettaglio tecnico.

### Integrazione
- `AutenticazioneControllerApplicativo` → usa `CredenzialiValidator` (rimosso regex inline).
- `OrdinaProdottoControllerGrafico` → usa `PagamentoExtractor` (rimossa conversione inline).
- `OrdinaProdottoControllerApplicativo` → usa `OrdineValidator`.
- View JavaFX + CLI mostrano `getUserMessage()`.

---

## Verifiche complessive

| Misura | Inizio | Fine |
|---|---|---|
| Test in suite | 85 | **115** |
| `mvn clean verify` | SUCCESS | **SUCCESS** |
| Coverage LINE JaCoCo | ~81% | **82.19%** (soglia 80%) |

---

## 4. Allineamento stile Habibi (exceptions + enums)

Rispetto al criterio di riferimento ("le beans fanno le cose, i controller
sottili"), si è allineato il layer di errore:

- **Gerarchia eccezioni**: base astratta `CiboAmicoException` (userMessage /
technicalMessage / errorCode / timestamp) con sotto-classi
`BusinessValidationException`, `AutenticazioneException` e `DAOException`.
- **Enum messaggi**: `ExceptionMessagesEnum` (tecnici, per il log) e
`UserErrorMessagesEnum` (all'utente), centralizzati.
- **`PersistenceTypeEnum`**: enum della modalità di persistenza (DB/FS/DEMO).
- I bean validano nei setter (Fail Fast) e il controller applicativo usa
`bean.validate()`; i controller grafici restano sottili.

### Verifiche
- 115 test verdi, `mvn clean verify` SUCCESS, coverage 82.19%.

---

## Relazione
- VOPC aggiornato (due livelli di controllo).
- Nuove sottosezioni: **"Controller applicativo e grafico"**, **"Validazione input e gestione errori"**.
- Capitolo **Exceptions** riscritto con la gerarchia `CiboAmicoException` + enums.
- Pagine: 29. PDF su Drive.

---

**Nota:** nessun riferimento a progetti/codice esterni nel sorgente o nella relazione.
