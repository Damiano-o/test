package it.uniroma2.ispw.ciboamico.exception;

// Sottoclasse di BusinessValidationException per gli errori di autenticazione (email non...

public class AutenticazioneException extends BusinessValidationException {

    public AutenticazioneException(String message) {
        super(message);
    }

    public AutenticazioneException(String userMessage, String technicalMessage, String errorCode) {
        super(userMessage, technicalMessage, errorCode);
    }
}