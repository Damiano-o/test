package it.uniroma2.ispw.ciboamico.control.facade;

import it.uniroma2.ispw.ciboamico.bean.OrdineBean;
import it.uniroma2.ispw.ciboamico.bean.PaymentInfoBean;
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

// Test della Facade di checkout UC-04: la boundary usa un unico punto di ingresso (catalo...

class OrdinaProdottoFacadeTest {

    private DemoDAOFactory factory;
    private OrdinaProdottoFacade facade;

    @BeforeEach
    void setup() throws Exception {
        factory = new DemoDAOFactory();
        OrdineLazyFactory.reset();
        OrdineLazyFactory.configure(factory);
        facade = new OrdinaProdottoFacade((DAOFactory) factory);
        SessionManager.getInstance().setLoggedUser(utenteBean());
    }

    @AfterEach
    void cleanup() {
        OrdineLazyFactory.reset();
        SessionManager.getInstance().logout();
    }

    private UtenteBean utenteBean() throws Exception {
        UtenteBean b = new UtenteBean();
        b.setUsername("Mario");
        b.setEmail("mario@cibo.it");
        return b;
    }

    private void salvaVenditoreConProdotto(String nome, double prezzo) throws Exception {
        Utente utenteVenditore = new Utente("Marco", "marco@cibo.it", "h");
        RuoloVenditore rv = new RuoloVenditore("RM", "tel");
        utenteVenditore.aggiungiRuolo(rv);
        Prodotto prodotto = new Prodotto(nome, prezzo, 10,
                LocalDate.now().plusDays(7), UnitaEnum.PEZZI, rv);
        factory.getProdottoDAO().save(prodotto);
    }

    private void checkoutSenzaBuono(UtenteBean utente) throws Exception {
        OrdineBean inCorso = facade.avviaCheckout(OrdineBean.fromCheckout("Caffè"));
        facade.processaPagamento(
                SessionManager.getInstance().getOrdineInCorso(), utente,
                payment(Math.round(inCorso.getTotale() * 100)));
    }

    private static PaymentInfoBean payment(long importoInCent) throws Exception {
        PaymentInfoBean payment = new PaymentInfoBean();
        payment.setNumeroCarta("0000000000000000");
        payment.setIntestatario("Mario");
        payment.setScadenza("12/29");
        payment.setCvv("000");
        payment.setImportoInCent(importoInCent);
        return payment;
    }

    @Test
    void checkoutSenzaBuonoCreaOrdine() throws Exception {
        salvaVenditoreConProdotto("Caffè", 4.50);
        OrdineBean inCorso = facade.avviaCheckout(OrdineBean.fromCheckout("Caffè"));
        assertEquals("Caffè", inCorso.getNomeProdotto());
        assertEquals(4.50, inCorso.getTotale());
        OrdineBean esito = facade.processaPagamento(
                SessionManager.getInstance().getOrdineInCorso(), utenteBean(), payment(450L));
        assertNotNull(esito.getIdOrdine());
        assertEquals("CREATED", esito.getStato());
    }

    @Test
    void checkoutConBuonoValidoApplicaSconto() throws Exception {
        // Venditore e buono associato (percentuale 20%, valido 30 giorni)
        Utente utenteVenditore = new Utente("Marco", "marco@cibo.it", "h");
        RuoloVenditore rv = new RuoloVenditore("RM", "tel");
        utenteVenditore.aggiungiRuolo(rv);
        Prodotto prodotto = new Prodotto("Pomodori", 10.0, 10,
                LocalDate.now().plusDays(7), UnitaEnum.PEZZI, rv);
        factory.getProdottoDAO().save(prodotto);
        BuonoPromozionale buono = new BuonoPromozionale("BUNDLE20", rv,
                LocalDate.now().minusDays(1), LocalDate.now().plusDays(30),
                new ScontoPercentualeStrategy(0.20));
        factory.getBuonoDAO().save(buono);

        facade.avviaCheckout(OrdineBean.fromCheckout("Pomodori"));
        OrdineBean scontato = facade.applicaBuono(
                "BUNDLE20", OrdineBean.fromCheckout("Pomodori"), utenteBean());
        assertNotNull(scontato.getCodiceBuono());
        assertEquals("BUNDLE20", scontato.getCodiceBuono());
    }

    @Test
    void avvioCheckoutSenzaProdottoLanciaBusinessValidation() {
        assertThrows(BusinessValidationException.class,
                () -> facade.avviaCheckout(OrdineBean.fromCheckout("")));
    }

    @Test
    void checkoutPagamentoRifiutatoLanciaBusinessValidation() throws Exception {
        salvaVenditoreConProdotto("Oro", 600.0);
        OrdineBean inCorso = facade.avviaCheckout(OrdineBean.fromCheckout("Oro"));
        // Soglia dello stub di pagamento: ~500 EUR → 600 EUR rifiutato.
        PaymentInfoBean payment = payment(Math.round(inCorso.getTotale() * 100));
        assertThrows(BusinessValidationException.class, () ->
                facade.processaPagamento(
                        SessionManager.getInstance().getOrdineInCorso(), utenteBean(), payment));
    }
}