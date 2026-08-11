package it.uniroma2.ispw.ciboamico.bean;

import it.uniroma2.ispw.ciboamico.exception.BusinessValidationException;

/**
 * Bean/DTO dei dati di pagamento (passo 6 + estensione 6a UC-04). Segue il
 * pattern BCE: il bean incapsula la validazione sintattica dei campi carta
 * (Fail Fast nei setter) e una validazione di coerenza d'insieme in
 * {@code validate()}. La boundary-scambia SOLO questo bean col controller.
 *
 * <p>Per lo stub demo i dati carta sono passati per completezza della firma;
 * l'autorizzazione effettiva valuta solo l'importo.</p>
 */
public class PaymentInfoBean {

    private String numeroCarta;
    private String intestatario;
    private String scadenza;
    private String cvv;
    /** Importo della transazione in centesimi (long) per evitare float su denaro. */
    private long importoInCent;

    public String getNumeroCarta() { return numeroCarta; }

    /**
     * Imposta il numero carta (non vuoto).
     *
     * @throws BusinessValidationException se la carta è vuota
     */
    public void setNumeroCarta(String numeroCarta) throws BusinessValidationException {
        if (numeroCarta == null || numeroCarta.isBlank()) {
            throw new BusinessValidationException(
                    "Compila il numero carta.",
                    "Numero carta vuoto nel pagamento.",
                    "ERR-CAMPI-PAGAMENTO");
        }
        this.numeroCarta = numeroCarta.trim();
    }

    public String getIntestatario() { return intestatario; }

    /**
     * Imposta l'intestatario (non vuoto).
     *
     * @throws BusinessValidationException se l'intestatario è vuoto
     */
    public void setIntestatario(String intestatario) throws BusinessValidationException {
        if (intestatario == null || intestatario.isBlank()) {
            throw new BusinessValidationException(
                    "Compila l'intestatario della carta.",
                    "Intestatario vuoto nel pagamento.",
                    "ERR-CAMPI-PAGAMENTO");
        }
        this.intestatario = intestatario.trim();
    }

    public String getScadenza() { return scadenza; }

    /**
     * Imposta la scadenza (non vuota).
     *
     * @throws BusinessValidationException se la scadenza è vuota
     */
    public void setScadenza(String scadenza) throws BusinessValidationException {
        if (scadenza == null || scadenza.isBlank()) {
            throw new BusinessValidationException(
                    "Compila la scadenza della carta.",
                    "Scadenza vuota nel pagamento.",
                    "ERR-CAMPI-PAGAMENTO");
        }
        this.scadenza = scadenza.trim();
    }

    public String getCvv() { return cvv; }

    /**
     * Imposta il CVV (esattamente 3 cifre).
     *
     * @throws BusinessValidationException se il CVV non è valido
     */
    public void setCvv(String cvv) throws BusinessValidationException {
        if (cvv == null || cvv.trim().length() != 3) {
            throw new BusinessValidationException(
                    "Il CVV deve essere di esattamente 3 cifre.",
                    "CVV non valido: " + (cvv == null ? "null" : cvv.length()) + " caratteri",
                    "ERR-CVV");
        }
        this.cvv = cvv.trim();
    }

    public long getImportoInCent() { return importoInCent; }

    /** Imposta l'importo da addebitare in centesimi (deve essere positivo). */
    public void setImportoInCent(long importoInCent) throws BusinessValidationException {
        if (importoInCent <= 0) {
            throw new BusinessValidationException(
                    "Importo da addebitare non valido.",
                    "Importo " + importoInCent + " non positivo.",
                    "ERR-IMPORTO");
        }
        this.importoInCent = importoInCent;
    }

    /**
     * Valida la coerenza complessiva del bean. I singoli campi sono già
     * validati nei setter (Fail Fast); questo metodo conferma che il bean sia
     * pronto per l'autorizzazione.
     *
     * @throws BusinessValidationException se qualche campo obbligatorio manca
     */
    public void validate() throws BusinessValidationException {
        if (numeroCarta == null || intestatario == null
                || scadenza == null || cvv == null) {
            throw new BusinessValidationException(
                    "Tutti i dati del pagamento sono obbligatori.",
                    "Campi carta mancanti nel pagamento.",
                    "ERR-CAMPI-PAGAMENTO");
        }
    }
}
