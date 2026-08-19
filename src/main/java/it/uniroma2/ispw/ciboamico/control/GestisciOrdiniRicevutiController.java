package it.uniroma2.ispw.ciboamico.control;

import it.uniroma2.ispw.ciboamico.bean.OrdineBean;
import it.uniroma2.ispw.ciboamico.entity.Ordine;
import it.uniroma2.ispw.ciboamico.entity.StatoOrdineEnum;
import it.uniroma2.ispw.ciboamico.persistence.dao.OrdineDAO;
import it.uniroma2.ispw.ciboamico.persistence.factory.DAOFactory;

import java.util.List;

/**
 * Control di UC-06 Gestisci Ordini Ricevuti.
 * Il venditore aggiorna lo stato degli ordini (validazione BR-04).
 * Flusso Bean-only: input email (primitivo), output List<OrdineBean>.
 * La Boundary non vede mai le Entity Ordine/Utente.
 */
public class GestisciOrdiniRicevutiController {

    private final OrdineDAO ordineDAO;

    public GestisciOrdiniRicevutiController(DAOFactory factory) {
        this.ordineDAO = factory.getOrdineDAO();
    }

    /** UC-06 MSS: recupera ordini CREATED/CONFIRMED del venditore (Bean-only). */
    public List<OrdineBean> visualizzaOrdiniRicevuti(String emailVenditore) {
        return ordineDAO.findByVenditore(emailVenditore).stream()
                .filter(o -> o.getStato() == StatoOrdineEnum.CREATED
                        || o.getStato() == StatoOrdineEnum.CONFIRMED)
                .map(this::aBean)
                .toList();
    }

    /** UC-06 MSS: stati aggiornabili dal venditore come stringhe (Bean-only). */
    public List<String> getStatiAggiornabili() {
        return List.of(
                StatoOrdineEnum.CONFIRMED.name(),
                StatoOrdineEnum.IN_DELIVERY.name(),
                StatoOrdineEnum.DELIVERED.name(),
                StatoOrdineEnum.ANNULLED.name());
    }

    /** UC-06 MSS: valida transizione (BR-04), aggiorna, notifica observer. */
    public OrdineBean aggiornaStato(Long idOrdine, String nuovoStato) {
        Ordine ordine = ordineDAO.findById(idOrdine);
        if (ordine == null) {
            throw new IllegalArgumentException("Ordine non trovato");
        }
        ordine.cambiaStato(StatoOrdineEnum.valueOf(nuovoStato)); // IllegalStateException se non valida
        ordineDAO.save(ordine);
        return aBean(ordine);
    }

    /** Mapping Entity → Bean (mai esposta alla Boundary). */
    private OrdineBean aBean(Ordine ordine) {
        OrdineBean bean = new OrdineBean();
        bean.setIdOrdine(ordine.getIdOrdine());
        bean.setTotale(ordine.getTotale());
        bean.setStato(ordine.getStato().name());
        bean.setCompratoreId(ordine.getCompratore().getEmail());
        bean.setVenditoreId(ordine.getVenditore().getEmail());
        return bean;
    }
}
