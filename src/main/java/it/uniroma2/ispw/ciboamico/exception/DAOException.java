package it.uniroma2.ispw.ciboamico.exception;

/**
 * Eccezione per gli errori durante l'accesso alla persistenza (DAO).
 * Eredita la struttura (user/technical messaggio, codice errore, timestamp)
 * dalla base {@link CiboAmicoException}.
 */
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
