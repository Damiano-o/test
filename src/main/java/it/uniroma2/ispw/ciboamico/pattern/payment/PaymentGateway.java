package it.uniroma2.ispw.ciboamico.pattern.payment;

import it.uniroma2.ispw.ciboamico.exception.PaymentRejectedException;

// Gateway di pagamento esterno (passo 6 UC-04 / estensione 6a)

public interface PaymentGateway {

    // Richiede l'autorizzazione all'addebito per l'importo della transazione

    boolean autorizza(long importoInCent) throws PaymentRejectedException;

    String fornitore();
}