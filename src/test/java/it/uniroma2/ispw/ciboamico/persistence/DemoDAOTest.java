package it.uniroma2.ispw.ciboamico.persistence;

import it.uniroma2.ispw.ciboamico.entity.Prodotto;
import it.uniroma2.ispw.ciboamico.entity.RuoloVenditore;
import it.uniroma2.ispw.ciboamico.entity.UnitaEnum;
import it.uniroma2.ispw.ciboamico.entity.Utente;
import it.uniroma2.ispw.ciboamico.persistence.factory.DemoDAOFactory;
import it.uniroma2.ispw.ciboamico.persistence.impl.demo.DemoProdottoDAO;
import it.uniroma2.ispw.ciboamico.persistence.impl.demo.DemoUtenteDAO;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DemoDAOTest {

    @Test
    void testUtenteRoundtrip() throws Exception {
        DemoUtenteDAO dao = new DemoUtenteDAO();
        Utente utente = new Utente("Mario", "mario@cibo.it", "hash");
        dao.save(utente);

        Utente trovato = dao.findByEmail("mario@cibo.it");

        assertNotNull(trovato);
        assertEquals("Mario", trovato.getNome());
    }

    @Test
    void testProdottoSaveAndFind() throws Exception {
        DemoProdottoDAO dao = new DemoProdottoDAO();
        RuoloVenditore venditore = new RuoloVenditore("RM", "tel");
        Prodotto prodotto = new Prodotto("Pane", 2.0, 10,
                LocalDate.now().plusDays(5), UnitaEnum.PEZZI, venditore);
        dao.save(prodotto);

        List<Prodotto> prodotti = dao.findAll();

        assertFalse(prodotti.isEmpty());
    }

    @Test
    void testProdottoFindByNomeCaseInsensitive() throws Exception {
        DemoProdottoDAO dao = new DemoProdottoDAO();
        RuoloVenditore venditore = new RuoloVenditore("RM", "tel");
        dao.save(new Prodotto("Pane", 2.0, 10,
                LocalDate.now().plusDays(5), UnitaEnum.PEZZI, venditore));

        Prodotto trovato = dao.findByNome("pane");

        assertNotNull(trovato);
        assertEquals("Pane", trovato.getNome());
    }

    @Test
    void testSeedDemoDataContieneUtentiEProdottiMarketplace() throws Exception {
        DemoDAOFactory factory = new DemoDAOFactory();
        factory.seedDemoData();

        assertNotNull(factory.getUtenteDAO().findByEmail("mario@cibo.it"));
        assertNotNull(factory.getUtenteDAO().findByEmail("marco@cibo.it"));
        assertFalse(factory.getProdottoDAO().findAll().isEmpty());
    }

    @Test
    void testSeedIdempotenteNonDuplica() throws Exception {
        DemoDAOFactory factory = new DemoDAOFactory();
        factory.seedDemoData();
        int prodotti = factory.getProdottoDAO().findAll().size();
        boolean marioPresente = factory.getUtenteDAO().findByEmail("mario@cibo.it") != null;
        boolean marcoPresente = factory.getUtenteDAO().findByEmail("marco@cibo.it") != null;

        factory.seedDemoData();

        assertEquals(prodotti, factory.getProdottoDAO().findAll().size());
        assertEquals(marioPresente,
                factory.getUtenteDAO().findByEmail("mario@cibo.it") != null);
        assertEquals(marcoPresente,
                factory.getUtenteDAO().findByEmail("marco@cibo.it") != null);
    }
}
