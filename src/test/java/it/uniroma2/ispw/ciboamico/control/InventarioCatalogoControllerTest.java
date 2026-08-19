package it.uniroma2.ispw.ciboamico.control;

import it.uniroma2.ispw.ciboamico.bean.ProdottoBean;
import it.uniroma2.ispw.ciboamico.bean.UtenteBean;
import it.uniroma2.ispw.ciboamico.entity.RuoloVenditore;
import it.uniroma2.ispw.ciboamico.entity.StatoVenditoreEnum;
import it.uniroma2.ispw.ciboamico.entity.UnitaEnum;
import it.uniroma2.ispw.ciboamico.entity.Utente;
import it.uniroma2.ispw.ciboamico.pattern.singleton.SessionManager;
import it.uniroma2.ispw.ciboamico.persistence.factory.DemoDAOFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test GestisciInventarioController (UC-01) e GestisciCatalogoVenditoreController (UC-05).
 
 * @author Michele Damiano
*/
class InventarioCatalogoControllerTest {

    private GestisciInventarioController inventarioController;
    private GestisciCatalogoVenditoreController catalogoController;

    @BeforeEach
    void setup() {
        factory = new DemoDAOFactory();
        inventarioController = new GestisciInventarioController(factory);
        catalogoController = new GestisciCatalogoVenditoreController(factory);
    }

    @AfterEach
    void teardown() {
        SessionManager.getInstance().logout();
    }

    private DemoDAOFactory factory;

    private void loginVenditore(boolean approvato) {
        Utente venditore = new Utente("Marco", "marco@cibo.it", "hash");
        RuoloVenditore ruolo = new RuoloVenditore("RM", "tel");
        if (approvato) {
            ruolo.setStato(StatoVenditoreEnum.APPROVATO);
        }
        venditore.aggiungiRuolo(ruolo);
        factory.getUtenteDAO().save(venditore);

        UtenteBean sessione = new UtenteBean();
        sessione.setEmail("marco@cibo.it");
        SessionManager.getInstance().setLoggedUser(sessione);
    }

    private ProdottoBean beanValido() {
        ProdottoBean bean = new ProdottoBean();
        bean.setNome("Latte");
        bean.setQuantita(2.0);
        bean.setPrezzo(1.50);
        bean.setScadenza(LocalDate.now().plusDays(10));
        bean.setPosizione("Frigo");
        bean.setUnitaMisura("LITRI");
        return bean;
    }

    @Test
    void testAggiungiProdottoValidoRestituisceBean() {
        ProdottoBean bean = inventarioController.aggiungiProdotto(beanValido(), "demo@cibo.it");
        assertNotNull(bean);
    }

    @Test
    void testAggiungiProdottoValidoNomeCorretto() {
        ProdottoBean bean = inventarioController.aggiungiProdotto(beanValido(), "demo@cibo.it");
        assertEquals("Latte", bean.getNome());
    }

    @Test
    void testAggiungiProdottoDatiMancanti() {
        ProdottoBean bean = new ProdottoBean(); // vuoto
        assertThrows(IllegalArgumentException.class,
                () -> inventarioController.aggiungiProdotto(bean, "demo@cibo.it"));
    }

    @Test
    void testInventarioOrdinatoPerScadenzaContieneOrdine() {

        inventarioController.aggiungiProdotto(beanValido(), "demo@cibo.it");
        List<ProdottoBean> lista = inventarioController.visualizzaInventarioOrdinato("demo@cibo.it");
        assertFalse(lista.isEmpty());
    }
    @Test
    void testInventarioOrdinatoPerScadenzaContieneOrdineParte2() {
        inventarioController.aggiungiProdotto(beanValido(), "demo@cibo.it");
        List<ProdottoBean> lista = inventarioController.visualizzaInventarioOrdinato("demo@cibo.it");
        assertFalse(lista.isEmpty());
        assertEquals("Latte", lista.get(0).getNome());}

    @Test
    void testPubblicaProdottoVenditoreNonApprovato() {
        loginVenditore(false); // IN_ATTESA
        ProdottoBean bean = beanValido();
        assertThrows(IllegalStateException.class,
                () -> catalogoController.pubblicaProdotto(bean));
    }

    @Test
    void testPubblicaProdottoVenditoreApprovato() {

        loginVenditore(true);
        ProdottoBean bean = catalogoController.pubblicaProdotto(beanValido());
        assertNotNull(bean);
    }
    @Test
    void testPubblicaProdottoVenditoreApprovatoParte2() {
        loginVenditore(true);
        ProdottoBean bean = catalogoController.pubblicaProdotto(beanValido());
        assertNotNull(bean);
        assertEquals("Latte", bean.getNome());}

    @Test
    void testPubblicaProdottoPrezzoNonValido() {
        loginVenditore(true);
        ProdottoBean bean = beanValido();
        bean.setPrezzo(0.0);
        assertThrows(IllegalArgumentException.class,
                () -> catalogoController.pubblicaProdotto(bean));
    }

    @Test
    void testPubblicaProdottoUtenteNonLoggato() {
        ProdottoBean bean = beanValido();
        assertThrows(IllegalStateException.class,
                () -> catalogoController.pubblicaProdotto(bean));
    }

    @Test
    void testUnitaEnumValori() {
        assertSame(UnitaEnum.GRAMMI, UnitaEnum.valueOf("GRAMMI"));
    }
}
