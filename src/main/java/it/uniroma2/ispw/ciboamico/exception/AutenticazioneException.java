package it.uniroma2.ispw.ciboamico.exception;

// Eccezione per errori di autenticazione

public class AutenticazioneException extends BusinessValidationException {

    public AutenticazioneException(String message) {
        super(message);
    }

    public AutenticazioneException(String userMessage, String technicalMessage, String errorCode) {
        super(userMessage, technicalMessage, errorCode);
    }
}