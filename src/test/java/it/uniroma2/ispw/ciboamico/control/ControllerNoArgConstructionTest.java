package it.uniroma2.ispw.ciboamico.control;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

// Copertura dei costruttori no-arg dei controller: la persistenza è risolta dal ServiceLo...

class ControllerNoArgConstructionTest {

    @Test
    void tuttiIControllerSiCostruisconoConNoArg() {
        assertNotNull(new OrdinaProdottoController());
        assertNotNull(new AutenticazioneController());
        assertNotNull(new ApplicaBuonoPromozionaleController());
        assertNotNull(new PagamentoController());
    }
}
