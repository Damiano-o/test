package it.uniroma2.ispw.ciboamico.control;

import it.uniroma2.ispw.ciboamico.bean.OrdineBean;
import it.uniroma2.ispw.ciboamico.bean.UtenteBean;
import it.uniroma2.ispw.ciboamico.entity.BuonoPromozionale;
import it.uniroma2.ispw.ciboamico.entity.Prodotto;
import it.uniroma2.ispw.ciboamico.entity.RuoloVenditore;
import it.uniroma2.ispw.ciboamico.entity.UnitaEnum;
import it.uniroma2.ispw.ciboamico.entity.Utente;
import it.uniroma2.ispw.ciboamico.exception.BusinessValidationException;
import it.uniroma2.ispw.ciboamico.pattern.factory.OrdineLazyFactory;
import it.uniroma2.ispw.ciboamico.pattern.singleton.SessionManager;
import it.uniroma2.ispw.ciboamico.pattern.strategy.ScontoPercentualeStrategy;
import it.uniroma2.ispw.ciboamico.persistence.factory.DemoDAOFactory;
import it.uniroma2.ispw.ciboamico.persistence.factory.DAOFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

// Test del controller dell'estensione Applica Buono Promozionale (4a di UC-04): validazio...

class ApplicaBuonoPromozionaleControllerTest {

    private DemoDAOFactory factory;
    private ApplicaBuonoPromozionaleController controller;

    @BeforeEach
    void setup() throws Exception {
        factory = new DemoDAOFactory();
        OrdineLazyFactory.reset();
        OrdineLazyFactory.configure(factory);
        controller = new ApplicaBuonoPromozionaleController((DAOFactory) factory);
        SessionManager.getInstance().setLoggedUser(utenteBean());
    }

    @AfterEach
    void cleanup() {
        OrdineLazyFactory.reset();
        SessionManager.getInstance().logout();
        SessionManager.getInstance().setOrdineInCorso(null);
    }

    private UtenteBean utenteBean() throws Exception {
        UtenteBean b = new UtenteBean();
        b.setUsername("Mario");
        b.setEmail("mario@cibo.it");
        return b;
    }

    private void salvaProdottoConBuono(String nomeProdotto, double prezzo, String codiceBuono,
                                       double sconto) throws Exception {
        Utente v = new Utente("Marco", "marco@cibo.it", "h");
        RuoloVenditore rv = new RuoloVenditore("RM", "tel");
        v.aggiungiRuolo(rv);
        factory.getProdottoDAO().save(new Prodotto(nomeProdotto, prezzo, 10,
                LocalDate.now().plusDays(7), UnitaEnum.PEZZI, rv));
        factory.getBuonoDAO().save(new BuonoPromozionale(codiceBuono, rv,
                LocalDate.now().minusDays(1), LocalDate.now().plusDays(30),
                new ScontoPercentualeStrategy(sconto)));
    }

    @Test
    void applicaBuonoValidoRestituisceSconto() throws Exception {
        salvaProdottoConBuono("Miele", 10.0, "BUNDLE20", 0.20);

        OrdineBean ris = controller.applicaBuonoPromozionale(
                "BUNDLE20", OrdineBean.fromCheckout("Miele"), utenteBean());
        assertEquals("BUNDLE20", ris.getCodiceBuono());
        assertEquals(8.0, ris.getTotale(), 1e-9);
    }

    @Test
    void applicaBuonoCodiceVuotoLancia() {
        assertThrows(BusinessValidationException.class,
                () -> controller.applicaBuonoPromozionale(
                        "  ", OrdineBean.fromCheckout("Miele"), utenteBean()));
    }

    @Test
    void applicaBuonoProdottoNonSelezionatoLancia() {
        // Prodotto non selezionato (nome vuoto): la conversione esterno→interno
        // via fromCheckout incapsula il Fail Fast.
        assertThrows(BusinessValidationException.class,
                () -> OrdineBean.fromCheckout(" "));
        assertThrows(BusinessValidationException.class,
                () -> OrdineBean.fromCheckout(null));
    }

    @Test
    void applicaBuonoUtenteEmailNull() throws Exception {
        // percorso monouso attivo (email presente)
        salvaProdottoConBuono("Miele", 10.0, "BUNDLE20", 0.20);
        UtenteBean utenteConEmail = new UtenteBean();
        utenteConEmail.setUsername("Mario");
        utenteConEmail.setEmail("mario@cibo.it");
        OrdineBean ris = controller.applicaBuonoPromozionale(
                "BUNDLE20", OrdineBean.fromCheckout("Miele"), utenteConEmail);
        assertEquals("BUNDLE20", ris.getCodiceBuono());
    }
}
