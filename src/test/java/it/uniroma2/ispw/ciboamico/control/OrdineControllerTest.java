package it.uniroma2.ispw.ciboamico.control;

import it.uniroma2.ispw.ciboamico.bean.OrdineBean;
import it.uniroma2.ispw.ciboamico.entity.*;
import it.uniroma2.ispw.ciboamico.exception.InvalidStateTransitionException;
import it.uniroma2.ispw.ciboamico.persistence.factory.DemoDAOFactory;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test OrdinaProdottoController (UC-04), GestisciOrdiniRicevutiController (UC-06)
 * e GestisciListaSpesaController (UC-03).
 
 * @author Michele Damiano
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
    void testObserverNotificaCambioStato() {
        Ordine ordine = new Ordine(1L, utenteCompratore(), utenteVenditore());
        final boolean[] notificato = {false};
        ordine.subscribe(o -> notificato[0] = true);

        ordine.cambiaStato(StatoOrdineEnum.CONFIRMED);

        assertTrue(notificato[0]);
    }

    @Test
    void testVisualizzaOrdiniRicevuti() {

        DemoDAOFactory factory = factory();
        GestisciOrdiniRicevutiController controller = new GestisciOrdiniRicevutiController(factory);
        Ordine ordine = new Ordine(1L, utenteCompratore(), utenteVenditore());
        factory.getOrdineDAO().save(ordine);

        List<OrdineBean> ricevuti = controller.visualizzaOrdiniRicevuti("marco@cibo.it");

        assertEquals(1, ricevuti.size());
    }
    @Test
    void testVisualizzaOrdiniRicevutiParte2() {
        DemoDAOFactory factory = factory();
        GestisciOrdiniRicevutiController controller = new GestisciOrdiniRicevutiController(factory);
        Ordine ordine = new Ordine(1L, utenteCompratore(), utenteVenditore());
        factory.getOrdineDAO().save(ordine);

        List<OrdineBean> ricevuti = controller.visualizzaOrdiniRicevuti("marco@cibo.it");

        assertEquals(1, ricevuti.size());
        assertEquals(StatoOrdineEnum.CREATED.name(), ricevuti.get(0).getStato());}

    @Test
    void testAggiornaStatoOrdine() {
        DemoDAOFactory factory = factory();
        GestisciOrdiniRicevutiController controller = new GestisciOrdiniRicevutiController(factory);
        Ordine ordine = new Ordine(1L, utenteCompratore(), utenteVenditore());
        factory.getOrdineDAO().save(ordine);

        OrdineBean aggiornato = controller.aggiornaStato(1L, "CONFIRMED");

        assertEquals(StatoOrdineEnum.CONFIRMED.name(), aggiornato.getStato());
    }

    @Test
    void testAggiornaStatoNonValido() {
        DemoDAOFactory factory = factory();
        GestisciOrdiniRicevutiController controller = new GestisciOrdiniRicevutiController(factory);
        Ordine ordine = new Ordine(1L, utenteCompratore(), utenteVenditore());
        factory.getOrdineDAO().save(ordine);

        assertThrows(InvalidStateTransitionException.class,
                () -> controller.aggiornaStato(1L, "DELIVERED")); // da CREATED non valido
    }

    @Test
    void testVoceOrdineParziale() {
        Prodotto p = new Prodotto("Pane", 2.50, 10, LocalDate.now().plusDays(5),
                UnitaEnum.PEZZI, utenteVenditore().getRuolo(RuoloVenditore.class));
        VoceOrdine voce = new VoceOrdine(p, 2);
        assertEquals(5.0, voce.getParziale(), 1e-9);
    }
}
