package it.uniroma2.ispw.ciboamico.control.facade;

import it.uniroma2.ispw.ciboamico.bean.AutenticazioneBean;
import it.uniroma2.ispw.ciboamico.bean.OrdineBean;
import it.uniroma2.ispw.ciboamico.bean.UtenteBean;
import it.uniroma2.ispw.ciboamico.control.AutenticazioneController;
import it.uniroma2.ispw.ciboamico.exception.AutenticazioneException;
import it.uniroma2.ispw.ciboamico.pattern.singleton.SessionManager;
import it.uniroma2.ispw.ciboamico.persistence.factory.DemoDAOFactory;
import it.uniroma2.ispw.ciboamico.persistence.factory.DAOFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

// Test della Facade di autenticazione UC-11: la boundary usa un

class AutenticazioneFacadeTest {

    private AutenticazioneFacade facade;

    @BeforeEach
    void setup() throws Exception {
        DemoDAOFactory factory = new DemoDAOFactory();
        factory.seedDemoData();
        facade = new AutenticazioneFacade((DAOFactory) factory);
        SessionManager.getInstance().logout();
    }

    @AfterEach
    void cleanup() {
        SessionManager.getInstance().logout();
    }

    private AutenticazioneBean credenziali(String email, String password) throws AutenticazioneException {
        return AutenticazioneBean.fromCredenziali(email, password);
    }

    @Test
    void loginConCredenzialiValideRestituisceUtente() throws Exception {
        UtenteBean utente = facade.login(credenziali("mario@cibo.it", "123"));
        assertNotNull(utente);
        assertNotNull(utente.getUsername());
    }

    @Test
    void loginConCredenzialiErrateLanciaAutenticazione() {
        assertThrows(AutenticazioneException.class,
                () -> facade.login(credenziali("mario@cibo.it", "sbagliata")));
    }

    @Test
    void facadeSiCostruisceConControllerIniettato() {
        AutenticazioneFacade f = new AutenticazioneFacade(new AutenticazioneController());
        assertNotNull(f);
    }

    // ---- Gestione sessione (responsabilità del Facade, non del

    @Test
    void loginSalvaUtenteInSessione() throws Exception {
        UtenteBean bean = facade.login(credenziali("mario@cibo.it", "123"));
        assertEquals(bean.getEmail(), SessionManager.getInstance().getLoggedUser().getEmail());
    }

    @Test
    void logoutPulisceSessione() throws Exception {
        facade.login(credenziali("mario@cibo.it", "123"));
        assertNotNull(SessionManager.getInstance().getLoggedUser());
        // Igiene di stato: un checkout in corso non deve essere ereditato
        // da un successivo utente dopo il logout.
        SessionManager.getInstance().setOrdineInCorso(new OrdineBean());
        facade.logout();
        assertNull(SessionManager.getInstance().getLoggedUser());
        assertNull(SessionManager.getInstance().getOrdineInCorso());
    }
}
