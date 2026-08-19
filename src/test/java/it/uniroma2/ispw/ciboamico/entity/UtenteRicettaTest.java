package it.uniroma2.ispw.ciboamico.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test Utente + Ruoli (whole-part, metamorfosi) e Ricetta (BR-05).
 
 * @author Michele Damiano
*/
class UtenteRicettaTest {

    @Test
    void testMetamorfosiRuolo() {

        Utente u = new Utente("Mario", "mario@cibo.it", "hash");
        assertFalse(u.haRuolo(RuoloVenditore.class));
    }
    @Test
    void testMetamorfosiRuoloParte2() {
        Utente u = new Utente("Mario", "mario@cibo.it", "hash");
        assertFalse(u.haRuolo(RuoloVenditore.class));

        RuoloVenditore rv = new RuoloVenditore("RM", "tel");
        rv.setStato(StatoVenditoreEnum.APPROVATO);
        u.aggiungiRuolo(rv);

        assertTrue(u.haRuolo(RuoloVenditore.class));}
    @Test
    void testMetamorfosiRuoloParte3() {
        Utente u = new Utente("Mario", "mario@cibo.it", "hash");
        assertFalse(u.haRuolo(RuoloVenditore.class));

        RuoloVenditore rv = new RuoloVenditore("RM", "tel");
        rv.setStato(StatoVenditoreEnum.APPROVATO);
        u.aggiungiRuolo(rv);

        assertTrue(u.haRuolo(RuoloVenditore.class));
        assertTrue(u.isVenditoreApprovato());}

    @Test
    void testVenditoreNonApprovato() {
        Utente u = new Utente("Mario", "mario@cibo.it", "hash");
        u.aggiungiRuolo(new RuoloVenditore("RM", "tel")); // stato IN_ATTESA
        assertFalse(u.isVenditoreApprovato());
    }

    @Test
    void testNomeRuoliConcreti() {

        assertEquals("AMMINISTRATORE", new RuoloAmministratore().getNomeRuolo());
    }
    @Test
    void testNomeRuoliConcretiParte2() {
        assertEquals("AMMINISTRATORE", new RuoloAmministratore().getNomeRuolo());
        assertEquals("CLIENTE", new RuoloCliente().getNomeRuolo());}
    @Test
    void testNomeRuoliConcretiParte3() {
        assertEquals("AMMINISTRATORE", new RuoloAmministratore().getNomeRuolo());
        assertEquals("CLIENTE", new RuoloCliente().getNomeRuolo());
        assertEquals("NUTRIZIONISTA", new RuoloNutrizionista().getNomeRuolo());}
    @Test
    void testNomeRuoliConcretiParte4() {
        assertEquals("AMMINISTRATORE", new RuoloAmministratore().getNomeRuolo());
        assertEquals("CLIENTE", new RuoloCliente().getNomeRuolo());
        assertEquals("NUTRIZIONISTA", new RuoloNutrizionista().getNomeRuolo());
        assertEquals("VENDITORE", new RuoloVenditore("RM", "tel").getNomeRuolo());}

    @Test
    void testRicettaHaAlmenoDueIngredienti() {

        RuoloNutrizionista nutrizionista = new RuoloNutrizionista();
        Ricetta r = new Ricetta("Pasta al pomodoro", "bollire", nutrizionista);
        assertFalse(r.haAlmenoDueIngredienti());
    }
    @Test
    void testRicettaHaAlmenoDueIngredientiParte2() {
        RuoloNutrizionista nutrizionista = new RuoloNutrizionista();
        Ricetta r = new Ricetta("Pasta al pomodoro", "bollire", nutrizionista);
        assertFalse(r.haAlmenoDueIngredienti()); // BR-05: serve >= 2

        Prodotto pasta = new Prodotto("Pasta", 2.0, 10, java.time.LocalDate.now().plusDays(100),
                UnitaEnum.GRAMMI, null);
        Prodotto pomodoro = new Prodotto("Pomodoro", 1.0, 10, java.time.LocalDate.now().plusDays(10),
                UnitaEnum.PEZZI, null);
        r.aggiungiIngrediente(new Ingrediente(pasta, 500, UnitaEnum.GRAMMI));
        r.aggiungiIngrediente(new Ingrediente(pomodoro, 3, UnitaEnum.PEZZI));

        assertTrue(r.haAlmenoDueIngredienti());}
}
