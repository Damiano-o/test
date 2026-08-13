package it.uniroma2.ispw.ciboamico.exception;

import java.time.LocalDateTime;

// Eccezione base astratta dell'applicazione CiboAmico (pattern BCE)

public abstract class CiboAmicoException extends Exception {

    private final String userMessage;
    private final String technicalMessage;
    private final String errorCode;
    private final LocalDateTime timestamp;

    protected CiboAmicoException(String message) {
        super(message);
        this.userMessage = message;
        this.technicalMessage = message;
        this.errorCode = null;
        this.timestamp = LocalDateTime.now();
    }

    protected CiboAmicoException(String message, Throwable cause) {
        super(message, cause);
        this.userMessage = message;
        this.technicalMessage = String.valueOf(cause);
        this.errorCode = null;
        this.timestamp = LocalDateTime.now();
    }

    // Costruttore completo: separa il messaggio user-friendly dal dettaglio tecnico destinato...

    protected CiboAmicoException(String userMessage, String technicalMessage, String errorCode) {
        super(technicalMessage == null ? userMessage : technicalMessage);
        this.userMessage = userMessage;
        this.technicalMessage = technicalMessage == null ? userMessage : technicalMessage;
        this.errorCode = errorCode;
        this.timestamp = LocalDateTime.now();
    }

    public String getUserMessage() {
        return userMessage;
    }

    public String getTechnicalMessage() {
        return technicalMessage;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}
