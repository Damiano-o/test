package it.uniroma2.ispw.ciboamico.pattern;

import it.uniroma2.ispw.ciboamico.entity.Ordine;
import it.uniroma2.ispw.ciboamico.entity.Utente;
import it.uniroma2.ispw.ciboamico.pattern.factory.OrdineLazyFactory;
import it.uniroma2.ispw.ciboamico.persistence.factory.DemoDAOFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

// Test OrdineLazyFactory (Lazy Initialization + caching, id dal

class OrdineLazyFactoryTest {

    private DemoDAOFactory factory;

    @BeforeEach
    void setup() {
        factory = new DemoDAOFactory();
        OrdineLazyFactory.reset();
    }

    @AfterEach
    void cleanup() {
        OrdineLazyFactory.reset();
    }

    private Utente utente(String nome, String email) {
        return new Utente(nome, email, "h");
    }

    @Test
    void testNewOrdineAssegnaIdIncrementale() throws Exception {
        OrdineLazyFactory.configure(factory);
        OrdineLazyFactory f = OrdineLazyFactory.getInstance();
        Ordine primo = f.newOrdine(utente("Mario", "mario@cibo.it"), utente("Marco", "marco@cibo.it"));
        Ordine secondo = f.newOrdine(utente("Mario", "mario@cibo.it"), utente("Marco", "marco@cibo.it"));

        assertNotEquals(primo.getIdOrdine(), secondo.getIdOrdine());
        assertEquals(1L, primo.getIdOrdine());
        assertEquals(2L, secondo.getIdOrdine());
    }

    @Test
    void testNewOrdineStatoCreated() throws Exception {
        OrdineLazyFactory.configure(factory);
        OrdineLazyFactory f = OrdineLazyFactory.getInstance();
        Ordine ordine = f.newOrdine(utente("Mario", "mario@cibo.it"), utente("Marco", "marco@cibo.it"));

        assertEquals("CREATED", ordine.getStato().name());
    }

    @Test
    void testCacheContieneOrdiniCreati() throws Exception {
        OrdineLazyFactory.configure(factory);
        OrdineLazyFactory f = OrdineLazyFactory.getInstance();
        f.newOrdine(utente("Mario", "mario@cibo.it"), utente("Marco", "marco@cibo.it"));

        assertEquals(1, f.getCacheOrdini().size());
    }

    @Test
    void testSingletonStessaIstanza() {
        OrdineLazyFactory.configure(factory);
        OrdineLazyFactory f1 = OrdineLazyFactory.getInstance();
        OrdineLazyFactory f2 = OrdineLazyFactory.getInstance();

        assertSame(f1, f2);
    }

    @Test
    void testNonConfigurataLanciaEccezione() {
        OrdineLazyFactory.reset();
        assertThrows(IllegalStateException.class, OrdineLazyFactory::getInstance);
    }
}
