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
    PAYMENT_REJECTED_MSG("Pagamento non autorizzato.");

    public final String message;

    UserErrorMessagesEnum(String message) {
        this.message = message;
    }
}
