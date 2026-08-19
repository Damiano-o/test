package it.uniroma2.ispw.ciboamico.strategy;

import it.uniroma2.ispw.ciboamico.bean.RicettaBean;
import it.uniroma2.ispw.ciboamico.entity.*;
import it.uniroma2.ispw.ciboamico.pattern.facade.RicettaMatchingFacade;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test Facade RicettaMatchingFacade: Strategy + conversione Entity→Bean.
 
 * @author Michele Damiano
*/
class RicettaMatchingFacadeTest {

    private Ricetta ricettaCompleta() {
        Ricetta r = new Ricetta("Pasta al pomodoro", "bollire", new RuoloNutrizionista());
        Prodotto pasta = new Prodotto("Pasta", 2.0, 100, LocalDate.now().plusDays(100),
                UnitaEnum.GRAMMI, null);
        Prodotto pomodoro = new Prodotto("Pomodoro", 1.0, 100, LocalDate.now().plusDays(10),
                UnitaEnum.PEZZI, null);
        r.aggiungiIngrediente(new Ingrediente(pasta, 500, UnitaEnum.GRAMMI));
        r.aggiungiIngrediente(new Ingrediente(pomodoro, 3, UnitaEnum.PEZZI));
        r.setStato(StatoRicettaEnum.APPROVATA);
        return r;
    }

    @Test
    void testFacadeConInventarioCompleto() {

        RicettaMatchingFacade facade = RicettaMatchingFacade.conSostituzione();
        ProdottoInventario pasta = new ProdottoInventario("Pasta", 1000,
                LocalDate.now().plusDays(100), "Dispensa", UnitaEnum.GRAMMI, null);
        ProdottoInventario pomodoro = new ProdottoInventario("Pomodoro", 5,
                LocalDate.now().plusDays(5), "Frigo", UnitaEnum.PEZZI, null);

        List<RicettaBean> beans = facade.getRecipes(List.of(pasta, pomodoro), List.of(ricettaCompleta()));

        assertEquals(1, beans.size());
    }
    @Test
    void testFacadeConInventarioCompletoParte2() {
        RicettaMatchingFacade facade = RicettaMatchingFacade.conSostituzione();
        ProdottoInventario pasta = new ProdottoInventario("Pasta", 1000,
                LocalDate.now().plusDays(100), "Dispensa", UnitaEnum.GRAMMI, null);
        ProdottoInventario pomodoro = new ProdottoInventario("Pomodoro", 5,
                LocalDate.now().plusDays(5), "Frigo", UnitaEnum.PEZZI, null);

        List<RicettaBean> beans = facade.getRecipes(List.of(pasta, pomodoro), List.of(ricettaCompleta()));

        assertEquals(1, beans.size());
        assertEquals("Pasta al pomodoro", beans.get(0).getNome());}
    @Test
    void testFacadeConInventarioCompletoParte3() {
        RicettaMatchingFacade facade = RicettaMatchingFacade.conSostituzione();
        ProdottoInventario pasta = new ProdottoInventario("Pasta", 1000,
                LocalDate.now().plusDays(100), "Dispensa", UnitaEnum.GRAMMI, null);
        ProdottoInventario pomodoro = new ProdottoInventario("Pomodoro", 5,
                LocalDate.now().plusDays(5), "Frigo", UnitaEnum.PEZZI, null);

        List<RicettaBean> beans = facade.getRecipes(List.of(pasta, pomodoro), List.of(ricettaCompleta()));

        assertEquals(1, beans.size());
        assertEquals("Pasta al pomodoro", beans.get(0).getNome());
        assertEquals(2, beans.get(0).getIngredientiNomi().size());}
    @Test
    void testFacadeConInventarioCompletoParte4() {
        RicettaMatchingFacade facade = RicettaMatchingFacade.conSostituzione();
        ProdottoInventario pasta = new ProdottoInventario("Pasta", 1000,
                LocalDate.now().plusDays(100), "Dispensa", UnitaEnum.GRAMMI, null);
        ProdottoInventario pomodoro = new ProdottoInventario("Pomodoro", 5,
                LocalDate.now().plusDays(5), "Frigo", UnitaEnum.PEZZI, null);

        List<RicettaBean> beans = facade.getRecipes(List.of(pasta, pomodoro), List.of(ricettaCompleta()));

        assertEquals(1, beans.size());
        assertEquals("Pasta al pomodoro", beans.get(0).getNome());
        assertEquals(2, beans.get(0).getIngredientiNomi().size());
        assertTrue(beans.get(0).haAlmenoDueIngredienti());}

    @Test
    void testFacadeConSostituzione() {
        RicettaMatchingFacade facade = RicettaMatchingFacade.conSostituzione();
        ProdottoInventario margarina = new ProdottoInventario("Margarina", 500,
                LocalDate.now().plusDays(100), "Dispensa", UnitaEnum.GRAMMI, null);

        Ricetta torta = new Ricetta("Torta", "forno", new RuoloNutrizionista());
        Prodotto burro = new Prodotto("Burro", 3.0, 100, LocalDate.now().plusDays(60),
                UnitaEnum.GRAMMI, null);
        torta.aggiungiIngrediente(new Ingrediente(burro, 200, UnitaEnum.GRAMMI));

        List<RicettaBean> beans = facade.getRecipes(List.of(margarina), List.of(torta));

        assertEquals(1, beans.size()); // sostituzione burro→margarina
    }

    @Test
    void testFacadeSenzaRisultati() {
        RicettaMatchingFacade facade = RicettaMatchingFacade.conSostituzione();
        List<RicettaBean> beans = facade.getRecipes(List.of(), List.of(ricettaCompleta()));
        assertTrue(beans.isEmpty());
    }
}
