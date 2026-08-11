package it.uniroma2.ispw.ciboamico.exception;

/**
 * Eccezione di dominio per le violazioni delle regole di business e delle
 * validazioni semantiche. Eredita la struttura (user/technical messaggio,
 * codice errore, timestamp) dalla base {@link CiboAmicoException}.
 *
 * <p>Estende {@code Exception} (checked): la maggior parte degli errori deriva
 * da input utente malformato o da flussi non conformi, gestiti nei controller
 * grafici. Sostituisce l'uso generico di {@code IllegalArgumentException} per i
 * vincoli di dominio, dando un'astrazione espressiva nel layer di errore.</p>
 */
public class BusinessValidationException extends CiboAmicoException {

    public BusinessValidationException(String message) {
        super(message);
    }

    public BusinessValidationException(String message, Throwable cause) {
        super(message, cause);
    }

    public BusinessValidationException(String userMessage, String technicalMessage, String errorCode) {
        super(userMessage, technicalMessage, errorCode);
    }
}
