package it.uniroma2.ispw.ciboamico.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import it.uniroma2.ispw.ciboamico.exception.BusinessValidationException;
import it.uniroma2.ispw.ciboamico.exception.InvalidStateTransitionException;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * T05/T06/T07 — Ordine: totale e transizioni di stato (BR-04).
 
 * @author Michele Damiano
*/
class OrdineTest {

    private Utente compratore() {
        Utente u = new Utente("Mario", "mario@cibo.it", "hash");
        u.aggiungiRuolo(new RuoloCliente());
        return u;
    }

    private Utente venditore() {
        Utente v = new Utente("Marco", "marco@cibo.it", "hash");
        RuoloVenditore rv = new RuoloVenditore("RM", "tel");
        rv.setStato(StatoVenditoreEnum.APPROVATO);
        v.aggiungiRuolo(rv);
        return v;
    }

    @Test
    void testCalcolaTotale() {
        Prodotto pane = new Prodotto("Pane", 2.50, 10, java.time.LocalDate.now().plusDays(5),
                UnitaEnum.PEZZI, venditore().getRuolo(RuoloVenditore.class));
        Prodotto uovo = new Prodotto("Uovo", 0.50, 10, java.time.LocalDate.now().plusDays(20),
                UnitaEnum.PEZZI, venditore().getRuolo(RuoloVenditore.class));

        Ordine ordine = new Ordine(1L, compratore(), venditore());
        ordine.aggiungiVoce(new VoceOrdine(pane, 1));
        ordine.aggiungiVoce(new VoceOrdine(uovo, 2));

        assertEquals(3.50, ordine.getTotale(), 1e-9);
    }

    @Test
    void testSetStatoValid() {
        Ordine ordine = new Ordine(1L, compratore(), venditore());
        ordine.cambiaStato(StatoOrdineEnum.CONFIRMED);
        assertEquals(StatoOrdineEnum.CONFIRMED, ordine.getStato());
    }

    @Test
    void testSetStatoInvalid() {
        Ordine ordine = new Ordine(1L, compratore(), venditore());
        // Sequenza valida fino a IN_DELIVERY (BR-04)
        ordine.cambiaStato(StatoOrdineEnum.CONFIRMED);
        ordine.cambiaStato(StatoOrdineEnum.IN_DELIVERY);
        // IN_DELIVERY → ANNULLED non è previsto (solo DELIVERED)
        assertThrows(InvalidStateTransitionException.class,
                () -> ordine.cambiaStato(StatoOrdineEnum.ANNULLED));
    }

    @Test
    void testAutoAcquistoVietato() {
        Utente stesso = compratore(); // stesso utente compra il proprio prodotto
        assertThrows(BusinessValidationException.class,
                () -> new Ordine(1L, stesso, stesso));
    }
}
