package it.uniroma2.ispw.ciboamico.persistence;

import it.uniroma2.ispw.ciboamico.entity.*;
import it.uniroma2.ispw.ciboamico.persistence.impl.fs.FSOrdineDAO;
import it.uniroma2.ispw.ciboamico.persistence.impl.fs.FSProdottoDAO;
import it.uniroma2.ispw.ciboamico.persistence.impl.fs.FSRicettaDAO;
import it.uniroma2.ispw.ciboamico.persistence.impl.fs.FSUtenteDAO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test DAO FS: roundtrip su file JSON (NFR-01 persistenza).
 * I file vengono scritti in data/ sotto il progetto (cartella gitignored).
 
 * @author Michele Damiano
*/
class FSDaoTest {

    @BeforeEach
    void pulisciDati() {
        // I file JSON persistono tra le run — pulizia per test indipendenti
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
    void testUtenteFSCircolare() {

        FSUtenteDAO dao = new FSUtenteDAO();
        Utente u = new Utente("Anna", "anna@cibo.it", "hash");
        dao.save(u);

        Utente trovato = dao.findByEmail("anna@cibo.it");

        assertNotNull(trovato);
    }
    @Test
    void testUtenteFSCircolareParte2() {
        FSUtenteDAO dao = new FSUtenteDAO();
        Utente u = new Utente("Anna", "anna@cibo.it", "hash");
        dao.save(u);

        Utente trovato = dao.findByEmail("anna@cibo.it");

        assertNotNull(trovato);
        assertEquals("Anna", trovato.getNome());}

    @Test
    void testProdottoFSCircolare() {
        FSProdottoDAO dao = new FSProdottoDAO();
        RuoloVenditore v = new RuoloVenditore("RM", "tel");
        Prodotto p = new Prodotto("Pomodori", 2.0, 50, LocalDate.now().plusDays(7),
                UnitaEnum.GRAMMI, v);
        dao.save(p);

        List<Prodotto> tutti = dao.findAll();

        assertFalse(tutti.isEmpty());
    }

    @Test
    void testRicettaFSCircolare() {
        FSRicettaDAO dao = new FSRicettaDAO();
        Ricetta r = new Ricetta("Insalata", "tagliare", new RuoloNutrizionista());
        dao.save(r);

        assertEquals(1, dao.findByStato("PROPOSTA").size());
    }

    @Test
    void testOrdineFSCircolare() {

        FSOrdineDAO dao = new FSOrdineDAO();
        Utente c = new Utente("C", "c@cibo.it", "h");
        Utente v = new Utente("V", "v@cibo.it", "h");
        Ordine ordine = new Ordine(99L, c, v);
        dao.save(ordine);

        Ordine trovato = dao.findById(99L);

        assertNotNull(trovato);
    }
    @Test
    void testOrdineFSCircolareParte2() {
        FSOrdineDAO dao = new FSOrdineDAO();
        Utente c = new Utente("C", "c@cibo.it", "h");
        Utente v = new Utente("V", "v@cibo.it", "h");
        Ordine ordine = new Ordine(99L, c, v);
        dao.save(ordine);

        Ordine trovato = dao.findById(99L);

        assertNotNull(trovato);
        assertEquals("v@cibo.it", trovato.getVenditore().getEmail());}

    @Test
    void testOrdineFSByVenditore() {

        FSOrdineDAO dao = new FSOrdineDAO();
        Utente c = new Utente("C", "c@cibo.it", "h");
        Utente v = new Utente("V", "v@cibo.it", "h");
        dao.save(new Ordine(1L, c, v));

        assertEquals(1, dao.findByVenditore("v@cibo.it").size());
    }
    @Test
    void testOrdineFSByVenditoreParte2() {
        FSOrdineDAO dao = new FSOrdineDAO();
        Utente c = new Utente("C", "c@cibo.it", "h");
        Utente v = new Utente("V", "v@cibo.it", "h");
        dao.save(new Ordine(1L, c, v));

        assertEquals(1, dao.findByVenditore("v@cibo.it").size());
        assertEquals(1, dao.findByCompratore("c@cibo.it").size());}
}
