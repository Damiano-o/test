package it.uniroma2.ispw.ciboamico.strategy;

import it.uniroma2.ispw.ciboamico.entity.*;
import it.uniroma2.ispw.ciboamico.pattern.strategy.SubstitutionMatchingStrategy;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * T03/T04 — SubstitutionMatchingStrategy (elemento innovativo D-01).
 
 * @author Michele Damiano
*/
class SubstitutionMatchingStrategyTest {

    private Ricetta ricetta(String nome, Prodotto... ingredienti) {
        Ricetta r = new Ricetta(nome, "istruzioni", new RuoloNutrizionista());
        for (Prodotto p : ingredienti) {
            r.aggiungiIngrediente(new Ingrediente(p, 1, UnitaEnum.GRAMMI));
        }
        return r;
    }

    private Prodotto prodotto(String nome) {
        return new Prodotto(nome, 1.0, 10, LocalDate.now().plusDays(30), UnitaEnum.GRAMMI, new RuoloVenditore("RM", "x"));
    }

    private ProdottoInventario inventario(String nome) {
        return new ProdottoInventario(nome, 100, LocalDate.now().plusDays(10), "Dispensa", UnitaEnum.GRAMMI, prodotto(nome));
    }

    @Test
    void testMatchWithSubstitution() {
        // Margarina disponibile → sostituisce Burro (fattore 1.2)
        ProdottoInventario margarina = inventario("Margarina");
        Ricetta ricetta = ricetta("Torta", prodotto("Burro"));

        List<Ricetta> risultato = new SubstitutionMatchingStrategy().match(List.of(margarina), List.of(ricetta));

        assertTrue(risultato.contains(ricetta));
    }

    @Test
    void testMatchWithoutSubstitute() {
        // Nessun ingrediente e nessun sostituto → lista vuota
        Ricetta ricetta = ricetta("Torta", prodotto("Burro"));

        List<Ricetta> risultato = new SubstitutionMatchingStrategy().match(List.of(), List.of(ricetta));

        assertEquals(0, risultato.size());
    }
}
