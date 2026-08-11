package it.uniroma2.ispw.ciboamico.control;

import it.uniroma2.ispw.ciboamico.bean.AutenticazioneBean;
import it.uniroma2.ispw.ciboamico.bean.UtenteBean;
import it.uniroma2.ispw.ciboamico.control.AutenticazioneController;
import it.uniroma2.ispw.ciboamico.exception.AutenticazioneException;
import it.uniroma2.ispw.ciboamico.entity.RuoloCliente;
import it.uniroma2.ispw.ciboamico.pattern.singleton.SessionManager;
import it.uniroma2.ispw.ciboamico.entity.Utente;
import it.uniroma2.ispw.ciboamico.persistence.dao.UtenteDAO;
import it.uniroma2.ispw.ciboamico.persistence.factory.DAOFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * T10/T11/T12 — Autenticazione: login valido, email non valida, password errata.
 
 * @author Michele Damiano
*/
class AutenticazioneControllerTest {

    private DAOFactory factoryConUtente(Utente utente) throws it.uniroma2.ispw.ciboamico.exception.DAOException {
        DAOFactory factory = mock(DAOFactory.class);
        UtenteDAO dao = mock(UtenteDAO.class);
        when(factory.getUtenteDAO()).thenReturn(dao);
        when(dao.findByEmail(utente.getEmail())).thenReturn(utente);
        return factory;
    }

    @Test
    void testLoginValidRestituisceBean() throws Exception {
        Utente utente = new Utente("Mario", "user@cibo.it", Utente.hashPassword("password123"));
        utente.aggiungiRuolo(new RuoloCliente());
        AutenticazioneController controller = new AutenticazioneController(factoryConUtente(utente));

        UtenteBean bean = controller.login("user@cibo.it", "password123");

        assertNotNull(bean);
    }

    @Test
    void testLoginValidEmailCorretta() throws Exception {
        Utente utente = new Utente("Mario", "user@cibo.it", Utente.hashPassword("password123"));
        utente.aggiungiRuolo(new RuoloCliente());
        AutenticazioneController controller = new AutenticazioneController(factoryConUtente(utente));

        UtenteBean bean = controller.login("user@cibo.it", "password123");

        assertEquals("user@cibo.it", bean.getEmail());
    }

    @Test
    void testLoginConBeanRestituisceBean() throws Exception {
        Utente utente = new Utente("Mario", "user@cibo.it", Utente.hashPassword("password123"));
        utente.aggiungiRuolo(new RuoloCliente());
        AutenticazioneController controller = new AutenticazioneController(factoryConUtente(utente));

        AutenticazioneBean bean = new AutenticazioneBean();
        bean.setEmail("user@cibo.it");
        bean.setPassword("password123");
        UtenteBean risultato = controller.login(bean);

        assertNotNull(risultato);
    }

    @Test
    void testLoginConBeanEmailCorretta() throws Exception {
        Utente utente = new Utente("Mario", "user@cibo.it", Utente.hashPassword("password123"));
        utente.aggiungiRuolo(new RuoloCliente());
        AutenticazioneController controller = new AutenticazioneController(factoryConUtente(utente));

        AutenticazioneBean bean = new AutenticazioneBean();
        bean.setEmail("user@cibo.it");
        bean.setPassword("password123");
        UtenteBean risultato = controller.login(bean);

        assertEquals("user@cibo.it", risultato.getEmail());
    }

    @Test
    void testLoginBeanEmailNonValida() throws Exception {
        Utente utente = new Utente("Mario", "user@cibo.it", Utente.hashPassword("password123"));
        utente.aggiungiRuolo(new RuoloCliente());
        AutenticazioneController controller = new AutenticazioneController(factoryConUtente(utente));
        // La validazione dell'email avviene nel setter del bean (Fail Fast).
        AutenticazioneBean bean = new AutenticazioneBean();
        assertThrows(AutenticazioneException.class, () -> bean.setEmail("user_at_cibo.it"));
        bean.setEmail("user@cibo.it");
        bean.setPassword("password123");
        assertNotNull(controller.login(bean));
    }

    @Test
    void testLoginInvalidEmail() throws Exception {
        AutenticazioneController controller =
                new AutenticazioneController(mock(DAOFactory.class));
        assertThrows(AutenticazioneException.class,
                () -> controller.login("user_at_cibo.it", "password123"));
    }

    @Test
    void testLoginWrongPassword() throws Exception {
        Utente utente = new Utente("Mario", "user@cibo.it", Utente.hashPassword("password123"));
        utente.aggiungiRuolo(new RuoloCliente());
        AutenticazioneController controller = new AutenticazioneController(factoryConUtente(utente));

        assertThrows(AutenticazioneException.class,
                () -> controller.login("user@cibo.it", "wrong"));
    }

    @Test
    void testLoginSalvaUtenteInSessione() throws Exception {
        SessionManager.getInstance().logout();
        Utente utente = new Utente("Mario", "user@cibo.it", Utente.hashPassword("password123"));
        utente.aggiungiRuolo(new RuoloCliente());
        AutenticazioneController controller = new AutenticazioneController(factoryConUtente(utente));

        UtenteBean bean = controller.login("user@cibo.it", "password123");
        assertEquals(bean.getEmail(), SessionManager.getInstance().getLoggedUser().getEmail());
        SessionManager.getInstance().logout();
    }

    @Test
    void testLoginSenzaRuoliUsaClienteDefault() throws Exception {
        Utente utente = new Utente("Mario", "user@cibo.it", Utente.hashPassword("password123"));
        // nessun ruolo aggiunto -> ruolo default CLIENTE
        AutenticazioneController controller = new AutenticazioneController(factoryConUtente(utente));
        UtenteBean bean = controller.login("user@cibo.it", "password123");
        assertEquals("CLIENTE", bean.getRuoloAttivo());
    }

    @Test
    void testLogoutPulisceSessione() throws Exception {
        Utente utente = new Utente("Mario", "user@cibo.it", Utente.hashPassword("password123"));
        utente.aggiungiRuolo(new RuoloCliente());
        AutenticazioneController controller = new AutenticazioneController(factoryConUtente(utente));
        controller.login("user@cibo.it", "password123");
        assertNotNull(SessionManager.getInstance().getLoggedUser());
        controller.logout();
        assertNull(SessionManager.getInstance().getLoggedUser());
    }
}
