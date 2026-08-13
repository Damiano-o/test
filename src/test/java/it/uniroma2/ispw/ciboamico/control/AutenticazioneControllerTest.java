package it.uniroma2.ispw.ciboamico.control;

import it.uniroma2.ispw.ciboamico.bean.AutenticazioneBean;
import it.uniroma2.ispw.ciboamico.bean.UtenteBean;
import it.uniroma2.ispw.ciboamico.control.AutenticazioneController;
import it.uniroma2.ispw.ciboamico.exception.AutenticazioneException;
import it.uniroma2.ispw.ciboamico.exception.DAOException;
import it.uniroma2.ispw.ciboamico.entity.RuoloCliente;
import it.uniroma2.ispw.ciboamico.entity.Utente;
import it.uniroma2.ispw.ciboamico.persistence.dao.UtenteDAO;
import it.uniroma2.ispw.ciboamico.persistence.factory.DAOFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

// T10/T11/T12 — Autenticazione: login valido, email non valida, password errata

class AutenticazioneControllerTest {

    private DAOFactory factoryConUtente(Utente utente) throws DAOException {
        DAOFactory factory = mock(DAOFactory.class);
        UtenteDAO dao = mock(UtenteDAO.class);
        when(factory.getUtenteDAO()).thenReturn(dao);
        when(dao.findByEmail(utente.getEmail())).thenReturn(utente);
        return factory;
    }

    private AutenticazioneBean credenziali(String email, String password) throws AutenticazioneException {
        return AutenticazioneBean.fromCredenziali(email, password);
    }

    @Test
    void testLoginValidRestituisceBean() throws Exception {
        Utente utente = new Utente("Mario", "user@cibo.it", Utente.hashPassword("password123"));
        utente.aggiungiRuolo(new RuoloCliente());
        AutenticazioneController controller = new AutenticazioneController(factoryConUtente(utente));

        UtenteBean bean = controller.login(credenziali("user@cibo.it", "password123"));

        assertNotNull(bean);
    }

    @Test
    void testLoginValidEmailCorretta() throws Exception {
        Utente utente = new Utente("Mario", "user@cibo.it", Utente.hashPassword("password123"));
        utente.aggiungiRuolo(new RuoloCliente());
        AutenticazioneController controller = new AutenticazioneController(factoryConUtente(utente));

        UtenteBean bean = controller.login(credenziali("user@cibo.it", "password123"));

        assertEquals("user@cibo.it", bean.getEmail());
    }

    @Test
    void testLoginConBeanRestituisceBean() throws Exception {
        Utente utente = new Utente("Mario", "user@cibo.it", Utente.hashPassword("password123"));
        utente.aggiungiRuolo(new RuoloCliente());
        AutenticazioneController controller = new AutenticazioneController(factoryConUtente(utente));

        AutenticazioneBean bean = credenziali("user@cibo.it", "password123");
        UtenteBean risultato = controller.login(bean);

        assertNotNull(risultato);
    }

    @Test
    void testLoginConBeanEmailCorretta() throws Exception {
        Utente utente = new Utente("Mario", "user@cibo.it", Utente.hashPassword("password123"));
        utente.aggiungiRuolo(new RuoloCliente());
        AutenticazioneController controller = new AutenticazioneController(factoryConUtente(utente));

        AutenticazioneBean bean = credenziali("user@cibo.it", "password123");
        UtenteBean risultato = controller.login(bean);

        assertEquals("user@cibo.it", risultato.getEmail());
    }

    @Test
    void testLoginBeanEmailNonValida() throws Exception {
        Utente utente = new Utente("Mario", "user@cibo.it", Utente.hashPassword("password123"));
        utente.aggiungiRuolo(new RuoloCliente());
        AutenticazioneController controller = new AutenticazioneController(factoryConUtente(utente));
        // La validazione dell'email avviene nel setter del bean (Fail Fast),
        // chiamato dalla conversione fromCredenziali.
        assertThrows(AutenticazioneException.class,
                () -> AutenticazioneBean.fromCredenziali("user_at_cibo.it", "password123"));
        assertNotNull(controller.login(credenziali("user@cibo.it", "password123")));
    }

    @Test
    void testLoginInvalidEmail() throws Exception {
        AutenticazioneController controller =
                new AutenticazioneController(mock(DAOFactory.class));
        // Email malformata: il bean rifiuta in fase di costruzione (Fail Fast).
        assertThrows(AutenticazioneException.class,
                () -> AutenticazioneBean.fromCredenziali("user_at_cibo.it", "password123"));
    }

    @Test
    void testLoginWrongPassword() throws Exception {
        Utente utente = new Utente("Mario", "user@cibo.it", Utente.hashPassword("password123"));
        utente.aggiungiRuolo(new RuoloCliente());
        AutenticazioneController controller = new AutenticazioneController(factoryConUtente(utente));

        assertThrows(AutenticazioneException.class,
                () -> controller.login(credenziali("user@cibo.it", "wrong")));
    }

    @Test
    void testLoginSenzaRuoliUsaClienteDefault() throws Exception {
        Utente utente = new Utente("Mario", "user@cibo.it", Utente.hashPassword("password123"));
        // nessun ruolo aggiunto -> ruolo default CLIENTE
        AutenticazioneController controller = new AutenticazioneController(factoryConUtente(utente));
        UtenteBean bean = controller.login(credenziali("user@cibo.it", "password123"));
        assertEquals("CLIENTE", bean.getRuoloAttivo());
    }

    @Test
    void testLoginAccountInesistenteLancia() throws Exception {
        // DAO che non trova l'account (ntorna null) -> credenziali respinte.
        DAOFactory factory = mock(DAOFactory.class);
        UtenteDAO dao = mock(UtenteDAO.class);
        when(factory.getUtenteDAO()).thenReturn(dao);
        AutenticazioneController controller = new AutenticazioneController(factory);
        assertThrows(AutenticazioneException.class,
                () -> controller.login(credenziali("utente@cibo.it", "password123")));
    }
}
