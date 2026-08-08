package it.uniroma2.ispw.ciboamico.entity;

import it.uniroma2.ispw.ciboamico.bean.PaymentInfoBean;
import it.uniroma2.ispw.ciboamico.bean.ProdottoBean;
import it.uniroma2.ispw.ciboamico.entity.*;
import it.uniroma2.ispw.ciboamico.pattern.strategy.ScontoImportoFissoStrategy;
import it.uniroma2.ispw.ciboamico.pattern.strategy.ScontoPercentualeStrategy;
import it.uniroma2.ispw.ciboamico.persistence.impl.demo.DemoBuonoDAO;
import it.uniroma2.ispw.ciboamico.persistence.impl.demo.DemoOrdineDAO;
import it.uniroma2.ispw.ciboamico.persistence.impl.demo.DemoProdottoDAO;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/** Test mirati ad alzare la coverage LINE su entity, DAO Demo, bean e strategie. */
class EntityDaosCoverageTest {

    private Utente utente(String email) {
        return new Utente("Nome", email, Utente.hashPassword("pwd"));
    }

    @Test
    void utenteRegistraEVerificaBuoni() {
        Utente u = utente("u@cibo.it");
        assertFalse(u.haUsatoBuono("X1"));
        u.registraBuonoUtilizzato("X1");
        assertTrue(u.haUsatoBuono("X1"));
        assertEquals(1, u.getBuoniUtilizzati().size());
        assertNotNull(u.getPasswordHash());
        assertNotNull(u.getNome());
    }

    @Test
    void utenteRuoliEVerifica() {
        Utente u = utente("r@cibo.it");
        assertFalse(u.haRuolo(RuoloCliente.class));
        u.aggiungiRuolo(new RuoloCliente());
        assertTrue(u.haRuolo(RuoloCliente.class));
        assertNotNull(u.getRuolo(RuoloCliente.class));
        assertNotNull(u.getRuoli());
        assertFalse(u.isVenditoreApprovato());
    }

    @Test
    void buonoValidoEAplicaSconto() throws Exception {
        RuoloVenditore rv = new RuoloVenditore("RM", "rv@cibo.it");
        rv.setStato(StatoVenditoreEnum.APPROVATO);
        BuonoPromozionale buono = new BuonoPromozionale("SCONTO20",
                rv, LocalDate.now().minusDays(1), LocalDate.now().plusDays(10),
                new ScontoPercentualeStrategy(0.20));
        assertTrue(buono.isValido());
        assertTrue(buono.isValido(LocalDate.now()));
        assertEquals(0.8, buono.applicaSconto(1.0), 0.001);
        assertEquals("SCONTO20", buono.getCodice());
        assertNotNull(buono.getVenditore());
        assertNotNull(buono.getDataInizio());
        assertNotNull(buono.getDataScadenza());
        assertNotNull(buono.getStrategiaSconto());
    }

    @Test
    void scontoImportoFisso() {
        ScontoImportoFissoStrategy s = new ScontoImportoFissoStrategy(2.0);
        assertEquals(10.0, s.applicaSconto(12.0), 0.001);
        assertNotNull(s.descrizione());
        assertEquals("FISSO", s.getTipo());
    }

    @Test
    void ordineSubjectUnsubscribe() {
        OrdineSubject os = new OrdineSubject();
        // listener che incrementa
        final int[] count = {0};
        OrdineEventListener l = ordine -> count[0]++;
        os.subscribe(l);
        // c'è solo un listener mock: usiamo unsubscribe su un altro -> no crash
        os.unsubscribe(new VenditoreNotifierStub());
        assertEquals(0, count[0]);
    }

    @Test
    void beanProdottoGetters() {
        ProdottoBean b = new ProdottoBean();
        b.setNome("Miele");
        b.setPrezzo(6.5);
        b.setQuantita(3.0);
        b.setScadenza(LocalDate.now());
        b.setPosizione("Dispensa");
        b.setUnitaMisura("PEZZI");
        assertEquals("Miele", b.getNome());
        assertEquals(6.5, b.getPrezzo());
        assertEquals(3.0, b.getQuantita());
        assertNotNull(b.getScadenza());
        assertEquals("Dispensa", b.getPosizione());
        assertEquals("PEZZI", b.getUnitaMisura());
    }

    @Test
    void beanPagamentoGetters() {
        PaymentInfoBean b = new PaymentInfoBean();
        b.setNumeroCarta("1234");
        b.setIntestatario("M");
        b.setScadenza("12/26");
        b.setCvv("123");
        b.setImportoInCent(650L);
        assertEquals("1234", b.getNumeroCarta());
        assertEquals("M", b.getIntestatario());
        assertEquals("12/26", b.getScadenza());
        assertEquals("123", b.getCvv());
        assertEquals(650L, b.getImportoInCent());
    }

    @Test
    void demoOrdineDaoOperazioni() throws Exception {
        DemoOrdineDAO dao = new DemoOrdineDAO();
        Utente c = utente("c@cibo.it");
        Utente v = utente("v@cibo.it");
        Long id = dao.getNextId();
        Ordine o = new Ordine(id, c, v);
        dao.save(o);
        assertNotNull(dao.findById(id));
        assertEquals(1, dao.findByVenditore("v@cibo.it").size());
        assertEquals(1, dao.findByCompratore("c@cibo.it").size());
    }

    @Test
    void demoBuonoDaoOperazioni() throws Exception {
        DemoBuonoDAO dao = new DemoBuonoDAO();
        // venditore con utente (aggiungiRuolo imposta la back-reference)
        Utente v = utente("fv@cibo.it");
        RuoloVenditore rv = new RuoloVenditore("RM", "fv@cibo.it");
        rv.setStato(StatoVenditoreEnum.APPROVATO);
        v.aggiungiRuolo(rv);
        BuonoPromozionale b = new BuonoPromozionale("B1", rv,
                LocalDate.now().minusDays(2), LocalDate.now().plusDays(5),
                new ScontoPercentualeStrategy(0.10));
        dao.save(b);
        assertNotNull(dao.findByCodice("B1"));
        assertEquals(1, dao.findByVenditoreEmail("fv@cibo.it").size());
        assertTrue(dao.findByVenditoreEmail("altra@cibo.it").isEmpty());
    }

    @Test
    void demoProdottoDaoOperazioni() throws Exception {
        DemoProdottoDAO dao = new DemoProdottoDAO();
        RuoloVenditore rv = new RuoloVenditore("RM", "pv@cibo.it");
        Prodotto p = new Prodotto("Pera", 1.5, 10, LocalDate.now().plusDays(20),
                UnitaEnum.PEZZI, rv);
        dao.save(p);
        assertEquals(1, dao.findAll().size());
        assertNotNull(dao.findByNome("pera"));      // case-insensitive
        assertNull(dao.findByNome("assente"));
        assertNotNull(dao.findById((long) p.getNome().hashCode()));
    }

    /** Listener stub per il test di unsubscribe (non fa nulla). */
    private static class VenditoreNotifierStub implements OrdineEventListener {
        @Override
        public void onStatoCambiato(Ordine ordine) { /* no op */ }
    }
}
