package it.uniroma2.ispw.ciboamico.entity;

import it.uniroma2.ispw.ciboamico.exception.BusinessValidationException;
import it.uniroma2.ispw.ciboamico.exception.InvalidStateTransitionException;
import java.time.ZoneId;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Ordine singolo diretto (D-03): un compratore, un venditore, più voci.
 * Referenzia due Utente (compratore e venditore) — Venditore è un Ruolo,
 * non una classe autonoma (whole-part).
 * Implementa la macchina a stati BR-04 e il pattern Observer (OrdineSubject).
 */
public class Ordine {

    private final Long idOrdine;
    private final Utente compratore;
    private final Utente venditore;
    private final LocalDateTime data;
    private StatoOrdineEnum stato;
    private double totale;
    private final List<VoceOrdine> voci = new ArrayList<>();
    private final OrdineSubject subject = new OrdineSubject();

    public Ordine(Long idOrdine, Utente compratore, Utente venditore) {
        if (compratore == null || venditore == null) {
            throw new BusinessValidationException("Compratore e venditore sono obbligatori");
        }
        if (compratore.getEmail().equals(venditore.getEmail())) {
            throw new BusinessValidationException("Un utente non può acquistare il proprio prodotto (autoacquisto vietato)");
        }
        this.idOrdine = idOrdine;
        this.compratore = compratore;
        this.venditore = venditore;
        this.data = LocalDateTime.now(ZoneId.systemDefault());
        this.stato = StatoOrdineEnum.CREATED;
    }

    public void aggiungiVoce(VoceOrdine voce) {
        voci.add(voce);
        ricalcolaTotale();
    }

    public void ricalcolaTotale() {
        totale = voci.stream().mapToDouble(VoceOrdine::getParziale).sum();
    }

    /**
     * BR-04: transizioni valide.
     * CREATED → CONFIRMED | ANNULLED
     * CONFIRMED → IN_DELIVERY | ANNULLED
     * IN_DELIVERY → DELIVERED
     */
    public void cambiaStato(StatoOrdineEnum nuovoStato) {
        boolean valida = switch (stato) {
            case CREATED -> nuovoStato == StatoOrdineEnum.CONFIRMED
                    || nuovoStato == StatoOrdineEnum.ANNULLED;
            case CONFIRMED -> nuovoStato == StatoOrdineEnum.IN_DELIVERY
                    || nuovoStato == StatoOrdineEnum.ANNULLED;
            case IN_DELIVERY -> nuovoStato == StatoOrdineEnum.DELIVERED;
            default -> false;
        };
        if (!valida) {
            throw new InvalidStateTransitionException(
                    "Transizione non valida da " + stato + " a " + nuovoStato + " (BR-04)");
        }
        this.stato = nuovoStato;
        subject.notifyObservers(this);
    }

    public void subscribe(OrdineEventListener listener) { subject.subscribe(listener); }

    /**
     * Ripristina lo stato senza validazione — usato SOLO dai DAO per il caricamento
     * da persistenza (lo stato salvato è già stato validato al momento della transizione).
     */
    public void ripristinaStato(StatoOrdineEnum stato) {
        this.stato = stato;
    }

    public Long getIdOrdine() { return idOrdine; }
    public Utente getCompratore() { return compratore; }
    public Utente getVenditore() { return venditore; }
    public LocalDateTime getData() { return data; }
    public StatoOrdineEnum getStato() { return stato; }
    public double getTotale() { return totale; }
    public List<VoceOrdine> getVoci() { return voci; }
}
