package it.uniroma2.ispw.ciboamico.pattern.observer;

import java.time.LocalDateTime;

/**
 * Data Transfer Object immutabile che rappresenta l'evento di conferma di un
 * ordine. Viene creato quando un Cliente conferma un ordine e propagato
 * attraverso l'{@link OrdineEventPublisher} per notificare gli observer
 * interessati (compratore e venditore).
 *
 * <p>Caratteristiche del design:</p>
 * <ul>
 *   <li><b>Immutabilità</b>: tutti i campi sono final e non esistono setter,
 *       garantendo thread-safety e prevenendo modifiche accidentali.</li>
 *   <li><b>Isolamento dei layer</b>: la View non riceve mai l'entità
 *       {@code Ordine} (che resta nel dominio), ma solo questo DTO di sola
 *       lettura — così lo strato di presentazione è completamente disaccoppiato.</li>
 *   <li><b>Value Object</b>: oggetto immutabile senza identità propria.</li>
 * </ul>
 *
 * @author Michele Damiano
 */
public class OrdineEvent {

    /** Identificativo univoco dell'ordine confermato. */
    private final Long numeroOrdine;

    /** Identificativo del cliente che ha effettuato l'ordine. */
    private final String clienteId;

    /** Importo totale dell'ordine (inclusi eventuali sconti). */
    private final double totale;

    /** Timestamp della conferma dell'ordine. */
    private final LocalDateTime timestamp;

    /**
     * Costruisce un nuovo evento di ordine confermato.
     * Il timestamp viene generato automaticamente al momento della creazione.
     *
     * @param numeroOrdine identificativo univoco dell'ordine
     * @param clienteId    identificativo del cliente che ha effettuato l'ordine
     * @param totale       importo totale dell'ordine
     * @throws IllegalArgumentException se numeroOrdine è null oppure clienteId è nullo/vuoto
     */
    public OrdineEvent(Long numeroOrdine, String clienteId, double totale) {
        if (numeroOrdine == null) {
            throw new IllegalArgumentException("Il numero ordine non può essere null");
        }
        if (clienteId == null || clienteId.trim().isEmpty()) {
            throw new IllegalArgumentException("Il clienteId non può essere null o vuoto");
        }
        this.numeroOrdine = numeroOrdine;
        this.clienteId = clienteId;
        this.totale = totale;
        this.timestamp = LocalDateTime.now();
    }

    /** @return il numero dell'ordine. */
    public Long getNumeroOrdine() {
        return numeroOrdine;
    }

    /** @return l'ID del cliente. */
    public String getClienteId() {
        return clienteId;
    }

    /** @return il totale in euro. */
    public double getTotale() {
        return totale;
    }

    /** @return il momento esatto della conferma. */
    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return String.format("OrdineEvent{ordine=#%d, cliente='%s', totale=€%.2f, timestamp=%s}",
                numeroOrdine, clienteId, totale, timestamp);
    }
}
