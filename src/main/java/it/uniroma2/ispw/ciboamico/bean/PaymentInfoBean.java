package it.uniroma2.ispw.ciboamico.bean;

import it.uniroma2.ispw.ciboamico.exception.BusinessValidationException;

// Bean/DTO dei dati di pagamento (passo 6 + estensione 6a UC-04)

public class PaymentInfoBean {

    private String numeroCarta;
    private String intestatario;
    private String scadenza;    private String cvv;

    private long importoInCent;

    // Factory method di conversione esterno→interno: costruisce il bean dai dati carta grezzi...

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

    // Imposta il numero carta (non vuoto)

    public void setNumeroCarta(String numeroCarta) throws BusinessValidationException {
        this.numeroCarta = validaNumeroCarta(numeroCarta);
    }

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

    // Imposta l'intestatario (non vuoto)

    public void setIntestatario(String intestatario) throws BusinessValidationException {
        this.intestatario = validaIntestatario(intestatario);
    }

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

    // Imposta la scadenza (non vuota)

    public void setScadenza(String scadenza) throws BusinessValidationException {
        this.scadenza = validaScadenza(scadenza);
    }

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

    // Imposta il CVV (esattamente 3 cifre)

    public void setCvv(String cvv) throws BusinessValidationException {
        this.cvv = validaCvv(cvv);
    }

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

    public void setImportoInCent(long importoInCent) throws BusinessValidationException {
        this.importoInCent = validaImporto(importoInCent);
    }

    private static long validaImporto(long importoInCent) throws BusinessValidationException {
        if (importoInCent <= 0) {
            throw new BusinessValidationException(
                    "Importo da addebitare non valido.",
                    "Importo " + importoInCent + " non positivo.",
                    "ERR-IMPORTO");
        }
        return importoInCent;
    }

    // Valida la coerenza complessiva del bean

    public void validate() throws BusinessValidationException {
        validaCampiObbligatori();
    }

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
