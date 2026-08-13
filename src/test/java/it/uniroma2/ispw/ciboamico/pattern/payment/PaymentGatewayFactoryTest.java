package it.uniroma2.ispw.ciboamico.pattern.payment;

import it.uniroma2.ispw.ciboamico.exception.PaymentRejectedException;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Test della Simple Factory del PaymentGateway (passo 6 UC-04 /

class PaymentGatewayFactoryTest {

    @Test
    void createGatewayDefaultRestituisceStub() throws Exception {
        PaymentGateway gateway = PaymentGatewayFactory.createGateway();
        assertNotNull(gateway);
        assertTrue(gateway instanceof StubPaymentGateway);
        // sotto la soglia (500 EUR) l'autorizzazione riesce
        assertTrue(gateway.autorizza(499_00L));
    }

    @Test
    void createGatewayDefaultRifiutaOltreSoglia() {
        PaymentGateway gateway = PaymentGatewayFactory.createGateway();
        // estensione 6a: importo oltre la soglia simulata -> rifiutato
        assertThrows(PaymentRejectedException.class, () -> gateway.autorizza(501_00L));
    }

    @Test
    void createGatewayApprovazioneSempreAutorizza() throws Exception {
        // variante testabile: approvaSempre ignora la soglia (estensione
        PaymentGateway gateway = PaymentGatewayFactory.createGateway(true);
        assertTrue(gateway.autorizza(999_00L));
        assertTrue(gateway.autorizza(1_000_00L));
    }

    @Test
    void fornitoreStub() {
        PaymentGateway gateway = PaymentGatewayFactory.createGateway();
        assertTrue(gateway.fornitore().contains("Stub"));
    }
}
