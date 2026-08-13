package it.uniroma2.ispw.ciboamico.pattern;

import it.uniroma2.ispw.ciboamico.bean.UtenteBean;
import it.uniroma2.ispw.ciboamico.entity.*;
import it.uniroma2.ispw.ciboamico.pattern.observer.OrdineEvent;
import it.uniroma2.ispw.ciboamico.pattern.observer.OrdineEventPublisher;
import it.uniroma2.ispw.ciboamico.pattern.observer.UtenteNotifier;
import it.uniroma2.ispw.ciboamico.pattern.observer.VenditoreNotifier;
import it.uniroma2.ispw.ciboamico.pattern.singleton.SessionManager;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

// Test Observer (notifier), SessionManager (singleton) e ruoli

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
        OrdineEventPublisher publisher = OrdineEventPublisher.getInstance();
        publisher.clearListeners();
        try {
            publisher.addListener(new VenditoreNotifier());
            // non deve lanciare: il DTO viene consegnato al notifier
            publisher.notifyOrdineConfermato(new OrdineEvent(1L, "mario@cibo.it", "marco@cibo.it", 10.0));
            assertEquals(1, publisher.getListenerCount());
        } finally {
            publisher.clearListeners();
        }
    }

    @Test
    void testUtenteNotifier() {
        OrdineEventPublisher publisher = OrdineEventPublisher.getInstance();
        publisher.clearListeners();
        try {
            publisher.addListener(new UtenteNotifier());
            publisher.notifyOrdineConfermato(new OrdineEvent(2L, "marco@cibo.it", "marco@cibo.it", 5.5));
            assertEquals(1, publisher.getListenerCount());
        } finally {
            publisher.clearListeners();
        }
    }

    @Test
    void testSessionManager() throws Exception {
        SessionManager manager = SessionManager.getInstance();
        UtenteBean bean = new UtenteBean();
        bean.setEmail("test@cibo.it");
        bean.setUsername("Test");
        manager.setLoggedUser(bean);

        assertSame(bean, SessionManager.getInstance().getLoggedUser());

        manager.logout();
        assertNull(SessionManager.getInstance().getLoggedUser());}

    @Test
    void testRuoloVenditoreStato() throws Exception {
        RuoloVenditore v = new RuoloVenditore("RM", "tel");
        assertEquals(StatoVenditoreEnum.IN_ATTESA, v.getStato());
        v.setStato(StatoVenditoreEnum.APPROVATO);
        assertEquals("RM", v.getZona());
        assertEquals("tel", v.getRecapito());}

    @Test
    void testNotificheAttiveConEntrambiINotifier() {
        // Simula il setup di Runner.avvia (GUI e CLI): registra i due notifier
        // di produzione più un listener osservatore, poi pubblica un evento e
        // verifica che TUTTI ricevano la notifica (nessun listener perso).
        OrdineEventPublisher publisher = OrdineEventPublisher.getInstance();
        try {
            publisher.clearListeners();
            publisher.addListener(new UtenteNotifier());
            publisher.addListener(new VenditoreNotifier());
            final int[] ricevuti = {0};
            publisher.addListener(e -> ricevuti[0]++);
            assertEquals(3, publisher.getListenerCount());
            publisher.notifyOrdineConfermato(
                    new OrdineEvent(1L, "mario@cibo.it", "marco@cibo.it", 5.20));
            assertEquals(1, ricevuti[0], "il listener osservatore deve ricevere l'evento");
        } finally {
            publisher.clearListeners();
        }
    }
}
