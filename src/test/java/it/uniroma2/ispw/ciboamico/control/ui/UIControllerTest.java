package it.uniroma2.ispw.ciboamico.control.ui;

import it.uniroma2.ispw.ciboamico.bean.OrdineBean;
import it.uniroma2.ispw.ciboamico.bean.PaymentInfoBean;
import it.uniroma2.ispw.ciboamico.bean.ProdottoBean;
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
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test dei controller grafici di UC-04 (Marketplace/Pagamento): verifica che
 * coordino l'interazione View→controller applicativo, indipendentemente dalla
 * UI (testabili in isolamento).
 */
class UIControllerTest {

    private DemoDAOFactory factory;
    private MarketplaceUIController marketplace;
    private PaymentUIController payment;
    private UtenteBean utente;

    @BeforeEach
    void setup() throws Exception {
        factory = new DemoDAOFactory();
        OrdineLazyFactory.reset();
        OrdineLazyFactory.configure(factory);
        utente = new UtenteBean();
        utente.setUsername("Mario");
        utente.setEmail("mario@cibo.it");
        marketplace = new MarketplaceUIController((DAOFactory) factory, utente);
        payment = new PaymentUIController((DAOFactory) factory);
        SessionManager.getInstance().setLoggedUser(utente);
    }

    @AfterEach
    void cleanup() {
        OrdineLazyFactory.reset();
        SessionManager.getInstance().logout();
        SessionManager.getInstance().setOrdineInCorso(null);
    }

    private void salvaProdotto(String nome, double prezzo) throws Exception {
        Utente v = new Utente("Marco", "marco@cibo.it", "h");
        RuoloVenditore rv = new RuoloVenditore("RM", "tel");
        v.aggiungiRuolo(rv);
        factory.getProdottoDAO().save(
                new Prodotto(nome, prezzo, 10, LocalDate.now().plusDays(7), UnitaEnum.PEZZI, rv));
    }

    // ===== MarketplaceUIController =====

    @Test
    void catalogoProdottiRestituisceLista() throws Exception {
        List<ProdottoBean> prodotti = marketplace.catalogoProdotti();
        assertNotNull(prodotti);
        assertEquals(factory.getProdottoDAO().findAll().size(), prodotti.size());
    }

    @Test
    void ordinaProdottoValidoAvviaCheckout() throws Exception {
        salvaProdotto("Caffè", 4.50);
        OrdineBean inCorso = marketplace.ordinaProdotto("Caffè");
        assertEquals("Caffè", inCorso.getNomeProdotto());
        assertEquals(4.50, inCorso.getTotale(), 1e-9);
    }

    @Test
    void ordinaProdottoVuotoLanciaBusiness() {
        assertThrows(BusinessValidationException.class,
                () -> marketplace.ordinaProdotto(""));
    }

    @Test
    void applicaBuonoValidoRestituisceSconto() throws Exception {
        Utente v = new Utente("Marco", "marco@cibo.it", "h");
        RuoloVenditore rv = new RuoloVenditore("RM", "tel");
        v.aggiungiRuolo(rv);
        factory.getProdottoDAO().save(new Prodotto("Miele", 10.0, 10,
                LocalDate.now().plusDays(7), UnitaEnum.PEZZI, rv));
        factory.getBuonoDAO().save(new BuonoPromozionale("BUNDLE20", rv,
                LocalDate.now().minusDays(1), LocalDate.now().plusDays(30),
                new ScontoPercentualeStrategy(0.20)));

        OrdineBean ris = marketplace.applicaBuono("BUNDLE20", "Miele");
        assertEquals("BUNDLE20", ris.getCodiceBuono());
        assertEquals(8.0, ris.getTotale(), 1e-9);
    }

    @Test
    void formattaEsitoBuono() {
        OrdineBean ris = new OrdineBean();
        ris.setCodiceBuono("BUNDLE20");
        ris.setTotale(8.0);
        assertEquals("Buono \"BUNDLE20\" applicato ✓ — totale 8,00 EUR",
                MarketplaceUIController.formattaEsitoBuono(ris));
    }

    // ===== PaymentUIController =====

    @Test
    void pagaValidoAutorizzaECompleta() throws Exception {
        salvaProdotto("Caffè", 4.50);
        OrdineBean ordine = marketplace.ordinaProdotto("Caffè");
        OrdineBean esito = payment.paga(ordine, utente,
                "1111222233334444", "Mario", "12/29", "123");
        assertNotNull(esito.getIdOrdine());
        assertEquals("CREATED", esito.getStato());
    }

    @Test
    void pagaNessunOrdineLanciaBusiness() {
        assertThrows(BusinessValidationException.class,
                () -> payment.paga(null, utente, "1111222233334444", "Mario", "12/29", "123"));
    }

    @Test
    void formattaEsitoPagamento() {
        OrdineBean ris = new OrdineBean();
        ris.setStato("CREATED");
        ris.setTotale(4.5);
        assertEquals("Pagamento riuscito ✓ — ordine CREATED, totale 4,50 EUR",
                PaymentUIController.formattaEsitoPagamento(ris));
    }
}
