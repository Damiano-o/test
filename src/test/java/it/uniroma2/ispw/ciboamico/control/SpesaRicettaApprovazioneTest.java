package it.uniroma2.ispw.ciboamico.control;

import it.uniroma2.ispw.ciboamico.bean.ProdottoBean;
import it.uniroma2.ispw.ciboamico.bean.RicettaBean;
import it.uniroma2.ispw.ciboamico.bean.UtenteBean;
import it.uniroma2.ispw.ciboamico.entity.*;
import it.uniroma2.ispw.ciboamico.pattern.singleton.SessionManager;
import it.uniroma2.ispw.ciboamico.persistence.factory.DemoDAOFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test ListaSpesa (UC-03), Ricette Nutrizionista (UC-07), Approvazioni (UC-08/09).
 
 * @author Michele Damiano
*/
class SpesaRicettaApprovazioneTest {

    private DemoDAOFactory factory;
    private GestisciListaSpesaController listaSpesa;
    private GestisciRicetteNutrizionistaController ricetteCtrl;
    private ApprovazioneController approvazione;

    @BeforeEach
    void setup() {
        factory = new DemoDAOFactory();
        listaSpesa = new GestisciListaSpesaController(factory);
        ricetteCtrl = new GestisciRicetteNutrizionistaController(factory);
        approvazione = new ApprovazioneController(factory);
        loginNutrizionista();
    }

    @AfterEach
    void teardown() {
        SessionManager.getInstance().logout();
    }

    /** L'utente loggato è un nutrizionista (per UC-07). */
    private void loginNutrizionista() {
        Utente nutrizionista = new Utente("Anna", "anna@cibo.it", "hash");
        nutrizionista.aggiungiRuolo(new RuoloNutrizionista());
        factory.getUtenteDAO().save(nutrizionista);

        UtenteBean sessione = new UtenteBean();
        sessione.setEmail("anna@cibo.it");
        SessionManager.getInstance().setLoggedUser(sessione);
    }

    private RicettaBean ricettaConDueIngredienti() {
        RicettaBean bean = new RicettaBean();
        bean.setNome("Pasta al pomodoro");
        bean.setIstruzioni("bollire");
        bean.setIngredientiNomi(List.of("Pasta", "Pomodoro"));
        bean.setDosi(List.of(500.0, 3.0));
        return bean;
    }

    @Test
    void testCreaRicettaValida() {

        RicettaBean bean = ricetteCtrl.creaRicetta(ricettaConDueIngredienti());
        assertNotNull(bean);
    }
    @Test
    void testCreaRicettaValidaParte2() {
        RicettaBean bean = ricetteCtrl.creaRicetta(ricettaConDueIngredienti());
        assertNotNull(bean);
        assertEquals("Pasta al pomodoro", bean.getNome());}

    @Test
    void testCreaRicettaMenoDiDueIngredienti() {
        RicettaBean bean = new RicettaBean();
        bean.setNome("Solo pasta");
        bean.setIngredientiNomi(List.of("Pasta")); // 1 solo → BR-05
        assertThrows(IllegalArgumentException.class,
                () -> ricetteCtrl.creaRicetta(bean));
    }

    @Test
    void testCalcolaMancanze() {

        // Ricetta con 2 ingredienti nel catalogo
        ricetteCtrl.creaRicetta(ricettaConDueIngredienti());
        // Inventario: solo Pasta → manca Pomodoro
        ProdottoInventario pasta = new ProdottoInventario("Pasta", 1000,
                LocalDate.now().plusDays(100), "Dispensa", UnitaEnum.GRAMMI, null);
        factory.getProdottoDAO().saveInventario("demo@cibo.it", pasta);

        List<ProdottoBean> mancanti = listaSpesa.calcolaMancanze("demo@cibo.it", "Pasta al pomodoro");

        assertFalse(mancanti.isEmpty());
    }
    @Test
    void testCalcolaMancanzeParte2() {
        // Ricetta con 2 ingredienti nel catalogo
        ricetteCtrl.creaRicetta(ricettaConDueIngredienti());
        // Inventario: solo Pasta → manca Pomodoro
        ProdottoInventario pasta = new ProdottoInventario("Pasta", 1000,
                LocalDate.now().plusDays(100), "Dispensa", UnitaEnum.GRAMMI, null);
        factory.getProdottoDAO().saveInventario("demo@cibo.it", pasta);

        List<ProdottoBean> mancanti = listaSpesa.calcolaMancanze("demo@cibo.it", "Pasta al pomodoro");

        assertFalse(mancanti.isEmpty());
        assertEquals("Pomodoro", mancanti.get(0).getNome());}

    @Test
    void testApprovazioneVenditoreNonTrovato() {
        assertThrows(IllegalArgumentException.class,
                () -> approvazione.approvaVenditore("nessuno@cibo.it", true));
    }

    @Test
    void testRicetteInAttesa() {

        ricetteCtrl.creaRicetta(ricettaConDueIngredienti());
        List<RicettaBean> inAttesa = approvazione.ricetteInAttesa();
        assertEquals(1, inAttesa.size());
    }
    @Test
    void testRicetteInAttesaParte2() {
        ricetteCtrl.creaRicetta(ricettaConDueIngredienti());
        List<RicettaBean> inAttesa = approvazione.ricetteInAttesa();
        assertEquals(1, inAttesa.size());
        assertEquals("Pasta al pomodoro", inAttesa.get(0).getNome());}

    @Test
    void testApprovaRicetta() {

        ricetteCtrl.creaRicetta(ricettaConDueIngredienti());
        // Prima dell'approvazione: 1 ricetta in attesa
        assertEquals(1, approvazione.ricetteInAttesa().size());
    }
    @Test
    void testApprovaRicettaParte2() {
        ricetteCtrl.creaRicetta(ricettaConDueIngredienti());
        // Prima dell'approvazione: 1 ricetta in attesa
        assertEquals(1, approvazione.ricetteInAttesa().size());
        approvazione.approvaRicetta("Pasta al pomodoro", true);
        // Dopo l'approvazione: nessuna in attesa
        assertTrue(approvazione.ricetteInAttesa().isEmpty());}
}
