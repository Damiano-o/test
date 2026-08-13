package it.uniroma2.ispw.ciboamico.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

// Test di dominio per RuoloVenditore: transizione di approvazione

class RuoloVenditoreTest {

    @Test
    void approvaTransizioneDaInAttesaAdApprovato() {
        RuoloVenditore v = new RuoloVenditore("RM", "marco@cibo.it");
        assertEquals(StatoVenditoreEnum.IN_ATTESA, v.getStato());
        v.approva();
        assertEquals(StatoVenditoreEnum.APPROVATO, v.getStato());
    }

    @Test
    void approvaEIdempotenteSeGiaApprovato() {
        RuoloVenditore v = new RuoloVenditore("RM", "marco@cibo.it");
        v.approva();
        v.approva(); // monotona: resta APPROVATO
        assertEquals(StatoVenditoreEnum.APPROVATO, v.getStato());
    }

    @Test
    void setStatoConsenteRipristinoDaPersistenza() {
        // Analogamente a Ordine.ripristinaStato, setStato è il metodo di
        // ripristino dal DAO/Gson: non valida la transizione.
        RuoloVenditore v = new RuoloVenditore("RM", "marco@cibo.it");
        v.setStato(StatoVenditoreEnum.APPROVATO);
        assertEquals(StatoVenditoreEnum.APPROVATO, v.getStato());
    }
}
