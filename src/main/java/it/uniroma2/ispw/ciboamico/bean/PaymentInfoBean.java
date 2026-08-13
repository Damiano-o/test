package it.uniroma2.ispw.ciboamico.bean;

import it.uniroma2.ispw.ciboamico.exception.BusinessValidationException;

/**
 * Bean/DTO dei dati di pagamento (passo 6 + estensione 6a UC-04). Segue il
 * pattern BCE: il bean incapsula la validazione sintattica dei campi carta
 * (Fail Fast nei setter) e una validazione di coerenza d'insieme in
 * {@code validate()}. La boundary scambia SOLO questo bean col controller.
 *
 * <p>Per lo stub demo i dati carta sono passati per completezza della firma;
 * l'autorizzazione effettiva valuta solo l'importo. La conversione
 * formati esterno→interno dal controller di presentazione è incapsulata nel factory
 * statico {@link #fromCardData(String, String, String, String, double)}.</p>
 */
public class PaymentInfoBean {

    private String numeroCarta;
    private String intestatario;
    private String scadenza;    private String cvv;
    /** Importo della transazione in centesimi (long) per evitare float su denaro. */
    private long importoInCent;

    /**
     * Factory method di conversione esterno→interno: costruisce il bean dai
     * dati carta grezzi della view e dall'importo dell'ordine. Unico punto per
     * graphic controller e boundary CLI (DRY): i setter validano (Fail Fast).
     *
     * @param numeroCarta  numero carta (input grezzo)
     * @param intestatario intestatario della carta
     * @param scadenza     scadenza della carta
     * @param cvv          codice di sicurezza
     * @param totale       importo lordo dell'ordine in euro (convertito in cent)
     * @return bean di pagamento pronto per l'autorizzazione
     */
    public static PaymentInfoBean fromCardData(String numeroCarta, String intestatario,
                                               String scadenza, String cvv, double totale)
            throws BusinessValidationException {
        PaymentInfoBean payment = new PaymentInfoBean();
        payment.setNumeroCarta(numeroCarta);
        payment.setIntestatario(intestatario);
        payment.setScadenza(scadenza);
        payment.setCvv(cvv);
        payment.setImportoInCent(Math.round(totale * 100));
        return payment;
    }

    public String getNumeroCarta() { return numeroCarta; }

    /**
     * Imposta il numero carta (non vuoto).
     *
     * @throws BusinessValidationException se la carta è vuota
     */
    public void setNumeroCarta(String numeroCarta) throws BusinessValidationException {
        this.numeroCarta = validaNumeroCarta(numeroCarta);
    }

    /** Controllo sintattico del numero carta; Fail Fast. */
    private static String validaNumeroCarta(String numeroCarta) throws BusinessValidationException {
        if (numeroCarta == null || numeroCarta.isBlank()) {
            throw new BusinessValidationException(
                    "Compila il numero carta.",
                    "Numero carta vuoto nel pagamento.",
                    "ERR-CAMPI-PAGAMENTO");
        }
        return numeroCarta.trim();
    }

    public String getIntestatario() { return intestatario; }

    /**
     * Imposta l'intestatario (non vuoto).
     *
     * @throws BusinessValidationException se l'intestatario è vuoto
     */
    public void setIntestatario(String intestatario) throws BusinessValidationException {
        this.intestatario = validaIntestatario(intestatario);
    }

    /** Controllo sintattico dell'intestatario; Fail Fast. */
    private static String validaIntestatario(String intestatario) throws BusinessValidationException {
        if (intestatario == null || intestatario.isBlank()) {
            throw new BusinessValidationException(
                    "Compila l'intestatario della carta.",
                    "Intestatario vuoto nel pagamento.",
                    "ERR-CAMPI-PAGAMENTO");
        }
        return intestatario.trim();
    }

    public String getScadenza() { return scadenza; }

    /**
     * Imposta la scadenza (non vuota).
     *
     * @throws BusinessValidationException se la scadenza è vuota
     */
    public void setScadenza(String scadenza) throws BusinessValidationException {
        this.scadenza = validaScadenza(scadenza);
    }

    /** Controllo sintattico della scadenza; Fail Fast. */
    private static String validaScadenza(String scadenza) throws BusinessValidationException {
        if (scadenza == null || scadenza.isBlank()) {
            throw new BusinessValidationException(
                    "Compila la scadenza della carta.",
                    "Scadenza vuota nel pagamento.",
                    "ERR-CAMPI-PAGAMENTO");
        }
        return scadenza.trim();
    }

    public String getCvv() { return cvv; }

    /**
     * Imposta il CVV (esattamente 3 cifre).
     *
     * @throws BusinessValidationException se il CVV non è valido
     */
    public void setCvv(String cvv) throws BusinessValidationException {
        this.cvv = validaCvv(cvv);
    }

    /** Controllo sintattico del CVV (esattamente 3 cifre) — metodo privato. */
    private static String validaCvv(String cvv) throws BusinessValidationException {
        if (cvv == null || cvv.trim().length() != 3) {
            throw new BusinessValidationException(
                    "Il CVV deve essere di esattamente 3 cifre.",
                    "CVV non valido: " + (cvv == null ? "null" : cvv.length()) + " caratteri",
                    "ERR-CVV");
        }
        return cvv.trim();
    }

    public long getImportoInCent() { return importoInCent; }

    /** Imposta l'importo da addebitare in centesimi (deve essere positivo). */
    public void setImportoInCent(long importoInCent) throws BusinessValidationException {
        this.importoInCent = validaImporto(importoInCent);
    }

    /** Controllo sintattico dell'importo (positivo). */
    private static long validaImporto(long importoInCent) throws BusinessValidationException {
        if (importoInCent <= 0) {
            throw new BusinessValidationException(
                    "Importo da addebitare non valido.",
                    "Importo " + importoInCent + " non positivo.",
                    "ERR-IMPORTO");
        }
        return importoInCent;
    }

    /**
     * Valida la coerenza complessiva del bean. I singoli campi sono già
     * validati nei setter (Fail Fast); questo metodo conferma che il bean sia
     * pronto per l'autorizzazione.
     *
     * @throws BusinessValidationException se qualche campo obbligatorio manca
     */
    public void validate() throws BusinessValidationException {
        validaCampiObbligatori();
    }

    /** Coerenza d'insieme dei dati carta: tutti i campi obbligatori presenti. */
    private void validaCampiObbligatori() throws BusinessValidationException {
        if (numeroCarta == null || intestatario == null
                || scadenza == null || cvv == null) {
            throw new BusinessValidationException(
                    "Tutti i dati del pagamento sono obbligatori.",
                    "Campi carta mancanti nel pagamento.",
                    "ERR-CAMPI-PAGAMENTO");
        }
    }
}
