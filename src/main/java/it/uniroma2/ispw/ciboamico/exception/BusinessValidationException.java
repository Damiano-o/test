package it.uniroma2.ispw.ciboamico.exception;

// Eccezione di dominio per le violazioni delle regole di business

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
