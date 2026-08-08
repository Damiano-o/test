package it.uniroma2.ispw.ciboamico.exception;

/**
 * Eccezione di dominio per le violazioni delle regole di business e delle
 * validazioni semantiche. Runtime perché la maggior parte
 * degli errori deriva da input utente malformato o da flussi non conformi,
 * gestiti nei controller grafici. Sostituisce l'uso generico di
 * IllegalArgumentException per i vincoli di dominio, dando un'astrazione
 * espressiva nel layer di errore.
 */
public class BusinessValidationException extends Exception {

    public BusinessValidationException(String message) {
        super(message);
    }

    public BusinessValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}