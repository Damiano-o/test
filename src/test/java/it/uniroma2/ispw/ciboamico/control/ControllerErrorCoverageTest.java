package it.uniroma2.ispw.ciboamico.control;

import it.uniroma2.ispw.ciboamico.bean.OrdineBean;
import it.uniroma2.ispw.ciboamico.bean.ProdottoBean;
import it.uniroma2.ispw.ciboamico.bean.UtenteBean;
import it.uniroma2.ispw.ciboamico.entity.BuonoPromozionale;
import it.uniroma2.ispw.ciboamico.entity.Ordine;
import it.uniroma2.ispw.ciboamico.entity.RuoloVenditore;
import it.uniroma2.ispw.ciboamico.entity.StatoVenditoreEnum;
import it.uniroma2.ispw.ciboamico.entity.Utente;
import it.uniroma2.ispw.ciboamico.exception.BusinessValidationException;
import it.uniroma2.ispw.ciboamico.pattern.strategy.ScontoPercentualeStrategy;
import it.uniroma2.ispw.ciboamico.persistence.factory.DemoDAOFactory;
import it.uniroma2.ispw.ciboamico.pattern.factory.OrdineLazyFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** Test dei rami di errore del controller applicativo per alzare la coverage LINE su UC-04. */
class ControllerErrorCoverageTest {

    private DemoDAOFactory factory;
    private OrdinaProdottoController controller;
    private ApplicaBuonoPromozionaleController buonoController;

    @BeforeEach
    void setup() throws Exception {
        factory = new DemoDAOFactory();
        factory.seedDemoData();
        OrdineLazyFactory.configure(factory);
        controller = new OrdinaProdottoController(factory);
        buonoController = new ApplicaBuonoPromozionaleController(factory);
    }

    private UtenteBean utente() throws Exception {
        UtenteBean u = new UtenteBean();
        u.setUsername("mario");
        u.setEmail("mario@cibo.it");
        u.setRuoloAttivo("CLIENTE");
        return u;
    }

    @Test
    void getProdottiDisponibiliNonVuoto() throws Exception {
        List<ProdottoBean> p = controller.getProdottiDisponibili();
        assertFalse(p.isEmpty());
    }

    @Test
    void buonoCodiceMancante() {
        assertThrows(BusinessValidationException.class,
                () -> buonoController.applicaBuonoPromozionale(null, new OrdineBean(), utente()));
    }

    @Test
    void buonoNonValido() throws Exception {
        OrdineBean bean = new OrdineBean();
        bean.setNomeProdotto("Miele locale");
        assertThrows(BusinessValidationException.class,
                () -> buonoController.applicaBuonoPromozionale("INESISTENTE", bean, utente()));
    }

    @Test
    void buonoScaduto() throws Exception {
        // crea venditore e prodotto e un buono scaduto
        RuoloVenditore rv = new RuoloVenditore("RM", "marco@cibo.it");
        rv.setStato(StatoVenditoreEnum.APPROVATO);
        // buono già scaduto
        BuonoPromozionale scaduto = new BuonoPromozionale("SCADUTO1", rv,
                LocalDate.now().minusDays(10), LocalDate.now().minusDays(1),
                new ScontoPercentualeStrategy(0.10));
        factory.getBuonoDAO().save(scaduto);
        OrdineBean bean = new OrdineBean();
        bean.setNomeProdotto("Miele locale");
        assertThrows(BusinessValidationException.class,
                () -> buonoController.applicaBuonoPromozionale("SCADUTO1", bean, utente()));
    }

    @Test
    void buonoVenditoreErrato() throws Exception {
        // buono di un venditore diverso da quello del prodotto "Miele locale" (marco@cibo.it)
        RuoloVenditore rvAltro = new RuoloVenditore("RM", "altro@cibo.it");
        rvAltro.setStato(StatoVenditoreEnum.APPROVATO);
        BuonoPromozionale buono = new BuonoPromozionale("ALTRU", rvAltro,
                LocalDate.now().minusDays(1), LocalDate.now().plusDays(10),
                new ScontoPercentualeStrategy(0.10));
        factory.getBuonoDAO().save(buono);
        OrdineBean bean = new OrdineBean();
        bean.setNomeProdotto("Miele locale");
        assertThrows(BusinessValidationException.class,
                () -> buonoController.applicaBuonoPromozionale("ALTRU", bean, utente()));
    }

    @Test
    void buonoSuccesso() throws Exception {
        // buono valido del venditore del prodotto "Miele locale" (marco@cibo.it)
        OrdineBean bean = new OrdineBean();
        bean.setNomeProdotto("Miele locale");
        OrdineBean ris = buonoController.applicaBuonoPromozionale("SALUTI20", bean, utente());
        assertEquals("SALUTI20", ris.getCodiceBuono());
        assertNotNull(ris.getTotale());
    }

    @Test
    void buonoGiaUsato() throws Exception {
        // monouso: dopo il primo riscatto da parte di mario@cibo.it, il secondo
        // utilizzo dello stesso codice deve essere rifiutato (BR monouso).
        OrdineBean bean = new OrdineBean();
        bean.setNomeProdotto("Miele locale");

        // Primo utilizzo: registra il buono come riscattato dall'utente.
        OrdineBean primo = buonoController.applicaBuonoPromozionale("SALUTI20", bean, utente());
        assertEquals("SALUTI20", primo.getCodiceBuono());

        // Secondo utilizzo dello stesso codice: deve lanciare BusinessValidationException.
        assertThrows(BusinessValidationException.class,
                () -> buonoController.applicaBuonoPromozionale("SALUTI20", bean, utente()));
    }
}
