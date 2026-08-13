package it.uniroma2.ispw.ciboamico.persistence;

import it.uniroma2.ispw.ciboamico.entity.*;
import it.uniroma2.ispw.ciboamico.persistence.impl.fs.FSBuonoDAO;
import it.uniroma2.ispw.ciboamico.persistence.impl.fs.FSOrdineDAO;
import it.uniroma2.ispw.ciboamico.persistence.impl.fs.FSProdottoDAO;
import it.uniroma2.ispw.ciboamico.persistence.impl.fs.FSUtenteDAO;
import it.uniroma2.ispw.ciboamico.pattern.strategy.ScontoImportoFissoStrategy;
import it.uniroma2.ispw.ciboamico.pattern.strategy.ScontoPercentualeStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

// Test DAO FS: roundtrip su file JSON (NFR-01 persistenza)

class FSDaoTest {

    @BeforeEach
    void pulisciDati() {
        // I file JSON persistono tra le run — pulizia per test
        try {
            Path dir = Path.of("data");
            if (Files.exists(dir)) {
                try (var stream = Files.list(dir)) {
                    stream.forEach(f -> {
                        try { Files.deleteIfExists(f); } catch (IOException ignored) { /* pulizia best-effort: se fallisce, il test successivo gestisce */ }
                    });
                }
            }
        } catch (IOException ignored) { /* pulizia best-effort: se fallisce, il test successivo gestisce */ }
    }

    @Test
    void testUtenteFSCircolare() throws Exception {
        FSUtenteDAO dao = new FSUtenteDAO();
        Utente u = new Utente("Anna", "anna@cibo.it", "hash");
        dao.save(u);

        Utente trovato = dao.findByEmail("anna@cibo.it");

        assertNotNull(trovato);
        assertEquals("Anna", trovato.getNome());}

    @Test
    void testUtenteFSConRuoloVenditoreNonCiclico() throws Exception {
        // Regressione: il back-link al transient evita lo StackOverflow di
        // sulla persistenza FS di un Utente che possiede un RuoloVenditore
        // (relazione unidirezionale in persistenza).
        FSUtenteDAO dao = new FSUtenteDAO();
        Utente v = new Utente("Marco", "marco@cibo.it", "h");
        RuoloVenditore rv = new RuoloVenditore("RM", "tel");
        v.aggiungiRuolo(rv);
        dao.save(v); // prima: StackOverflowError; ora: senza ciclo

        Utente ricostruito = dao.findByEmail("marco@cibo.it");
        assertNotNull(ricostruito);
        assertTrue(ricostruito.haRuolo(RuoloVenditore.class));
        assertEquals("RM", ricostruito.getRuolo(RuoloVenditore.class).getZona());
        // il back-link non è persistito (resta null): la navigazione di
        // nei ruoli si ristabilisce a runtime quando serve (ricostruzione
        assertNull(ricostruito.getRuolo(RuoloVenditore.class).getUtente());
    }

    @Test
    void testBuonoFSCircolare() throws Exception {
        // Persistenza FS completa di un venditore e dei suoi buoni
        // verifica che il ScontoStrategy sia ricostruita e il venditore
        // dal DAO utenti (niente ciclo Gson, grazie alla relazione
        FSUtenteDAO utenteDao = new FSUtenteDAO();
        Utente v = new Utente("Marco", "marco@cibo.it", "h");
        RuoloVenditore rv = new RuoloVenditore("RM", "tel");
        v.aggiungiRuolo(rv);
        utenteDao.save(v);

        FSBuonoDAO dao = new FSBuonoDAO(utenteDao);
        dao.save(new BuonoPromozionale("SALUTI20", rv,
                LocalDate.now().minusDays(1), LocalDate.now().plusDays(30),
                new ScontoPercentualeStrategy(0.20)));
        dao.save(new BuonoPromozionale("FISSO", rv,
                LocalDate.now().minusDays(1), LocalDate.now().plusDays(30),
                new ScontoImportoFissoStrategy(5.0)));

        BuonoPromozionale trovato = dao.findByCodice("SALUTI20");
        assertNotNull(trovato);
        assertEquals("SALUTI20", trovato.getCodice());
        assertEquals("VENDITORE", trovato.getVenditore().getNomeRuolo());
        // strategia percentuale ricostruita: -20% su 10 -> 8
        assertEquals(8.0, trovato.applicaSconto(10.0), 1e-9);

        assertEquals(2, dao.findByVenditoreEmail("marco@cibo.it").size());
    }

    @Test
    void testProdottoFSCircolare() throws Exception {
        FSProdottoDAO dao = new FSProdottoDAO();
        RuoloVenditore v = new RuoloVenditore("RM", "tel");
        Prodotto p = new Prodotto("Pomodori", 2.0, 50, LocalDate.now().plusDays(7),
                UnitaEnum.GRAMMI, v);
        dao.save(p);

        List<Prodotto> tutti = dao.findAll();

        assertFalse(tutti.isEmpty());
    }

    @Test
    void testProdottoFindByNome() throws Exception {
        FSProdottoDAO dao = new FSProdottoDAO();
        RuoloVenditore v = new RuoloVenditore("RM", "tel");
        dao.save(new Prodotto("Pomodori", 2.0, 50, LocalDate.now().plusDays(7),
                UnitaEnum.GRAMMI, v));

        Prodotto trovato = dao.findByNome("Pomodori");

        assertNotNull(trovato);
        assertEquals("Pomodori", trovato.getNome());
    }

    @Test
    void testOrdineFSCircolare() throws Exception {
        FSOrdineDAO dao = new FSOrdineDAO();
        Utente c = new Utente("C", "c@cibo.it", "h");
        Utente v = new Utente("V", "v@cibo.it", "h");
        Ordine ordine = new Ordine(99L, c, v);
        dao.save(ordine);

        Ordine trovato = dao.findById(99L);

        assertNotNull(trovato);
        assertEquals("v@cibo.it", trovato.getVenditore().getEmail());}

    @Test
    void testOrdineFSByVenditore() throws Exception {
        FSOrdineDAO dao = new FSOrdineDAO();
        Utente c = new Utente("C", "c@cibo.it", "h");
        Utente v = new Utente("V", "v@cibo.it", "h");
        dao.save(new Ordine(1L, c, v));

        assertEquals(1, dao.findByVenditore("v@cibo.it").size());
        assertEquals(1, dao.findByCompratore("c@cibo.it").size());}
}
