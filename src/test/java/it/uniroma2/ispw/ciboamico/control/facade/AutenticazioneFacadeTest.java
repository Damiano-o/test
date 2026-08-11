package it.uniroma2.ispw.ciboamico.control.facade;

import it.uniroma2.ispw.ciboamico.bean.UtenteBean;
import it.uniroma2.ispw.ciboamico.control.AutenticazioneController;
import it.uniroma2.ispw.ciboamico.exception.AutenticazioneException;
import it.uniroma2.ispw.ciboamico.persistence.factory.DemoDAOFactory;
import it.uniroma2.ispw.ciboamico.persistence.factory.DAOFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Test della Facade di autenticazione UC-11: la boundary usa un unico punto
 * di ingresso (login) senza conoscere il controller applicativo.
 */
class AutenticazioneFacadeTest {

    private AutenticazioneFacade facade;

    @BeforeEach
    void setup() throws Exception {
        DemoDAOFactory factory = new DemoDAOFactory();
        factory.seedDemoData();
        facade = new AutenticazioneFacade((DAOFactory) factory);
    }

    @Test
    void loginConCredenzialiValideRestituisceUtente() throws Exception {
        UtenteBean utente = facade.login("mario@cibo.it", "password123");
        assertNotNull(utente);
        assertNotNull(utente.getUsername());
    }

    @Test
    void loginConCredenzialiErrateLanciaAutenticazione() {
        assertThrows(AutenticazioneException.class,
                () -> facade.login("mario@cibo.it", "sbagliata"));
    }

    @Test
    void facadeSiCostruisceConControllerIniettato() {
        AutenticazioneFacade f = new AutenticazioneFacade(new AutenticazioneController());
        assertNotNull(f);
    }
}
