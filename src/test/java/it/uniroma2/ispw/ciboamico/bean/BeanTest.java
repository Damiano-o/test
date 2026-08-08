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
    void testProdottoBeanValidazione() throws Exception {
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
    void testProdottoBeanNomeVuoto() throws Exception {
        ProdottoBean bean = new ProdottoBean();
        bean.setNome("   ");
        bean.setQuantita(2.0);
        bean.setScadenza(LocalDate.now().plusDays(10));
        bean.setPosizione("Frigo");
        bean.setUnitaMisura("LITRI");
        assertFalse(bean.datiObbligatoriPresenti());
    }



    @Test
    void testOrdineBean() throws Exception {
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
