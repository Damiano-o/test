package it.uniroma2.ispw.ciboamico.pattern.observer;

import java.time.LocalDateTime;

// Data Transfer Object immutabile che rappresenta l'evento di conferma di un ordine

public class OrdineEvent {

    private final Long numeroOrdine;

    private final String clienteId;

    private final String venditoreId;

    private final double totale;

    private final LocalDateTime timestamp;

    // Costruisce un nuovo evento di ordine confermato

    public OrdineEvent(Long numeroOrdine, String clienteId, String venditoreId, double totale) {
        if (numeroOrdine == null) {
            throw new IllegalArgumentException("Il numero ordine non può essere null");
        }
        if (clienteId == null || clienteId.trim().isEmpty()) {
            throw new IllegalArgumentException("Il clienteId non può essere null o vuoto");
        }
        this.numeroOrdine = numeroOrdine;
        this.clienteId = clienteId;
        this.venditoreId = venditoreId;
        this.totale = totale;
        this.timestamp = LocalDateTime.now();
    }

    public Long getNumeroOrdine() {
        return numeroOrdine;
    }

    public String getClienteId() {
        return clienteId;
    }

    public String getVenditoreId() {
        return venditoreId;
    }

    public double getTotale() {
        return totale;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return String.format("OrdineEvent{ordine=#%d, cliente='%s', venditore='%s', totale=€%.2f, timestamp=%s}",
                numeroOrdine, clienteId, venditoreId, totale, timestamp);
    }
}
