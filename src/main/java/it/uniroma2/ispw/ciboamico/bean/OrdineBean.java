package it.uniroma2.ispw.ciboamico.bean;

import it.uniroma2.ispw.ciboamico.exception.BusinessValidationException;

/**
 * Bean/DTO per l'ordine, scambiato tra la boundary e il controller
 * applicativo (UC-04). Segue il pattern BCE: incapsula la validazione
 * sintattica nei setter (Fail Fast) e una validazione di coerenza in
 * {@code validate()}.
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

    /** Prodotto selezionato dalla boundary (chiave di lookup per UC-04). */
    public String getNomeProdotto() { return nomeProdotto; }

    /**
     * Imposta il prodotto selezionato (obbligatorio, non vuoto).
     *
     * @throws BusinessValidationException se il prodotto non è selezionato
     */
    public void setNomeProdotto(String nomeProdotto) throws BusinessValidationException {
        if (nomeProdotto == null || nomeProdotto.isBlank()) {
            throw new BusinessValidationException(
                    "Seleziona un prodotto dal catalogo prima di procedere.",
                    "OrdineBean senza prodotto selezionato.",
                    "ERR-PRODOTTO-MANCANTE");
        }
        this.nomeProdotto = nomeProdotto.trim();
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
        if (nomeProdotto == null || nomeProdotto.isBlank()) {
            throw new BusinessValidationException(
                    "Seleziona un prodotto dal catalogo prima di procedere.",
                    "OrdineBean senza prodotto selezionato.",
                    "ERR-PRODOTTO-MANCANTE");
        }
    }
}
