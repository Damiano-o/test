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

/** Test dei rami di errore del controller per alzare la coverage LINE su UC-04. */
class ControllerErrorCoverageTest {

    private DemoDAOFactory factory;
    private OrdinaProdottoController controller;

    @BeforeEach
    void setup() {
        factory = new DemoDAOFactory();
        factory.seedDemoData();
        OrdineLazyFactory.configure(factory);
        controller = new OrdinaProdottoController(factory);
    }

    private UtenteBean utente() {
        UtenteBean u = new UtenteBean();
        u.setUsername("mario");
        u.setEmail("mario@cibo.it");
        u.setRuoloAttivo("RuoloCliente");
        return u;
    }

    @Test
    void getProdottiDisponibiliNonVuoto() {
        List<ProdottoBean> p = controller.getProdottiDisponibili();
        assertFalse(p.isEmpty());
    }

    @Test
    void buonoCodiceMancante() {
        assertThrows(BusinessValidationException.class,
                () -> controller.applicaBuonoPromozionale(null, new OrdineBean(), utente()));
    }

    @Test
    void buonoNonValido() {
        OrdineBean bean = new OrdineBean();
        bean.setNomeProdotto("Miele locale");
        assertThrows(BusinessValidationException.class,
                () -> controller.applicaBuonoPromozionale("INESISTENTE", bean, utente()));
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
                () -> controller.applicaBuonoPromozionale("SCADUTO1", bean, utente()));
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
                () -> controller.applicaBuonoPromozionale("ALTRU", bean, utente()));
    }

    @Test
    void buonoSuccesso() throws Exception {
        // buono valido del venditore del prodotto "Miele locale" (marco@cibo.it)
        OrdineBean bean = new OrdineBean();
        bean.setNomeProdotto("Miele locale");
        OrdineBean ris = controller.applicaBuonoPromozionale("SALUTI20", bean, utente());
        assertEquals("SALUTI20", ris.getCodiceBuono());
        assertNotNull(ris.getTotale());
    }
}
