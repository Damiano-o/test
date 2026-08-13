package it.uniroma2.ispw.ciboamico.control;

import it.uniroma2.ispw.ciboamico.bean.OrdineBean;
import it.uniroma2.ispw.ciboamico.bean.UtenteBean;
import it.uniroma2.ispw.ciboamico.entity.Prodotto;
import it.uniroma2.ispw.ciboamico.entity.RuoloVenditore;
import it.uniroma2.ispw.ciboamico.entity.UnitaEnum;
import it.uniroma2.ispw.ciboamico.entity.Utente;
import it.uniroma2.ispw.ciboamico.exception.BusinessValidationException;
import it.uniroma2.ispw.ciboamico.pattern.factory.OrdineLazyFactory;
import it.uniroma2.ispw.ciboamico.pattern.singleton.SessionManager;
import it.uniroma2.ispw.ciboamico.persistence.factory.DemoDAOFactory;
import it.uniroma2.ispw.ciboamico.persistence.factory.DAOFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

// Test del controller principale di UC-04 Ordina Prodotto: catalogo e avvio del checkout

class OrdinaProdottoControllerTest {

    private DemoDAOFactory factory;
    private OrdinaProdottoController controller;

    @BeforeEach
    void setup() throws Exception {
        factory = new DemoDAOFactory();
        OrdineLazyFactory.reset();
        OrdineLazyFactory.configure(factory);
        controller = new OrdinaProdottoController((DAOFactory) factory);
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

    private void salvaProdotto(String nome, double prezzo) throws Exception {
        Utente v = new Utente("Marco", "marco@cibo.it", "h");
        RuoloVenditore rv = new RuoloVenditore("RM", "tel");
        v.aggiungiRuolo(rv);
        factory.getProdottoDAO().save(
                new Prodotto(nome, prezzo, 10, LocalDate.now().plusDays(7), UnitaEnum.PEZZI, rv));
    }

    // ================= Operazioni di schermata =================

    @Test
    void avviaCheckoutCreaOrdineInCorso() throws Exception {
        salvaProdotto("Caffè", 4.50);
        OrdineBean inCorso = controller.avviaCheckout(OrdineBean.fromCheckout("Caffè"));
        assertEquals("Caffè", inCorso.getNomeProdotto());
        assertEquals(4.50, inCorso.getTotale(), 1e-9);
        // Il controller applicativo è state-less: la scrittura in SessionManager
        // è responsabilità del Facade, non qui.
    }

    @Test
    void avviaCheckoutProdottoInesistenteLancia() {
        assertThrows(BusinessValidationException.class,
                () -> controller.avviaCheckout(OrdineBean.fromCheckout("Assente")));
    }

    @Test
    void avviaCheckoutNomeVuotoLancia() {
        assertThrows(BusinessValidationException.class,
                () -> OrdineBean.fromCheckout("  "));
    }
}
