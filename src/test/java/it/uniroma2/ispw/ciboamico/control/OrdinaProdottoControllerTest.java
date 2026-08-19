package it.uniroma2.ispw.ciboamico.control;

import it.uniroma2.ispw.ciboamico.bean.OrdineBean;
import it.uniroma2.ispw.ciboamico.bean.UtenteBean;
import it.uniroma2.ispw.ciboamico.entity.*;
import it.uniroma2.ispw.ciboamico.exception.BusinessValidationException;
import it.uniroma2.ispw.ciboamico.persistence.factory.DemoDAOFactory;
import it.uniroma2.ispw.ciboamico.pattern.singleton.SessionManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test UC-04 OrdinaProdottoController: verifica che il VENDITORE sia risolto
 * dal prodotto (non dall'utente loggato) — regressione del bug fix 2026-08-02.
 
 * @author Michele Damiano
*/
class OrdinaProdottoControllerTest {

    private DemoDAOFactory factory;
    private OrdinaProdottoController controller;

    @BeforeEach
    void setup() {
        factory = new DemoDAOFactory();
        controller = new OrdinaProdottoController(factory);
        // Utente loggato = compratore
        UtenteBean bean = new UtenteBean();
        bean.setUsername("Mario");
        bean.setEmail("mario@cibo.it");
        SessionManager.getInstance().setLoggedUser(bean);
    }

    @AfterEach
    void cleanup() {
        SessionManager.getInstance().logout();
    }

    private UtenteBean utenteBean() {
        UtenteBean b = new UtenteBean();
        b.setUsername("Mario");
        b.setEmail("mario@cibo.it");
        return b;
    }

    @Test
    void testSubmitOrdineUtenteNull() {
        OrdineBean bean = new OrdineBean();
        assertThrows(IllegalStateException.class,
                () -> controller.submitOrdine(bean, null));
    }

    @Test
    void testSubmitOrdineProdottoNonTrovato() {
        OrdineBean bean = new OrdineBean();
        bean.setIdOrdine(999L); // non esiste nel catalogo demo
        assertThrows(IllegalStateException.class, () -> controller.submitOrdine(bean, utenteBean()));
    }

    @Test
    void testSubmitOrdineVenditoreDalProdotto() {

        // Venditore con back-reference all'Utente
        Utente utenteVenditore = new Utente("Marco", "marco@cibo.it", "h");
        RuoloVenditore rv = new RuoloVenditore("RM", "tel");
        utenteVenditore.aggiungiRuolo(rv); // setta back-reference

        Prodotto prodotto = new Prodotto("Pomodori", 2.0, 50,
                LocalDate.now().plusDays(7), UnitaEnum.GRAMMI, rv);
        factory.getProdottoDAO().save(prodotto);

        OrdineBean bean = new OrdineBean();
        bean.setIdOrdine((long) "Pomodori".hashCode());
        bean.setCompratoreId("mario@cibo.it");

        OrdineBean risultato = controller.submitOrdine(bean, utenteBean());

        assertNotNull(risultato);
    }
    @Test
    void testSubmitOrdineVenditoreDalProdottoParte2() {
        // Venditore con back-reference all'Utente
        Utente utenteVenditore = new Utente("Marco", "marco@cibo.it", "h");
        RuoloVenditore rv = new RuoloVenditore("RM", "tel");
        utenteVenditore.aggiungiRuolo(rv); // setta back-reference

        Prodotto prodotto = new Prodotto("Pomodori", 2.0, 50,
                LocalDate.now().plusDays(7), UnitaEnum.GRAMMI, rv);
        factory.getProdottoDAO().save(prodotto);

        OrdineBean bean = new OrdineBean();
        bean.setIdOrdine((long) "Pomodori".hashCode());
        bean.setCompratoreId("mario@cibo.it");

        OrdineBean risultato = controller.submitOrdine(bean, utenteBean());

        assertNotNull(risultato);
        assertNotNull(risultato.getIdOrdine());}
    @Test
    void testSubmitOrdineVenditoreDalProdottoParte3() {
        // Venditore con back-reference all'Utente
        Utente utenteVenditore = new Utente("Marco", "marco@cibo.it", "h");
        RuoloVenditore rv = new RuoloVenditore("RM", "tel");
        utenteVenditore.aggiungiRuolo(rv); // setta back-reference

        Prodotto prodotto = new Prodotto("Pomodori", 2.0, 50,
                LocalDate.now().plusDays(7), UnitaEnum.GRAMMI, rv);
        factory.getProdottoDAO().save(prodotto);

        OrdineBean bean = new OrdineBean();
        bean.setIdOrdine((long) "Pomodori".hashCode());
        bean.setCompratoreId("mario@cibo.it");

        OrdineBean risultato = controller.submitOrdine(bean, utenteBean());

        assertNotNull(risultato);
        assertNotNull(risultato.getIdOrdine());
        assertEquals("CREATED", risultato.getStato());}
    @Test
    void testSubmitOrdineVenditoreDalProdottoParte4() {
        // Venditore con back-reference all'Utente
        Utente utenteVenditore = new Utente("Marco", "marco@cibo.it", "h");
        RuoloVenditore rv = new RuoloVenditore("RM", "tel");
        utenteVenditore.aggiungiRuolo(rv); // setta back-reference

        Prodotto prodotto = new Prodotto("Pomodori", 2.0, 50,
                LocalDate.now().plusDays(7), UnitaEnum.GRAMMI, rv);
        factory.getProdottoDAO().save(prodotto);

        OrdineBean bean = new OrdineBean();
        bean.setIdOrdine((long) "Pomodori".hashCode());
        bean.setCompratoreId("mario@cibo.it");

        OrdineBean risultato = controller.submitOrdine(bean, utenteBean());

        assertNotNull(risultato);
        assertNotNull(risultato.getIdOrdine());
        assertEquals("CREATED", risultato.getStato());
        assertEquals(2.0, risultato.getTotale(), 1e-9);}

    @Test
    void testSubmitOrdineRiduceDisponibilita() {
        Utente utenteVenditore = new Utente("Marco", "marco@cibo.it", "h");
        RuoloVenditore rv = new RuoloVenditore("RM", "tel");
        utenteVenditore.aggiungiRuolo(rv);

        Prodotto prodotto = new Prodotto("Mele", 1.5, 3,
                LocalDate.now().plusDays(7), UnitaEnum.PEZZI, rv);
        factory.getProdottoDAO().save(prodotto);

        OrdineBean bean = new OrdineBean();
        bean.setIdOrdine((long) "Mele".hashCode());
        bean.setCompratoreId("mario@cibo.it");

        controller.submitOrdine(bean, utenteBean());
        assertEquals(2, prodotto.getQuantitaDisponibile());
    }

    @Test
    void testAcquistoQuantitaEccessivaLanciaEccezione() {
        Utente utenteVenditore = new Utente("Marco", "marco@cibo.it", "h");
        RuoloVenditore rv = new RuoloVenditore("RM", "tel");
        utenteVenditore.aggiungiRuolo(rv);

        // Un solo pezzo disponibile
        Prodotto prodotto = new Prodotto("Uova", 3.0, 1,
                LocalDate.now().plusDays(7), UnitaEnum.PEZZI, rv);
        factory.getProdottoDAO().save(prodotto);

        // Prima acquisto: consuma l'unico pezzo disponibile
        OrdineBean bean = new OrdineBean();
        bean.setIdOrdine((long) "Uova".hashCode());
        bean.setCompratoreId("mario@cibo.it");
        controller.submitOrdine(bean, utenteBean());

        // Secondo acquisto: quantità non più disponibile (estensione 2a)
        OrdineBean bean2 = new OrdineBean();
        bean2.setIdOrdine((long) "Uova".hashCode());
        bean2.setCompratoreId("mario@cibo.it");
        assertThrows(BusinessValidationException.class,
                () -> controller.submitOrdine(bean2, utenteBean()));
    }
}
