package it.uniroma2.ispw.ciboamico.exception;

// Eccezione per gli errori durante l'accesso alla persistenza

public class DAOException extends CiboAmicoException {

    public DAOException(String message) {
        super(message);
    }

    public DAOException(String message, Throwable cause) {
        super(message, cause);
    }

    public DAOException(String userMessage, String technicalMessage, String errorCode) {
        super(userMessage, technicalMessage, errorCode);
    }
}
