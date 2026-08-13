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
 * Implementa la sola macchina a stati BR-04: la notifica di conferma è
esternalizzata (il controller pubblica un {@code OrdineEvent} tramite
{@code OrdineEventPublisher}, cosicché l'entity resta svincolata dai layer
di presentazione).
 */
public class Ordine {

    private final Long idOrdine;
    private final Utente compratore;
    private final Utente venditore;
    private final LocalDateTime data;
    private StatoOrdineEnum stato;
    private double totale;
    private BuonoPromozionale buonoApplicato;
    private final List<VoceOrdine> voci = new ArrayList<>();

    public Ordine(Long idOrdine, Utente compratore, Utente venditore)
            throws BusinessValidationException {
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

    /**
     * Ricalcola il totale: somma delle voci, poi eventuale sconto del buono promozionale
     * (Ordine = Context del pattern Strategy: delega lo sconto alla strategia del buono).
     */
    public void ricalcolaTotale() {
        double subtotale = voci.stream().mapToDouble(VoceOrdine::getParziale).sum();
        totale = buonoApplicato != null ? buonoApplicato.applicaSconto(subtotale) : subtotale;
    }

    /**
     * Applica un buono promozionale all'ordine. Il buono deve appartenere al venditore
     * dell'ordine (regola CiboAmico: coupon del medesimo commerciante).
     * Al termine ricalcola il totale.
     *
     * @throws BusinessValidationException se il buono non appartiene al venditore dell'ordine
     */
    public void applicaBuono(BuonoPromozionale buono) throws BusinessValidationException {
        if (buono == null) {
            throw new BusinessValidationException("Il buono promozionale non può essere nullo");
        }
        BuonoPromozionale applicabile = buono.getVenditore() != null
                && buono.getVenditore().getUtente() != null
                && buono.getVenditore().getUtente().getEmail().equals(venditore.getEmail())
                ? buono
                : null;
        if (applicabile == null) {
            throw new BusinessValidationException(
                    "Il buono promozionale non è associato al venditore di questo ordine");
        }
        this.buonoApplicato = applicabile;
        ricalcolaTotale();
    }

    /**
     * BR-04: transizioni valide.
     * CREATED → CONFIRMED | ANNULLED
     * CONFIRMED → IN_DELIVERY | ANNULLED
     * IN_DELIVERY → DELIVERED
     */
    public void cambiaStato(StatoOrdineEnum nuovoStato)
            throws InvalidStateTransitionException {
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
    }

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
    public BuonoPromozionale getBuonoApplicato() { return buonoApplicato; }
    public List<VoceOrdine> getVoci() {
        // Defensive copy: i chiamanti non devono poter mutare le voci interne.
        return new ArrayList<>(voci);
    }
}
