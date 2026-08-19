package it.uniroma2.ispw.ciboamico.control;

import it.uniroma2.ispw.ciboamico.bean.AutenticazioneBean;
import it.uniroma2.ispw.ciboamico.bean.UtenteBean;
import it.uniroma2.ispw.ciboamico.exception.AutenticazioneException;
import it.uniroma2.ispw.ciboamico.entity.RuoloCliente;
import it.uniroma2.ispw.ciboamico.entity.Utente;
import it.uniroma2.ispw.ciboamico.persistence.dao.UtenteDAO;
import it.uniroma2.ispw.ciboamico.persistence.factory.DAOFactory;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * T10/T11/T12 — Autenticazione: login valido, email non valida, password errata.
 
 * @author Michele Damiano
*/
class AutenticazioneControllerTest {

    private String hash(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return java.util.HexFormat.of().formatHex(
                    md.digest(("ciboamico-salt" + password).getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private DAOFactory factoryConUtente(Utente utente) {
        DAOFactory factory = mock(DAOFactory.class);
        UtenteDAO dao = mock(UtenteDAO.class);
        when(factory.getUtenteDAO()).thenReturn(dao);
        when(dao.findByEmail(utente.getEmail())).thenReturn(utente);
        return factory;
    }

    @Test
    void testLoginValidRestituisceBean() {
        Utente utente = new Utente("Mario", "user@cibo.it", hash("password123"));
        utente.aggiungiRuolo(new RuoloCliente());
        AutenticazioneController controller = new AutenticazioneController(factoryConUtente(utente));

        UtenteBean bean = controller.login("user@cibo.it", "password123");

        assertNotNull(bean);
    }

    @Test
    void testLoginValidEmailCorretta() {
        Utente utente = new Utente("Mario", "user@cibo.it", hash("password123"));
        utente.aggiungiRuolo(new RuoloCliente());
        AutenticazioneController controller = new AutenticazioneController(factoryConUtente(utente));

        UtenteBean bean = controller.login("user@cibo.it", "password123");

        assertEquals("user@cibo.it", bean.getEmail());
    }

    @Test
    void testLoginConBeanRestituisceBean() {
        Utente utente = new Utente("Mario", "user@cibo.it", hash("password123"));
        utente.aggiungiRuolo(new RuoloCliente());
        AutenticazioneController controller = new AutenticazioneController(factoryConUtente(utente));

        AutenticazioneBean bean = new AutenticazioneBean();
        bean.setEmail("user@cibo.it");
        bean.setPassword("password123");
        UtenteBean risultato = controller.login(bean);

        assertNotNull(risultato);
    }

    @Test
    void testLoginConBeanEmailCorretta() {
        Utente utente = new Utente("Mario", "user@cibo.it", hash("password123"));
        utente.aggiungiRuolo(new RuoloCliente());
        AutenticazioneController controller = new AutenticazioneController(factoryConUtente(utente));

        AutenticazioneBean bean = new AutenticazioneBean();
        bean.setEmail("user@cibo.it");
        bean.setPassword("password123");
        UtenteBean risultato = controller.login(bean);

        assertEquals("user@cibo.it", risultato.getEmail());
    }

    @Test
    void testLoginBeanEmailNonValida() {
        Utente utente = new Utente("Mario", "user@cibo.it", hash("password123"));
        utente.aggiungiRuolo(new RuoloCliente());
        AutenticazioneController controller = new AutenticazioneController(factoryConUtente(utente));
        AutenticazioneBean bean = new AutenticazioneBean();
        bean.setEmail("user_at_cibo.it");   // formato errato
        bean.setPassword("password123");
        assertThrows(AutenticazioneException.class, () -> controller.login(bean));
    }

    @Test
    void testLoginInvalidEmail() {
        AutenticazioneController controller =
                new AutenticazioneController(mock(DAOFactory.class));
        assertThrows(AutenticazioneException.class,
                () -> controller.login("user_at_cibo.it", "password123"));
    }

    @Test
    void testLoginWrongPassword() {
        Utente utente = new Utente("Mario", "user@cibo.it", hash("password123"));
        utente.aggiungiRuolo(new RuoloCliente());
        AutenticazioneController controller = new AutenticazioneController(factoryConUtente(utente));

        assertThrows(AutenticazioneException.class,
                () -> controller.login("user@cibo.it", "wrong"));
    }
}
