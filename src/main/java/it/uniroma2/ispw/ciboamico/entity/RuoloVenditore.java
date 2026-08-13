package it.uniroma2.ispw.ciboamico.entity;

/**
 * Ruolo Venditore (contadino/fornitore locale).
 * Solo con stato APPROVATO può pubblicare prodotti (BR-02).
 *
 * <p>Relazioni unidirezionali: la navigazione Utente→Ruolo è quella di
 * dominio. Il back-reference {@code utente} è <b>transient</b> così
 * la persistenza FS/JSON (Gson) non serializza il ciclo Utente↔RuoloVenditore
 * (evita lo StackOverflow): dopo il caricamento il back-link va ristabilito
 * quando necessario (es. ricostruzione dal DAO del venditore o aggiungiRuolo).</p>
 */
public class RuoloVenditore extends Ruolo {

    private final String zona;
    private final String recapito;
    private StatoVenditoreEnum stato;
    private transient Utente utente;

    public RuoloVenditore(String zona, String recapito) {
        this.zona = zona;
        this.recapito = recapito;
        this.stato = StatoVenditoreEnum.IN_ATTESA;
    }

    public String getZona() { return zona; }
    public String getRecapito() { return recapito; }
    public StatoVenditoreEnum getStato() { return stato; }

    @Override
    public String getNomeRuolo() { return "VENDITORE"; }

    /** Utente proprietario di questo ruolo (set quando il ruolo è aggiunto all'Utente). */
    public Utente getUtente() { return utente; }
    void setUtente(Utente utente) { this.utente = utente; }

    /**
     * Approvazione del venditore (BR-02): transizione IN_ATTESA → APPROVATO.
     * Solo un venditore approvato può pubblicare prodotti (regola di dominio
     * espressa sull'Information Expert). Niente effetto se già approvato
     * (monotona).
     */
    public void approva() {
        if (stato == StatoVenditoreEnum.IN_ATTESA) {
            this.stato = StatoVenditoreEnum.APPROVATO;
        }
    }

    /**
     * Imposta lo stato senza validazione di transizione. Riservato alla
     * <em>inizializzazione</em> (seed/demo) e al <em>ripristino da
     * persistenza</em> (DAO/Gson), dove lo stato salvato è già valido:
     * stessa semantica di {@link Ordine#ripristinaStato}. Il passaggio a
     * APPROVATO in ambito di dominio va fatto con {@link #approva()}.
     */
    public void setStato(StatoVenditoreEnum stato) { this.stato = stato; }
}
