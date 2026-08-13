package it.uniroma2.ispw.ciboamico.exception;

import it.uniroma2.ispw.ciboamico.pattern.payment.PaymentGateway;

// Autorizzazione di pagamento negata (estensione 6a del caso d'uso

public class PaymentRejectedException extends Exception {

    public PaymentRejectedException(String message) {
        super(message);
    }
}