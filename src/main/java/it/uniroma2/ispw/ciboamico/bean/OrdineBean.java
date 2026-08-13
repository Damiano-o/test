package it.uniroma2.ispw.ciboamico.bean;

import it.uniroma2.ispw.ciboamico.exception.BusinessValidationException;

// Bean/DTO per l'ordine, scambiato tra la boundary e il controller applicativo (UC-04)

public class OrdineBean {

    private Long idOrdine;
    private String nomeProdotto;
    private Double totale;
    private String stato;
    private String compratoreId;
    private String venditoreId;

    private String codiceBuono;

    public Long getIdOrdine() { return idOrdine; }
    public void setIdOrdine(Long idOrdine) { this.idOrdine = idOrdine; }

    // Factory method di conversione esterno→interno: partendo dal nome prodotto selezionato n...

    public static OrdineBean fromCheckout(String nomeProdotto) throws BusinessValidationException {
        OrdineBean bean = new OrdineBean();
        bean.setNomeProdotto(nomeProdotto);
        return bean;
    }

    public String getNomeProdotto() { return nomeProdotto; }

    // Imposta il prodotto selezionato (obbligatorio, non vuoto)

    public void setNomeProdotto(String nomeProdotto) throws BusinessValidationException {
        this.nomeProdotto = validaNomeProdotto(nomeProdotto);
    }

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

    public void setCodiceBuono(String codiceBuono) { this.codiceBuono = codiceBuono; }

    // Valida che l'ordine abbia i dati minimi per procedere

    public void validate() throws BusinessValidationException {
        validaNomeProdotto(nomeProdotto);
    }
}
