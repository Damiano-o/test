package it.uniroma2.ispw.ciboamico.pattern.payment;

import it.uniroma2.ispw.ciboamico.exception.PaymentRejectedException;

import java.util.logging.Logger;

/**
 * Stub in-memory del {@link PaymentGateway} (nessun PSP reale).
 * Simula in modo sincrono l'autorizzazione: esito positivo predefinito,
 * negativo se l'importo supera una soglia simulata. Coerente con gli stub
 * della persistenza in-memory (DemoDAOFactory): nessuna rete, deterministico
 * e testabile. L'estensione 6a è modellata lanciando {@link PaymentRejectedException}.
 */
public final class StubPaymentGateway implements PaymentGateway {

    private static final Logger LOG = Logger.getLogger(StubPaymentGateway.class.getName());

    /** Soglia simulata oltre la quale il pagamento è rifiutato. */
    private static final long SOGLIA_RIFIUTO = 500_00L; // 500,00 EUR in centesimi

    private final boolean approvaSempre;

    public StubPaymentGateway() {
        this(false);
    }

    /** Costruttore per test deterministici. */
    public StubPaymentGateway(boolean approvaSempre) {
        this.approvaSempre = approvaSempre;
    }

    @Override
    public boolean autorizza(long importoInCent) throws PaymentRejectedException {
        if (!approvaSempre && importoInCent > SOGLIA_RIFIUTO) {
            LOG.warning("[PAYMENT] Addebito rifiutato per importo " + importoInCent + " cent");
            throw new PaymentRejectedException("Pagamento negato: importo oltre il limite consentito (6a).");
        }
        LOG.info("[PAYMENT] Addebito autorizzato per " + importoInCent + " cent");
        return true;
    }

    @Override
    public String fornitore() {
        return "StubPaymentGateway (in-memory)";
    }
}