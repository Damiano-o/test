package it.uniroma2.ispw.ciboamico.pattern.payment;

// Simple Factory del PaymentGateway (passo 6 UC-04, estensione 6a)

public final class PaymentGatewayFactory {

    private PaymentGatewayFactory() { }

    // Restituisce il gateway di pagamento attivo

    public static PaymentGateway createGateway() {
        return new StubPaymentGateway();
    }

    public static PaymentGateway createGateway(boolean approvaSempre) {
        return new StubPaymentGateway(approvaSempre);
    }
}