package it.uniroma2.ispw.ciboamico.control;

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
import it.uniroma2.ispw.ciboamico.pattern.strategy.ScontoPercentualeStrategy;
import it.uniroma2.ispw.ciboamico.pattern.singleton.SessionManager;
import it.uniroma2.ispw.ciboamico.persistence.factory.DemoDAOFactory;
import it.uniroma2.ispw.ciboamico.persistence.factory.DAOFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test del controller dell'estensione Pagamento (passo 6 / estensione 6a di
 * UC-04): autorizzazione addebito + submit ordine.
 */
class PagamentoControllerTest {

    private DemoDAOFactory factory;
    private PagamentoController controller;

    @BeforeEach
    void setup() throws Exception {
        factory = new DemoDAOFactory();
        OrdineLazyFactory.reset();
        OrdineLazyFactory.configure(factory);
        controller = new PagamentoController((DAOFactory) factory);
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

    // ================= submitOrdine =================

    @Test
    void testSubmitOrdineUtenteNull() {
        OrdineBean bean = new OrdineBean();
        assertThrows(IllegalStateException.class,
                () -> controller.submitOrdine(bean, null));
    }

    @Test
    void testSubmitOrdineProdottoNonTrovato() throws Exception {
        OrdineBean bean = new OrdineBean();
        bean.setNomeProdotto("ProdottoInesistente");
        assertThrows(IllegalStateException.class, () -> controller.submitOrdine(bean, utenteBean()));
    }

    @Test
    void testSubmitOrdineVenditoreDalProdotto() throws Exception {
        Utente utenteVenditore = new Utente("Marco", "marco@cibo.it", "h");
        RuoloVenditore rv = new RuoloVenditore("RM", "tel");
        utenteVenditore.aggiungiRuolo(rv);
        Prodotto prodotto = new Prodotto("Pomodori", 2.0, 50,
                LocalDate.now().plusDays(7), UnitaEnum.GRAMMI, rv);
        factory.getProdottoDAO().save(prodotto);

        OrdineBean bean = new OrdineBean();
        bean.setNomeProdotto("Pomodori");
        bean.setCompratoreId("mario@cibo.it");

        OrdineBean risultato = controller.submitOrdine(bean, utenteBean());
        assertNotNull(risultato);
        assertNotNull(risultato.getIdOrdine());
        assertEquals("CREATED", risultato.getStato());
        assertEquals(2.0, risultato.getTotale(), 1e-9);
    }

    @Test
    void testSubmitOrdineRiduceDisponibilita() throws Exception {
        Utente utenteVenditore = new Utente("Marco", "marco@cibo.it", "h");
        RuoloVenditore rv = new RuoloVenditore("RM", "tel");
        utenteVenditore.aggiungiRuolo(rv);
        Prodotto prodotto = new Prodotto("Mele", 1.5, 3,
                LocalDate.now().plusDays(7), UnitaEnum.PEZZI, rv);
        factory.getProdottoDAO().save(prodotto);

        OrdineBean bean = new OrdineBean();
        bean.setNomeProdotto("Mele");
        bean.setCompratoreId("mario@cibo.it");

        controller.submitOrdine(bean, utenteBean());
        assertEquals(2, prodotto.getQuantitaDisponibile());
    }

    @Test
    void testSubmitOrdineApplicaBuonoDalCheckout() throws Exception {
        // Regressione: il buono applicato al checkout deve sopravvivere nel
        // submit dell'ordine (lo sconto non va perso al pagamento).
        Utente utenteVenditore = new Utente("Marco", "marco@cibo.it", "h");
        RuoloVenditore rv = new RuoloVenditore("RM", "tel");
        utenteVenditore.aggiungiRuolo(rv);
        factory.getProdottoDAO().save(
                new Prodotto("Mele", 1.5, 10,
                        LocalDate.now().plusDays(7), UnitaEnum.PEZZI, rv));
        factory.getBuonoDAO().save(new BuonoPromozionale("SALUTI20", rv,
                LocalDate.now().minusDays(1), LocalDate.now().plusDays(30),
                new ScontoPercentualeStrategy(0.20)));

        OrdineBean bean = new OrdineBean();
        bean.setNomeProdotto("Mele");
        bean.setCodiceBuono("SALUTI20");

        OrdineBean risultato = controller.submitOrdine(bean, utenteBean());
        // 1.50 - 20% = 1.20 (lo sconto NON deve andare perso)
        assertEquals(1.20, risultato.getTotale(), 1e-9);
        assertEquals("CREATED", risultato.getStato());
    }

    @Test
    void testAcquistoQuantitaEccessivaLanciaEccezione() throws Exception {
        Utente utenteVenditore = new Utente("Marco", "marco@cibo.it", "h");
        RuoloVenditore rv = new RuoloVenditore("RM", "tel");
        utenteVenditore.aggiungiRuolo(rv);
        Prodotto prodotto = new Prodotto("Uova", 3.0, 1,
                LocalDate.now().plusDays(7), UnitaEnum.PEZZI, rv);
        factory.getProdottoDAO().save(prodotto);

        OrdineBean bean = new OrdineBean();
        bean.setNomeProdotto("Uova");
        controller.submitOrdine(bean, utenteBean());

        OrdineBean bean2 = new OrdineBean();
        bean2.setNomeProdotto("Uova");
        assertThrows(BusinessValidationException.class,
                () -> controller.submitOrdine(bean2, utenteBean()));
    }

    // ================= processaPagamento =================

    @Test
    void testProcessaPagamentoSuccessoCreaOrdine() throws Exception {
        salvaProdotto("Caffè", 4.50);
        OrdineBean bean = new OrdineBean();
        bean.setNomeProdotto("Caffè");
        PaymentInfoBean payment = new PaymentInfoBean();
        payment.setNumeroCarta("1111222233334444");
        payment.setCvv("123");
        payment.setImportoInCent(450L);

        OrdineBean risultato = controller.processaPagamento(bean, utenteBean(), payment);
        assertNotNull(risultato.getIdOrdine());
        assertEquals("CREATED", risultato.getStato());
    }

    @Test
    void testProcessaPagamentoRifiutatoLanciaBusinessValidation() throws Exception {
        salvaProdotto("Oro", 600.0);
        OrdineBean bean = new OrdineBean();
        bean.setNomeProdotto("Oro");
        PaymentInfoBean payment = new PaymentInfoBean();
        payment.setNumeroCarta("1111222233334444");
        payment.setCvv("123");
        payment.setImportoInCent(60_000L);

        assertThrows(BusinessValidationException.class,
                () -> controller.processaPagamento(bean, utenteBean(), payment));
    }

    @Test
    void processaPagamentoConDatiCartaValidi() throws Exception {
        salvaProdotto("Caffè", 4.50);
        OrdineBean inCorso = new OrdineBean();
        inCorso.setNomeProdotto("Caffè");
        inCorso.setTotale(4.50);
        SessionManager.getInstance().setOrdineInCorso(inCorso);

        PaymentInfoBean payment = new PaymentInfoBean();
        payment.setNumeroCarta("1111222233334444");
        payment.setIntestatario("Mario");
        payment.setScadenza("12/29");
        payment.setCvv("123");
        payment.setImportoInCent(450L);

        OrdineBean esito = controller.processaPagamento(
                inCorso, utenteBean(), payment);
        assertNotNull(esito.getIdOrdine());
        assertEquals("CREATED", esito.getStato());
    }

    @Test
    void processaPagamentoCvvErratoLancia() throws Exception {
        salvaProdotto("Caffè", 4.50);
        OrdineBean inCorso = new OrdineBean();
        inCorso.setNomeProdotto("Caffè");
        inCorso.setTotale(4.50);
        SessionManager.getInstance().setOrdineInCorso(inCorso);

        assertThrows(BusinessValidationException.class, () -> {
            PaymentInfoBean payment = new PaymentInfoBean();
            payment.setNumeroCarta("1111222233334444");
            payment.setIntestatario("Mario");
            payment.setScadenza("12/29");
            payment.setCvv("12"); // CVV non valido -> bean non costruibile
            controller.processaPagamento(inCorso, utenteBean(), payment);
        });
    }

    @Test
    void processaPagamentoNessunOrdineLanciaBusiness() throws Exception {
        PaymentInfoBean payment = new PaymentInfoBean();
        payment.setNumeroCarta("1111222233334444");
        payment.setIntestatario("Mario");
        payment.setScadenza("12/29");
        payment.setCvv("123");
        payment.setImportoInCent(450L);
        assertThrows(BusinessValidationException.class,
                () -> controller.processaPagamento(null, utenteBean(), payment));
    }

    @Test
    void processaPagamentoBeanPaymentNull() throws Exception {
        salvaProdotto("Caffè", 4.50);
        OrdineBean bean = new OrdineBean();
        bean.setNomeProdotto("Caffè");
        assertThrows(BusinessValidationException.class,
                () -> controller.processaPagamento(bean, utenteBean(), null));
    }

    @Test
    void processaPagamentoBeanImportoNonPositivo() throws Exception {
        salvaProdotto("Caffè", 4.50);
        OrdineBean bean = new OrdineBean();
        bean.setNomeProdotto("Caffè");
        // importo default 0 -> non positivo -> rifiutato
        PaymentInfoBean payment = new PaymentInfoBean();
        assertThrows(BusinessValidationException.class,
                () -> controller.processaPagamento(bean, utenteBean(), payment));
    }
}
