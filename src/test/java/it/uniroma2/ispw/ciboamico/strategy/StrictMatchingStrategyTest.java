package it.uniroma2.ispw.ciboamico.strategy;

import it.uniroma2.ispw.ciboamico.entity.*;
import it.uniroma2.ispw.ciboamico.pattern.strategy.StrictMatchingStrategy;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * T01/T02 — StrictMatchingStrategy: solo ricette 100% compatibili.
 
 * @author Michele Damiano
*/
class StrictMatchingStrategyTest {

    private Ricetta ricetta(String nome, Prodotto... ingredienti) {
        Ricetta r = new Ricetta(nome, "istruzioni", new RuoloNutrizionista());
        for (Prodotto p : ingredienti) {
            r.aggiungiIngrediente(new Ingrediente(p, 1, UnitaEnum.PEZZI));
        }
        return r;
    }

    private Prodotto prodotto(String nome) {
        return new Prodotto(nome, 1.0, 10, LocalDate.now().plusDays(30), UnitaEnum.PEZZI, new RuoloVenditore("RM", "x"));
    }

    @Test
    void testMatchCompatible() {
        ProdottoInventario uovo = new ProdottoInventario("Uovo", 5, LocalDate.now().plusDays(10), "Frigo", UnitaEnum.PEZZI, prodotto("Uovo"));
        Ricetta ricetta = ricetta("Uova al tegamino", prodotto("Uovo"));

        List<Ricetta> risultato = new StrictMatchingStrategy().match(List.of(uovo), List.of(ricetta));

        assertTrue(risultato.contains(ricetta));
    }

    @Test
    void testMatchNotCompatible() {
        ProdottoInventario uovo = new ProdottoInventario("Uovo", 5, LocalDate.now().plusDays(10), "Frigo", UnitaEnum.PEZZI, prodotto("Uovo"));
        Ricetta ricetta = ricetta("Frittata", prodotto("Uovo"), prodotto("Farina"));

        List<Ricetta> risultato = new StrictMatchingStrategy().match(List.of(uovo), List.of(ricetta));

        assertEquals(0, risultato.size());
    }
}
