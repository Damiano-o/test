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
 * Test del flusso di dominio dell'Ordine (UC-04) e del pattern Observer.
 *
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
    void testObserverNotificaCambioStato() throws Exception {
        Ordine ordine = new Ordine(1L, utenteCompratore(), utenteVenditore());
        final boolean[] notificato = {false};
        ordine.subscribe(o -> notificato[0] = true);

        ordine.cambiaStato(StatoOrdineEnum.CONFIRMED);

        assertTrue(notificato[0]);
    }





    @Test
    void testVoceOrdineParziale() throws Exception {
        Prodotto p = new Prodotto("Pane", 2.50, 10, LocalDate.now().plusDays(5),
                UnitaEnum.PEZZI, utenteVenditore().getRuolo(RuoloVenditore.class));
        VoceOrdine voce = new VoceOrdine(p, 2);
        assertEquals(5.0, voce.getParziale(), 1e-9);
    }
}
