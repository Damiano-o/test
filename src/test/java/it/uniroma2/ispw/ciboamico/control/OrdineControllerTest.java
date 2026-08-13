package it.uniroma2.ispw.ciboamico.control;

import it.uniroma2.ispw.ciboamico.bean.OrdineBean;
import it.uniroma2.ispw.ciboamico.entity.*;
import it.uniroma2.ispw.ciboamico.exception.InvalidStateTransitionException;
import it.uniroma2.ispw.ciboamico.pattern.observer.OrdineEvent;
import it.uniroma2.ispw.ciboamico.pattern.observer.OrdineEventPublisher;
import it.uniroma2.ispw.ciboamico.persistence.factory.DemoDAOFactory;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test del flusso di dominio dell'Ordine (UC-04) e del pattern Observer.
 *
 */
class OrdineControllerTest {

    private DemoDAOFactory factory() { return new DemoDAOFactory(); }

    private Utente utenteCompratore() {
        Utente u = new Utente("Mario", "mario@cibo.it", "hash");
        u.aggiungiRuolo(new RuoloCliente());
        return u;
    }

    private Utente utenteVenditore() {
        Utente v = new Utente("Marco", "marco@cibo.it", "hash");
        RuoloVenditore rv = new RuoloVenditore("RM", "tel");
        rv.setStato(StatoVenditoreEnum.APPROVATO);
        v.aggiungiRuolo(rv);
        return v;
    }

    @Test
    void testObserverNotificaConferma() throws Exception {
        // Il publisher notifica i listener registrati quando viene confermato un ordine,
        // passando il DTO OrdineEvent (mai l'entità Ordine).
        OrdineEventPublisher publisher = OrdineEventPublisher.getInstance();
        publisher.clearListeners();
        try {
            final boolean[] notificato = {false};
            final OrdineEvent[] ricevuto = {null};
            publisher.addListener(e -> { notificato[0] = true; ricevuto[0] = e; });

            publisher.notifyOrdineConfermato(new OrdineEvent(1L, "mario@cibo.it", "marco@cibo.it", 12.5));

            assertTrue(notificato[0]);
            assertNotNull(ricevuto[0]);
            assertEquals(1L, ricevuto[0].getNumeroOrdine());
            assertEquals("mario@cibo.it", ricevuto[0].getClienteId());
            assertEquals(12.5, ricevuto[0].getTotale(), 1e-9);
        } finally {
            publisher.clearListeners();
        }
    }





    @Test
    void testVoceOrdineParziale() throws Exception {
        Prodotto p = new Prodotto("Pane", 2.50, 10, LocalDate.now().plusDays(5),
                UnitaEnum.PEZZI, utenteVenditore().getRuolo(RuoloVenditore.class));
        VoceOrdine voce = new VoceOrdine(p, 2);
        assertEquals(5.0, voce.getParziale(), 1e-9);
    }
}
