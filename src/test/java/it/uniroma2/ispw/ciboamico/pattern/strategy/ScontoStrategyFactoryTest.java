package it.uniroma2.ispw.ciboamico.pattern.strategy;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

// Copertura completa di ScontoStrategyFactory (Simple Factory dei

class ScontoStrategyFactoryTest {

    @Test
    void creaStrategyPercentuale() {
        ScontoStrategy s = ScontoStrategyFactory.createStrategy(
                ScontoStrategyFactory.TIPO_PERCENTUALE, 0.1);
        assertInstanceOf(ScontoPercentualeStrategy.class, s);
        assertEquals(90.0, s.applicaSconto(100), 1e-9);
    }

    @Test
    void creaStrategyImportoFisso() {
        ScontoStrategy s = ScontoStrategyFactory.createStrategy(
                ScontoStrategyFactory.TIPO_FISSO, 5.0);
        assertInstanceOf(ScontoImportoFissoStrategy.class, s);
        assertEquals(95.0, s.applicaSconto(100), 1e-9);
    }

    @Test
    void tipoSconosciutoLanciaEccezione() {
        assertThrows(IllegalArgumentException.class,
                () -> ScontoStrategyFactory.createStrategy("SCONTO_IGNOTO", 1.0));
    }
}
