package it.uniroma2.ispw.ciboamico.exception;

/**
 * Sottoclasse di BusinessValidationException per gli errori di autenticazione
 * (email non valida o credenziali errate, UC-11). Separa la gestione degli
 * errori di accesso dalle altre violazioni di business, facilitando la
 * presentazione di messaggi dedicati nella LoginView.
 */
public class AutenticazioneException extends BusinessValidationException {

    public AutenticazioneException(String message) {
        super(message);
    }

    /** Costruttore che separa messaggio utente e dettaglio tecnico. */
    public AutenticazioneException(String userMessage, String technicalMessage, String errorCode) {
        super(userMessage, technicalMessage, errorCode);
    }
}