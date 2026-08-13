package it.uniroma2.ispw.ciboamico.entity;

import it.uniroma2.ispw.ciboamico.exception.BusinessValidationException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

// T08/T09 — Prodotto: validazioni prezzo (BR-06) e quantità (BR-03)

class ProdottoTest {

    private RuoloVenditore venditoreApprovato() {
        RuoloVenditore v = new RuoloVenditore("RM", "tel");
        v.setStato(StatoVenditoreEnum.APPROVATO);
        return v;
    }

    @Test
    void testValidaPrezzo() throws Exception {
        LocalDate scadenza = LocalDate.now().plusDays(5);
        RuoloVenditore venditore = venditoreApprovato();
        assertThrows(BusinessValidationException.class,
                () -> new Prodotto("Pane", -1.50, 10, scadenza, UnitaEnum.PEZZI, venditore));
    }

    @Test
    void testValidaQuantita() throws Exception {
        LocalDate scadenza = LocalDate.now().plusDays(5);
        RuoloVenditore venditore = venditoreApprovato();
        assertThrows(BusinessValidationException.class,
                () -> new Prodotto("Pane", 2.0, -5, scadenza, UnitaEnum.PEZZI, venditore));
    }

    @Test
    void testValidaScadenzaPassata() throws Exception {
        RuoloVenditore venditore = venditoreApprovato();
        LocalDate scaduta = LocalDate.now().minusDays(1);
        assertThrows(BusinessValidationException.class,
                () -> new Prodotto("Pane", 2.0, 5, scaduta, UnitaEnum.PEZZI, venditore));
    }

    @Test
    void testRiduciDisponibilita() throws Exception {
        Prodotto p = new Prodotto("Pane", 2.0, 5, LocalDate.now().plusDays(5),
                UnitaEnum.PEZZI, venditoreApprovato());
        p.riduciDisponibilita(3);
        assertEquals(2, p.getQuantitaDisponibile());
    }

    @Test
    void testRiduciDisponibilitaQuantitaEccessiva() throws Exception {
        Prodotto p = new Prodotto("Pane", 2.0, 5, LocalDate.now().plusDays(5),
                UnitaEnum.PEZZI, venditoreApprovato());
        assertThrows(BusinessValidationException.class,
                () -> p.riduciDisponibilita(10));
        assertEquals(5, p.getQuantitaDisponibile());}
}
