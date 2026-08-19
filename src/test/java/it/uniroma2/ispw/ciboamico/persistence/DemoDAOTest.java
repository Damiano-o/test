package it.uniroma2.ispw.ciboamico.persistence;

import it.uniroma2.ispw.ciboamico.entity.Prodotto;
import it.uniroma2.ispw.ciboamico.entity.RuoloVenditore;
import it.uniroma2.ispw.ciboamico.entity.UnitaEnum;
import it.uniroma2.ispw.ciboamico.entity.Utente;
import it.uniroma2.ispw.ciboamico.persistence.impl.demo.DemoProdottoDAO;
import it.uniroma2.ispw.ciboamico.persistence.impl.demo.DemoUtenteDAO;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test DAO Demo: roundtrip utente e prodotto.
 
 * @author Michele Damiano
*/
class DemoDAOTest {

    @Test
    void testUtenteRoundtrip() {

        DemoUtenteDAO dao = new DemoUtenteDAO();
        Utente u = new Utente("Mario", "mario@cibo.it", "hash");
        dao.save(u);

        Utente trovato = dao.findByEmail("mario@cibo.it");

        assertNotNull(trovato);
    }
    @Test
    void testUtenteRoundtripParte2() {
        DemoUtenteDAO dao = new DemoUtenteDAO();
        Utente u = new Utente("Mario", "mario@cibo.it", "hash");
        dao.save(u);

        Utente trovato = dao.findByEmail("mario@cibo.it");

        assertNotNull(trovato);
        assertEquals("Mario", trovato.getNome());}

    @Test
    void testProdottoSaveAndFind() {
        DemoProdottoDAO dao = new DemoProdottoDAO();
        RuoloVenditore venditore = new RuoloVenditore("RM", "tel");
        Prodotto p = new Prodotto("Pane", 2.0, 10, LocalDate.now().plusDays(5),
                UnitaEnum.PEZZI, venditore);
        dao.save(p);

        List<Prodotto> tutti = dao.findAll();

        assertFalse(tutti.isEmpty());
    }
}
