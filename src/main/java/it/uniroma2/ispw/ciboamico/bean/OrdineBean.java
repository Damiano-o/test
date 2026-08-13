package it.uniroma2.ispw.ciboamico.bean;

import it.uniroma2.ispw.ciboamico.exception.BusinessValidationException;

/**
 * Bean/DTO per l'ordine, scambiato tra la boundary e il controller
 * applicativo (UC-04). Segue il pattern BCE: incapsula la validazione
 * sintattica nei setter (Fail Fast) e una validazione di coerenza in
 * {@code validate()}. La conversione esterno→interno (dalla boundary al
 * controller applicativo state-less) è incapsulata nel factory statico
 * {@link #fromCheckout(String)}.
 */
public class OrdineBean {

    private Long idOrdine;
    private String nomeProdotto;
    private Double totale;
    private String stato;
    private String compratoreId;
    private String venditoreId;
    /** Codice del buono promozionale applicato (opzionale, estensione 4a UC-04). */
    private String codiceBuono;

    public Long getIdOrdine() { return idOrdine; }
    public void setIdOrdine(Long idOrdine) { this.idOrdine = idOrdine; }

    /**
     * Factory method di conversione esterno→interno: partendo dal nome
     * prodotto selezionato nella boundary (formato esterno) costruisce l'ordine
     * in checkout (formato interno). La costruzione del bean è responsabilità
     * del controller di presentazione, non del controller applicativo. Vedi anche
     * {@link #setNomeProdotto(String)}.
     *
     * @param nomeProdotto prodotto selezionato dall'utente
     * @return bean ordine in checkout valorizzato con il prodotto
     * @throws BusinessValidationException se il prodotto non è selezionato
     */
    public static OrdineBean fromCheckout(String nomeProdotto) throws BusinessValidationException {
        OrdineBean bean = new OrdineBean();
        bean.setNomeProdotto(nomeProdotto);
        return bean;
    }

    /** Prodotto selezionato dalla boundary (chiave di lookup per UC-04). */
    public String getNomeProdotto() { return nomeProdotto; }

    /**
     * Imposta il prodotto selezionato (obbligatorio, non vuoto).
     *
     * @throws BusinessValidationException se il prodotto non è selezionato
     */
    public void setNomeProdotto(String nomeProdotto) throws BusinessValidationException {
        this.nomeProdotto = validaNomeProdotto(nomeProdotto);
    }

    /** Controllo sintattico del prodotto selezionato (Fail Fast) — metodo privato. */
    private String validaNomeProdotto(String nomeProdotto) throws BusinessValidationException {
        if (nomeProdotto == null || nomeProdotto.isBlank()) {
            throw new BusinessValidationException(
                    "Seleziona un prodotto dal catalogo prima di procedere.",
                    "OrdineBean senza prodotto selezionato.",
                    "ERR-PRODOTTO-MANCANTE");
        }
        return nomeProdotto.trim();
    }

    public Double getTotale() { return totale; }
    public void setTotale(Double totale) { this.totale = totale; }
    public String getStato() { return stato; }
    public void setStato(String stato) { this.stato = stato; }
    public String getCompratoreId() { return compratoreId; }
    public void setCompratoreId(String compratoreId) { this.compratoreId = compratoreId; }
    public String getVenditoreId() { return venditoreId; }
    public void setVenditoreId(String venditoreId) { this.venditoreId = venditoreId; }
    public String getCodiceBuono() { return codiceBuono; }

    /** Imposta il codice del buono promozionale (opzionale). */
    public void setCodiceBuono(String codiceBuono) { this.codiceBuono = codiceBuono; }

    /**
     * Valida che l'ordine abbia i dati minimi per procedere.
     *
     * @throws BusinessValidationException se il prodotto non è selezionato
     */
    public void validate() throws BusinessValidationException {
        validaNomeProdotto(nomeProdotto);
    }
}
