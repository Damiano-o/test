package it.uniroma2.ispw.ciboamico.pattern;

import it.uniroma2.ispw.ciboamico.boundary.OrdiniRicevutiStore;
import it.uniroma2.ispw.ciboamico.pattern.observer.OrdineEvent;
import it.uniroma2.ispw.ciboamico.pattern.observer.OrdineEventPublisher;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

// Test end-to-end della notifica attiva (Observer/push) dal

class OrdiniRicevutiStoreTest {

    private OrdineEventPublisher publisher;
    private OrdiniRicevutiStore store;

    @BeforeEach
    void setup() {
        publisher = OrdineEventPublisher.getInstance();
        publisher.clearListeners();
        store = OrdiniRicevutiStore.getInstance();
        store.clear();
        // Registra lo store come observer (come fa Runner.avvia).
        publisher.addListener(store);
    }

    @AfterEach
    void cleanup() {
        publisher.clearListeners();
        store.clear();
    }

    @Test
    void clienteConfermaOrdine_ilVenditoreRiceveEventoInPush() {
        AtomicInteger notifichePush = new AtomicInteger(0);
        store.addOrdineArrivatoListener(ev -> notifichePush.incrementAndGet());

        // Il cliente (mario) conferma un ordine verso il venditore
        // questo è ciò che fa PagamentoController.submitOrdine().
        publisher.notifyOrdineConfermato(
                new OrdineEvent(1L, "mario@cibo.it", "marco@cibo.it", 5.50));

        // Push: il listener di presentazione è stato notificato subito.
        assertEquals(1, notifichePush.get(),
                "la notifica push deve arrivare senza polling");

        // Il venditore legge i PROPRII ordini (filtro per venditoreId).
        List<OrdineEvent> ricevutiMarco = store.getOrdiniPerVenditore("marco@cibo.it");
        assertEquals(1, ricevutiMarco.size());
        assertEquals("mario@cibo.it", ricevutiMarco.get(0).getClienteId());
        assertEquals(5.50, ricevutiMarco.get(0).getTotale(), 1e-9);
    }

    @Test
    void ilVenditoreNonVedeOrdiniDiAltriVenditori() {
        // Un ordine verso un ALTRO venditore non deve comparire per Marco.
        publisher.notifyOrdineConfermato(
                new OrdineEvent(2L, "mario@cibo.it", "altro@cibo.it", 3.00));

        assertTrue(store.getOrdiniPerVenditore("marco@cibo.it").isEmpty(),
                "Marco non deve vedere ordini destinati ad altri venditori");
        assertEquals(1, store.getOrdiniPerVenditore("altro@cibo.it").size());
    }

    @Test
    void venditoreFiltroCaseInsensitiveSullaEmail() {
        publisher.notifyOrdineConfermato(
                new OrdineEvent(3L, "mario@cibo.it", "marco@cibo.it", 4.20));

        assertEquals(1, store.getOrdiniPerVenditore("MARCO@CIBO.IT").size(),
                "il confronto sull'email del venditore deve essere case-insensitive");
    }
}
