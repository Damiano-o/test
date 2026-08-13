package it.uniroma2.ispw.ciboamico.pattern.strategy;

import it.uniroma2.ispw.ciboamico.bean.OrdineBean;
import it.uniroma2.ispw.ciboamico.bean.UtenteBean;
import it.uniroma2.ispw.ciboamico.control.ApplicaBuonoPromozionaleController;
import it.uniroma2.ispw.ciboamico.entity.BuonoPromozionale;
import it.uniroma2.ispw.ciboamico.entity.Ordine;
import it.uniroma2.ispw.ciboamico.entity.Prodotto;
import it.uniroma2.ispw.ciboamico.entity.RuoloVenditore;
import it.uniroma2.ispw.ciboamico.entity.UnitaEnum;
import it.uniroma2.ispw.ciboamico.entity.Utente;
import it.uniroma2.ispw.ciboamico.entity.VoceOrdine;
import it.uniroma2.ispw.ciboamico.exception.BusinessValidationException;
import it.uniroma2.ispw.ciboamico.pattern.factory.OrdineLazyFactory;
import it.uniroma2.ispw.ciboamico.persistence.factory.DemoDAOFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

// Test del Buono Promozionale: sconto (Strategy), validità temporale, monouso e applicazi...

class BuonoPromozionaleStrategyTest {

    private DemoDAOFactory factory;
    private ApplicaBuonoPromozionaleController controller;

    @BeforeEach
    void setup() {
        factory = new DemoDAOFactory();
        OrdineLazyFactory.reset();
        OrdineLazyFactory.configure(factory);
        controller = new ApplicaBuonoPromozionaleController(factory);
    }

    @AfterEach
    void cleanup() {
        OrdineLazyFactory.reset();
    }

    private Utente venditoreUtente() {
        return new Utente("Marco", "marco@cibo.it", "h");
    }

    private RuoloVenditore venditore() {
        Utente u = venditoreUtente();
        RuoloVenditore rv = new RuoloVenditore("RM", "tel");
        u.aggiungiRuolo(rv);
        return rv;
    }

    private BuonoPromozionale buonoValido(double perc) throws BusinessValidationException {
        return new BuonoPromozionale("BUNDLE" + (int) (perc * 100), venditore(),
                LocalDate.now().minusDays(1), LocalDate.now().plusDays(30),
                new ScontoPercentualeStrategy(perc));
    }

    private Prodotto prodotto(String nome, double prezzo, RuoloVenditore rv) throws BusinessValidationException {
        return new Prodotto(nome, prezzo, 20, LocalDate.now().plusDays(7), UnitaEnum.PEZZI, rv);
    }

    private UtenteBean utenteBean(String email) throws Exception {
        UtenteBean b = new UtenteBean();
        b.setUsername("Mario");
        b.setEmail(email);
        return b;
    }

    // ---------- Strategy ----------
    @Test
    void testScontoPercentuale() {
        double ris = new ScontoPercentualeStrategy(0.20).applicaSconto(10.0);
        assertEquals(8.0, ris, 1e-9);
        assertEquals("PERCENTUALE", new ScontoPercentualeStrategy(0.2).getTipo());
    }

    @Test
    void testScontoPercentualeNonSottoZero() {
        double ris = new ScontoPercentualeStrategy(0.99).applicaSconto(1.0);
        assertTrue(ris >= 0.0);
    }

    @Test
    void testScontoImportoFisso() {
        double ris = new ScontoImportoFissoStrategy(3.0).applicaSconto(10.0);
        assertEquals(7.0, ris, 1e-9);
        // mai sotto zero
        assertEquals(0.0, new ScontoImportoFissoStrategy(50.0).applicaSconto(10.0), 1e-9);
        assertEquals("FISSO", new ScontoImportoFissoStrategy(1.0).getTipo());
    }

    @Test
    void testPercentualeFuoriRangeLancia() {
        assertThrows(IllegalArgumentException.class,
                () -> new ScontoPercentualeStrategy(1.5));
    }

    // ---------- Validità temporale ----------
    @Test
    void testBuonoValidoNelleDate() throws BusinessValidationException {
        BuonoPromozionale b = new BuonoPromozionale("X", venditore(),
                LocalDate.now().minusDays(1), LocalDate.now().plusDays(5),
                new ScontoPercentualeStrategy(0.1));
        assertTrue(b.isValido(LocalDate.now()));
        assertFalse(b.isValido(LocalDate.now().plusDays(10)));
    }

    // ---------- Monouso ----------
    @Test
    void testMonousoRegistratoSuUtente() {
        Utente u = venditoreUtente();
        assertFalse(u.haUsatoBuono("BUONO1"));
        u.registraBuonoUtilizzato("BUONO1");
        assertTrue(u.haUsatoBuono("BUONO1"));
        u.registraBuonoUtilizzato("BUONO1"); // idempotente
        assertEquals(1, u.getBuoniUtilizzati().size());
    }

    // ---------- Applicazione all'Ordine (Information Expert) ----------
    @Test
    void testApplicaBuonoScontaTotaleOrdine() throws Exception {
        RuoloVenditore rv = venditore();
        Prodotto p = prodotto("Miele", 10.0, rv);
        Ordine ordine = OrdineLazyFactory.getInstance().newOrdine(
                new Utente("Mario", "mario@cibo.it", ""),
                new Utente("Marco", "marco@cibo.it", ""));

        ordine.aggiungiVoce(new VoceOrdine(p, 1));
        assertEquals(10.0, ordine.getTotale(), 1e-9);

        ordine.applicaBuono(buonoValido(0.20));
        assertEquals(8.0, ordine.getTotale(), 1e-9);
    }

    @Test
    void testBuonoDiVenditoreErratoLancia() throws Exception {
        // Il buono è del venditore marco@cibo.it
        BuonoPromozionale buono = buonoValido(0.20);
        // Secondo venditore (anna@cibo.it) — il buono non è suo
        Utente altro = new Utente("Anna", "anna@cibo.it", "h");
        RuoloVenditore rvAltro = new RuoloVenditore("RM", "tel");
        altro.aggiungiRuolo(rvAltro);
        Ordine ordine = OrdineLazyFactory.getInstance().newOrdine(
                new Utente("Mario", "mario@cibo.it", ""),
                new Utente("Anna", "anna@cibo.it", ""));

        assertThrows(BusinessValidationException.class, () -> ordine.applicaBuono(buono));
    }

    // ---------- Controller (estensione 4a) ----------
    @Test
    void testControllerApplicaBuonoPercentuale() throws Exception {
        RuoloVenditore rv = venditore();
        factory.getProdottoDAO().save(prodotto("Cestino Fresco", 10.0, rv));
        factory.getBuonoDAO().save(buonoValido(0.20));

        OrdineBean bean = new OrdineBean();
        bean.setNomeProdotto("Cestino Fresco");
        bean.setCompratoreId("mario@cibo.it");

        OrdineBean ris = controller.applicaBuonoPromozionale(
                "BUNDLE20", bean, utenteBean("mario@cibo.it"));

        assertNotNull(ris);
        assertEquals(8.0, ris.getTotale(), 1e-9);
        assertEquals("BUNDLE20", ris.getCodiceBuono());
    }

    @Test
    void testControllerBuonoInesistenteLancia() throws Exception {
        RuoloVenditore rv = venditore();
        factory.getProdottoDAO().save(prodotto("Cestino Fresco", 10.0, rv));

        OrdineBean bean = new OrdineBean();
        bean.setNomeProdotto("Cestino Fresco");

        assertThrows(BusinessValidationException.class,
                () -> controller.applicaBuonoPromozionale("NON_ESISTE", bean, utenteBean("mario@cibo.it")));
    }

    @Test
    void testControllerBuonoScadutoLancia() throws Exception {
        RuoloVenditore rv = venditore();
        factory.getProdottoDAO().save(prodotto("Cestino Fresco", 10.0, rv));
        BuonoPromozionale scaduto = new BuonoPromozionale("SCADUTO", rv,
                LocalDate.now().minusDays(10), LocalDate.now().minusDays(1),
                new ScontoPercentualeStrategy(0.20));
        factory.getBuonoDAO().save(scaduto);

        OrdineBean bean = new OrdineBean();
        bean.setNomeProdotto("Cestino Fresco");

        assertThrows(BusinessValidationException.class,
                () -> controller.applicaBuonoPromozionale("SCADUTO", bean, utenteBean("mario@cibo.it")));
    }
}
