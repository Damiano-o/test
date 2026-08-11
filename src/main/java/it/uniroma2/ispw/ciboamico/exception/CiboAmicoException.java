package it.uniroma2.ispw.ciboamico.exception;

import java.time.LocalDateTime;

/**
 * Eccezione base astratta dell'applicazione CiboAmico (pattern BCE).
 *
 * <p>Estende {@code Exception} (checked) per forzare la gestione esplicita
 * degli errori. Raccoglie la struttura comune a tutte le eccezioni di dominio:
 * il messaggio mostrato all'utente, il dettaglio tecnico per il log, un codice
 * d'errore simbolico e lo timestamp. In questo modo la UI mostra messaggi chiari
 * e il sistema di logging conserva il contesto tecnico.</p>
 */
public abstract class CiboAmicoException extends Exception {

    private final String userMessage;
    private final String technicalMessage;
    private final String errorCode;
    private final LocalDateTime timestamp;

    /** Costruttore: messaggio usato sia come user che come technical (fallback). */
    protected CiboAmicoException(String message) {
        super(message);
        this.userMessage = message;
        this.technicalMessage = message;
        this.errorCode = null;
        this.timestamp = LocalDateTime.now();
    }

    /** Costruttore con messaggio e causa. */
    protected CiboAmicoException(String message, Throwable cause) {
        super(message, cause);
        this.userMessage = message;
        this.technicalMessage = String.valueOf(cause);
        this.errorCode = null;
        this.timestamp = LocalDateTime.now();
    }

    /**
     * Costruttore completo: separa il messaggio user-friendly dal dettaglio
     * tecnico destinato ai log.
     *
     * @param userMessage      messaggio da mostrare all'utente
     * @param technicalMessage dettaglio per il log (null → userMessage)
     * @param errorCode        codice errore opzionale (null → non assegnato)
     */
    protected CiboAmicoException(String userMessage, String technicalMessage, String errorCode) {
        super(technicalMessage == null ? userMessage : technicalMessage);
        this.userMessage = userMessage;
        this.technicalMessage = technicalMessage == null ? userMessage : technicalMessage;
        this.errorCode = errorCode;
        this.timestamp = LocalDateTime.now();
    }

    /** Messaggio adatto a essere mostrato all'utente finale. */
    public String getUserMessage() {
        return userMessage;
    }

    /** Dettaglio tecnico per il logging (più ricco del messaggio utente). */
    public String getTechnicalMessage() {
        return technicalMessage;
    }

    /** Codice errore simbolico, se assegnato. */
    public String getErrorCode() {
        return errorCode;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}
