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
import it.uniroma2.ispw.ciboamico.pattern.singleton.SessionManager;
import it.uniroma2.ispw.ciboamico.pattern.strategy.ScontoPercentualeStrategy;
import it.uniroma2.ispw.ciboamico.persistence.factory.DemoDAOFactory;
import it.uniroma2.ispw.ciboamico.persistence.factory.DAOFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test del controller di UC-04 Ordina Prodotto: verifica la logica di business
 * (submit, pagamento, buono) e le operazioni di schermata (checkout) esposte
 * alle boundary.
 */
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

    private Utente venditore() {
        Utente v = new Utente("Marco", "marco@cibo.it", "h");
        RuoloVenditore rv = new RuoloVenditore("RM", "tel");
        v.aggiungiRuolo(rv);
        return v;
    }

    private void salvaProdotto(String nome, double prezzo) throws Exception {
        Utente v = venditore();
        factory.getProdottoDAO().save(
                new Prodotto(nome, prezzo, 10, LocalDate.now().plusDays(7), UnitaEnum.PEZZI,
                        (RuoloVenditore) v.getRuoli().get(0)));
    }

    // ================= Logica di business =================

    @Test
    void testSubmitOrdineUtenteNull() throws Exception {
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
        Utente utenteVenditore = venditore();
        RuoloVenditore rv = (RuoloVenditore) utenteVenditore.getRuoli().get(0);
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
    void testAcquistoQuantitaEccessivaLanciaEccezione() throws Exception {
        Utente utenteVenditore = venditore();
        RuoloVenditore rv = (RuoloVenditore) utenteVenditore.getRuoli().get(0);
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

    // ================= Operazioni di schermata =================

    @Test
    void avviaCheckoutCreaOrdineInCorso() throws Exception {
        salvaProdotto("Caffè", 4.50);
        OrdineBean inCorso = controller.avviaCheckout("Caffè");
        assertEquals("Caffè", inCorso.getNomeProdotto());
        assertEquals(4.50, inCorso.getTotale(), 1e-9);
        assertSame(inCorso, SessionManager.getInstance().getOrdineInCorso());
    }

    @Test
    void avviaCheckoutProdottoInesistenteLancia() {
        assertThrows(BusinessValidationException.class, () -> controller.avviaCheckout("Assente"));
    }

    @Test
    void avviaCheckoutNomeVuotoLancia() {
        assertThrows(BusinessValidationException.class, () -> controller.avviaCheckout("  "));
    }

    @Test
    void applicaBuonoValidoRestituisceSconto() throws Exception {
        Utente v = venditore();
        RuoloVenditore rv = (RuoloVenditore) v.getRuoli().get(0);
        factory.getProdottoDAO().save(new Prodotto("Miele", 10.0, 10,
                LocalDate.now().plusDays(7), UnitaEnum.PEZZI, rv));
        factory.getBuonoDAO().save(new BuonoPromozionale("BUNDLE20", rv,
                LocalDate.now().minusDays(1), LocalDate.now().plusDays(30),
                new ScontoPercentualeStrategy(0.20)));

        OrdineBean ris = controller.applicaBuono("BUNDLE20", "Miele", utenteBean());
        assertEquals("BUNDLE20", ris.getCodiceBuono());
        assertEquals(8.0, ris.getTotale(), 1e-9);
        assertSame(ris, SessionManager.getInstance().getOrdineInCorso());
    }

    @Test
    void applicaBuonoCodiceVuotoLancia() throws Exception {
        assertThrows(BusinessValidationException.class,
                () -> controller.applicaBuono("  ", "Miele", utenteBean()));
    }

    @Test
    void processaPagamentoConDatiCartaValidi() throws Exception {
        salvaProdotto("Caffè", 4.50);
        OrdineBean inCorso = controller.avviaCheckout("Caffè");

        OrdineBean esito = controller.processaPagamento(
                inCorso, utenteBean(), "1111222233334444", "Mario", "12/29", "123");
        assertNotNull(esito.getIdOrdine());
        assertEquals("CREATED", esito.getStato());
        assertNull(SessionManager.getInstance().getOrdineInCorso());
    }

    @Test
    void processaPagamentoCvvErratoLancia() throws Exception {
        salvaProdotto("Caffè", 4.50);
        OrdineBean inCorso = controller.avviaCheckout("Caffè");
        assertThrows(BusinessValidationException.class,
                () -> controller.processaPagamento(inCorso, utenteBean(),
                        "1111222233334444", "Mario", "12/29", "12"));
    }

    @Test
    void processaPagamentoNessunOrdineLanciaBusiness() throws Exception {
        assertThrows(BusinessValidationException.class,
                () -> controller.processaPagamento(null, utenteBean(),
                        "1111222233334444", "Mario", "12/29", "123"));
    }

    // -------- Rami aggiuntivi per copertura --------

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

    @Test
    void applicaBuonoUtenteEmailNull() throws Exception {
        Utente v = venditore();
        RuoloVenditore rv = (RuoloVenditore) v.getRuoli().get(0);
        factory.getProdottoDAO().save(new Prodotto("Miele", 10.0, 10,
                LocalDate.now().plusDays(7), UnitaEnum.PEZZI, rv));
        factory.getBuonoDAO().save(new BuonoPromozionale("BUNDLE20", rv,
                LocalDate.now().minusDays(1), LocalDate.now().plusDays(30),
                new ScontoPercentualeStrategy(0.20)));
        UtenteBean utenteConEmail = new UtenteBean();
        utenteConEmail.setUsername("Mario");
        utenteConEmail.setEmail("mario@cibo.it");
        // percorso monouso attivo (email presente)
        OrdineBean ris = controller.applicaBuono("BUNDLE20", "Miele", utenteConEmail);
        assertEquals("BUNDLE20", ris.getCodiceBuono());
    }
}
