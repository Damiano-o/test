package it.uniroma2.ispw.ciboamico.bean;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test Bean: validazione sintattica e getter/setter.
 
 * @author Michele Damiano
*/
class BeanTest {

    @Test
    void testProdottoBeanValidazione() {

        ProdottoBean bean = new ProdottoBean();
        assertFalse(bean.datiObbligatoriPresenti());
    }
    @Test
    void testProdottoBeanValidazioneParte2() {
        ProdottoBean bean = new ProdottoBean();
        assertFalse(bean.datiObbligatoriPresenti());

        bean.setNome("Latte");
        bean.setQuantita(2.0);
        bean.setScadenza(LocalDate.now().plusDays(10));
        bean.setPosizione("Frigo");
        bean.setUnitaMisura("LITRI");

        assertTrue(bean.datiObbligatoriPresenti());}
    @Test
    void testProdottoBeanValidazioneParte3() {
        ProdottoBean bean = new ProdottoBean();
        assertFalse(bean.datiObbligatoriPresenti());

        bean.setNome("Latte");
        bean.setQuantita(2.0);
        bean.setScadenza(LocalDate.now().plusDays(10));
        bean.setPosizione("Frigo");
        bean.setUnitaMisura("LITRI");

        assertTrue(bean.datiObbligatoriPresenti());
        assertEquals("Latte", bean.getNome());}
    @Test
    void testProdottoBeanValidazioneParte4() {
        ProdottoBean bean = new ProdottoBean();
        assertFalse(bean.datiObbligatoriPresenti());

        bean.setNome("Latte");
        bean.setQuantita(2.0);
        bean.setScadenza(LocalDate.now().plusDays(10));
        bean.setPosizione("Frigo");
        bean.setUnitaMisura("LITRI");

        assertTrue(bean.datiObbligatoriPresenti());
        assertEquals("Latte", bean.getNome());
        assertEquals(2.0, bean.getQuantita());}

    @Test
    void testProdottoBeanNomeVuoto() {
        ProdottoBean bean = new ProdottoBean();
        bean.setNome("   ");
        bean.setQuantita(2.0);
        bean.setScadenza(LocalDate.now().plusDays(10));
        bean.setPosizione("Frigo");
        bean.setUnitaMisura("LITRI");
        assertFalse(bean.datiObbligatoriPresenti());
    }

    @Test
    void testRicettaBean() {

        RicettaBean bean = new RicettaBean();
        assertFalse(bean.haAlmenoDueIngredienti());
    }
    @Test
    void testRicettaBeanParte2() {
        RicettaBean bean = new RicettaBean();
        assertFalse(bean.haAlmenoDueIngredienti());

        bean.setIngredientiNomi(java.util.List.of("A", "B"));
        assertTrue(bean.haAlmenoDueIngredienti());}
    @Test
    void testRicettaBeanParte3() {
        RicettaBean bean = new RicettaBean();
        assertFalse(bean.haAlmenoDueIngredienti());

        bean.setIngredientiNomi(java.util.List.of("A", "B"));
        assertTrue(bean.haAlmenoDueIngredienti());
        assertEquals("A", bean.getIngredientiNomi().get(0));}

    @Test
    void testUtenteBean() {
        UtenteBean bean = new UtenteBean();
        bean.setRuoloAttivo("VENDITORE");
        assertEquals("VENDITORE", bean.getRuoloAttivo());
    }

    @Test
    void testOrdineBean() {

        OrdineBean bean = new OrdineBean();
        bean.setIdOrdine(5L);
        bean.setTotale(10.0);
        bean.setStato("CREATED");
        bean.setCompratoreId("c1");
        bean.setVenditoreId("v1");

        assertEquals(5L, bean.getIdOrdine());
    }
    @Test
    void testOrdineBeanParte2() {
        OrdineBean bean = new OrdineBean();
        bean.setIdOrdine(5L);
        bean.setTotale(10.0);
        bean.setStato("CREATED");
        bean.setCompratoreId("c1");
        bean.setVenditoreId("v1");

        assertEquals(5L, bean.getIdOrdine());
        assertEquals(10.0, bean.getTotale());}
    @Test
    void testOrdineBeanParte3() {
        OrdineBean bean = new OrdineBean();
        bean.setIdOrdine(5L);
        bean.setTotale(10.0);
        bean.setStato("CREATED");
        bean.setCompratoreId("c1");
        bean.setVenditoreId("v1");

        assertEquals(5L, bean.getIdOrdine());
        assertEquals(10.0, bean.getTotale());
        assertEquals("CREATED", bean.getStato());}
    @Test
    void testOrdineBeanParte4() {
        OrdineBean bean = new OrdineBean();
        bean.setIdOrdine(5L);
        bean.setTotale(10.0);
        bean.setStato("CREATED");
        bean.setCompratoreId("c1");
        bean.setVenditoreId("v1");

        assertEquals(5L, bean.getIdOrdine());
        assertEquals(10.0, bean.getTotale());
        assertEquals("CREATED", bean.getStato());
        assertEquals("c1", bean.getCompratoreId());}
    @Test
    void testOrdineBeanParte5() {
        OrdineBean bean = new OrdineBean();
        bean.setIdOrdine(5L);
        bean.setTotale(10.0);
        bean.setStato("CREATED");
        bean.setCompratoreId("c1");
        bean.setVenditoreId("v1");

        assertEquals(5L, bean.getIdOrdine());
        assertEquals(10.0, bean.getTotale());
        assertEquals("CREATED", bean.getStato());
        assertEquals("c1", bean.getCompratoreId());
        assertEquals("v1", bean.getVenditoreId());}
}
