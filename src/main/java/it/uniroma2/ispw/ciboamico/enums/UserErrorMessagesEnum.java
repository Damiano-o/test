package it.uniroma2.ispw.ciboamico.enums;

/**
 * Messaggi mostrati all'utente finale (titoli ed errori) dalla UI. Fonte
 * testuale centralizzata per mantenere chiari e coerenti i testi sul confine
 * applicativo.
 */
public enum UserErrorMessagesEnum {

    /* === TITLES === */
    LOGIN_ERROR_TITLE("Errore di accesso"),
    DATA_RETRIEVAL_TITLE("Errore di recupero dati"),
    AUTHORIZATION_TITLE("Errore di autorizzazione"),
    VALIDATION_TITLE("Errore di validazione"),
    PAYMENT_TITLE("Errore di pagamento"),

    /* === MESSAGES === */
    WRONG_PASSWORD_MSG("Email o password non validi."),
    USER_NOT_FOUND_MSG("Nessun utente corrisponde alle credenziali."),
    MALFORMED_EMAIL_MSG("Inserisci un indirizzo email con il formato corretto."),
    MISSING_AUTHORIZATION_MSG("Nessun ruolo associato all'utente."),
    PAYMENT_REJECTED_MSG("Pagamento non autorizzato."),

    /* === UC-04: buono promozionale === */
    BUONO_CODICE_OBBLIGATORIO_MSG("Inserisci un codice buono."),
    BUONO_NON_VALIDO_MSG("Buono promozionale non valido."),
    BUONO_NON_ATTIVO_MSG("Buono promozionale scaduto o non ancora attivo."),
    BUONO_GIÀ_USATO_MSG("Buono già utilizzato da questo utente."),
    BUONO_VENDITORE_ERRATO_MSG("Il buono promozionale non appartiene al venditore di questo prodotto"),

    /* === UC-04: ordine === */
    PRODOTTO_NON_SELEZIONATO_MSG("Nessun prodotto selezionato."),
    PRODOTTO_NON_DISPONIBILE_MSG("Prodotto non più disponibile."),

    /* === UC-04: pagamento === */
    ORDINE_MANCANTE_MSG("Nessun ordine in checkout."),
    DATI_PAGAMENTO_NON_VALIDI_MSG("Dati di pagamento non validi");

    public final String message;

    UserErrorMessagesEnum(String message) {
        this.message = message;
    }
}
