package it.uniroma2.ispw.ciboamico.control.graphic;

import it.uniroma2.ispw.ciboamico.bean.OrdineBean;
import it.uniroma2.ispw.ciboamico.bean.ProdottoBean;
import it.uniroma2.ispw.ciboamico.bean.UtenteBean;
import it.uniroma2.ispw.ciboamico.control.facade.OrdinaProdottoFacade;
import it.uniroma2.ispw.ciboamico.exception.BusinessValidationException;
import it.uniroma2.ispw.ciboamico.persistence.factory.DAOFactory;

import java.util.List;

/**
 * Controller grafico della MarketPlace (UC-04 Ordina Prodotto).
 *
 * <p>Come da lezione sul controller grafico (MVC), coordina l'interazione tra
 * la {@code MarketplaceView} (boundary/UI) e il controller applicativo
 * <b>disaccoppiato</b>: raccoglie gli input dell'utente, costruisce la mappa
 * input → processi di business (via {@link OrdinaProdottoFacade}), applica la
 * validazione di presentazione e restituisce il risultato in una forma pronta
 * per la View. La view, così, resta un puro layout.</p>
 *
 * <p>Non dipende da JavaFX: è testabile in isolamento. Se la view evolve,
 * l'unico componente da toccare è questo controller grafico, non il controller
 * applicativo (variante <em>disaccoppiata</em> delle slide).</p>
 */
public class MarketplaceGraphicController {

    private final OrdinaProdottoFacade facade;
    private final UtenteBean utente;

    public MarketplaceGraphicController(DAOFactory factory, UtenteBean utente) {
        this.facade = new OrdinaProdottoFacade(factory);
        this.utente = utente;
    }

    /** Costruttore di comodo per la boundary: factory attiva di runtime. */
    public MarketplaceGraphicController(UtenteBean utente) {
        this.facade = new OrdinaProdottoFacade();
        this.utente = utente;
    }

    /** Catalogo dei prodotti disponibili (per la View). */
    public List<ProdottoBean> catalogoProdotti() throws it.uniroma2.ispw.ciboamico.exception.DAOException {
        return facade.getProdottiDisponibili();
    }

    /**
     * Mappa l'azione "aggiorna catalogo": recupera i prodotti e restituisce la
     * riga di stato da mostrare sulla UI.
     *
     * @return messaggio di esito da presentare nella view
     */
    public String aggiornaCatalogo() throws it.uniroma2.ispw.ciboamico.exception.DAOException {
        return catalogoProdotti().size() + " prodotti disponibili nel marketplace locale.";
    }

    /**
     * Mappa l'azione "ordina il prodotto selezionato": avvia il checkout e
     * valida la selezione.
     *
     * @param nomeProdotto prodotto scelto dall'utente (valido anche per input vuoto)
     * @return l'ordine in corso se la selezione è valida, {@code null} altrimenti
     */
    public OrdineBean ordinaProdotto(String nomeProdotto)
            throws BusinessValidationException, it.uniroma2.ispw.ciboamico.exception.DAOException {
        if (nomeProdotto == null || nomeProdotto.isBlank()) {
            throw new BusinessValidationException("Seleziona un prodotto dal catalogo.");
        }
        return facade.avviaCheckout(nomeProdotto);
    }

    /**
     * Mappa l'azione "applica buono promozionale": delega l'applicazione dello
     * sconto al controller applicativo via Facade.
     *
     * @param codiceBuono  codice buono inserito
     * @param nomeProdotto prodotto selezionato
     * @return l'ordine aggiornato dopo lo sconto
     */
    public OrdineBean applicaBuono(String codiceBuono, String nomeProdotto)
            throws BusinessValidationException, it.uniroma2.ispw.ciboamico.exception.DAOException {
        return facade.applicaBuono(codiceBuono, nomeProdotto, utente);
    }

    /**
     * Formatta l'esito dell'operazione "applica buono" per la presentazione.
     *
     * @param ris ordine risultante dall'applicazione del buono
     * @return stringa di riepilogo da mostrare nella view
     */
    public static String formattaEsitoBuono(OrdineBean ris) {
        return "Buono \"" + ris.getCodiceBuono()
                + "\" applicato ✓ — totale " + String.format("%.2f", ris.getTotale()) + " EUR";
    }
}
