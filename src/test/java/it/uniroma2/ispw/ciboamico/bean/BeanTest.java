package it.uniroma2.ispw.ciboamico.bean;

import it.uniroma2.ispw.ciboamico.exception.AutenticazioneException;
import it.uniroma2.ispw.ciboamico.exception.BusinessValidationException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test Bean: validazione sintattica e getter/setter.
 
*/
class BeanTest {

    @Test
    void testProdottoBeanValidazione() throws Exception {
        ProdottoBean bean = new ProdottoBean();
        assertThrows(Exception.class, bean::validate);

        bean.setNome("Latte");
        bean.setQuantita(2.0);
        bean.setPrezzo(1.2);
        bean.setScadenza(LocalDate.now().plusDays(10));
        bean.setPosizione("Frigo");
        bean.setUnitaMisura("LITRI");

        bean.validate(); // non lancia ora
        assertEquals("Latte", bean.getNome());
        assertEquals(2.0, bean.getQuantita());
    }

    @Test
    void testProdottoBeanNomeVuoto() throws Exception {
        // Fail Fast del setter: il nome vuoto lancia subito
        // all'inserimento, non solo alla validate().
        ProdottoBean bean = new ProdottoBean();
        assertThrows(BusinessValidationException.class, () -> bean.setNome("   "));
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

    // Validazione nei setter (criterio bean: Fail Fast)

    @Test
    void emailVuotaONonValidaLancia() {
        AutenticazioneBean bean = new AutenticazioneBean();
        assertThrows(AutenticazioneException.class, () -> bean.setEmail("user@.it"));
        assertThrows(AutenticazioneException.class, () -> bean.setEmail(null));
    }

    @Test
    void passwordVuotaLancia() {
        AutenticazioneBean bean = new AutenticazioneBean();
        assertThrows(AutenticazioneException.class, () -> bean.setPassword(""));
    }

    @Test
    void cvvNonValidoLancia() {
        PaymentInfoBean bean = new PaymentInfoBean();
        assertThrows(BusinessValidationException.class, () -> bean.setCvv("12"));
        assertThrows(BusinessValidationException.class, () -> bean.setCvv("1234"));
    }

    @Test
    void importoNonPositivoLancia() {
        PaymentInfoBean bean = new PaymentInfoBean();
        assertThrows(BusinessValidationException.class, () -> bean.setImportoInCent(0L));
        assertThrows(BusinessValidationException.class, () -> bean.setImportoInCent(-5L));
    }

    @Test
    void prodottoVuotoONullLancia() {
        OrdineBean bean = new OrdineBean();
        assertThrows(BusinessValidationException.class, () -> bean.setNomeProdotto("   "));
        assertThrows(BusinessValidationException.class, () -> bean.setNomeProdotto(null));
    }

    @Test
    void emailUtenteNonValidaLancia() {
        assertThrows(AutenticazioneException.class,
                () -> new UtenteBean().setEmail("non-valida"));
    }
}
