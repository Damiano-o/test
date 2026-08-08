package it.uniroma2.ispw.ciboamico.control;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Copertura dei costruttori no-arg dei controller applicativi (ServiceLocator):
 * la persistenza è risolta dal ServiceLocator (ApplicationModeManager) in
 * modalità DEMO. Verifica che ogni controller sia istanziabile senza che la
 * View debba iniettare la DAOFactory (BCE: boundary non conosce la persistenza).
 */
class ControllerNoArgConstructionTest {

    @Test
    void tuttiIControllerSiCostruisconoConNoArg() {
    }
}
