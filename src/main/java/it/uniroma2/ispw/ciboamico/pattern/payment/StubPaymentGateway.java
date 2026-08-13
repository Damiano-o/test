package it.uniroma2.ispw.ciboamico.pattern.payment;

import it.uniroma2.ispw.ciboamico.exception.PaymentRejectedException;

import java.util.logging.Logger;

// Stub in-memory del PaymentGateway (nessun PSP reale)

public final class StubPaymentGateway implements PaymentGateway {

    private static final Logger LOG = Logger.getLogger(StubPaymentGateway.class.getName());

    private static final long SOGLIA_RIFIUTO = 500_00L; // 500,00 EUR in centesimi

    private final boolean approvaSempre;

    public StubPaymentGateway() {
        this(false);
    }

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