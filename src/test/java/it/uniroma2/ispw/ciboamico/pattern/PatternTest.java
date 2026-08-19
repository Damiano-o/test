package it.uniroma2.ispw.ciboamico.pattern;

import it.uniroma2.ispw.ciboamico.bean.UtenteBean;
import it.uniroma2.ispw.ciboamico.entity.*;
import it.uniroma2.ispw.ciboamico.pattern.observer.UtenteNotifier;
import it.uniroma2.ispw.ciboamico.pattern.observer.VenditoreNotifier;
import it.uniroma2.ispw.ciboamico.pattern.singleton.SessionManager;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test Observer (notifier), SessionManager (singleton) e ruoli.
 
 * @author Michele Damiano
*/
class PatternTest {

    private Utente compratore() {
        Utente u = new Utente("Mario", "mario@cibo.it", "h");
        u.aggiungiRuolo(new RuoloCliente());
        return u;
    }

    private Utente venditore() {
        Utente v = new Utente("Marco", "marco@cibo.it", "h");
        v.aggiungiRuolo(new RuoloVenditore("RM", "tel"));
        return v;
    }

    @Test
    void testVenditoreNotifier() {
        Ordine ordine = new Ordine(1L, compratore(), venditore());
        ordine.subscribe(new VenditoreNotifier());
        ordine.cambiaStato(StatoOrdineEnum.CONFIRMED); // non deve lanciare
        assertEquals(StatoOrdineEnum.CONFIRMED, ordine.getStato());
    }

    @Test
    void testUtenteNotifier() {
        Ordine ordine = new Ordine(1L, compratore(), venditore());
        ordine.subscribe(new UtenteNotifier());
        ordine.cambiaStato(StatoOrdineEnum.CONFIRMED);
        assertEquals(StatoOrdineEnum.CONFIRMED, ordine.getStato());
    }

    @Test
    void testSessionManager() {

        SessionManager manager = SessionManager.getInstance();
        UtenteBean bean = new UtenteBean();
        bean.setEmail("test@cibo.it");
        bean.setUsername("Test");
        manager.setLoggedUser(bean);

        assertSame(bean, SessionManager.getInstance().getLoggedUser());
    }
    @Test
    void testSessionManagerParte2() {
        SessionManager manager = SessionManager.getInstance();
        UtenteBean bean = new UtenteBean();
        bean.setEmail("test@cibo.it");
        bean.setUsername("Test");
        manager.setLoggedUser(bean);

        assertSame(bean, SessionManager.getInstance().getLoggedUser());

        manager.logout();
        assertNull(SessionManager.getInstance().getLoggedUser());}

    @Test
    void testRuoloVenditoreStato() {

        RuoloVenditore v = new RuoloVenditore("RM", "tel");
        assertEquals(StatoVenditoreEnum.IN_ATTESA, v.getStato());
    }
    @Test
    void testRuoloVenditoreStatoParte2() {
        RuoloVenditore v = new RuoloVenditore("RM", "tel");
        assertEquals(StatoVenditoreEnum.IN_ATTESA, v.getStato());
        v.setStato(StatoVenditoreEnum.APPROVATO);
        assertEquals("RM", v.getZona());}
    @Test
    void testRuoloVenditoreStatoParte3() {
        RuoloVenditore v = new RuoloVenditore("RM", "tel");
        assertEquals(StatoVenditoreEnum.IN_ATTESA, v.getStato());
        v.setStato(StatoVenditoreEnum.APPROVATO);
        assertEquals("RM", v.getZona());
        assertEquals("tel", v.getRecapito());}
}
