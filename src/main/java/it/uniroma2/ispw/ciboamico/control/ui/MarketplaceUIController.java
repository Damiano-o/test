package it.uniroma2.ispw.ciboamico.control.ui;

import it.uniroma2.ispw.ciboamico.bean.OrdineBean;
import it.uniroma2.ispw.ciboamico.bean.ProdottoBean;
import it.uniroma2.ispw.ciboamico.bean.UtenteBean;
import it.uniroma2.ispw.ciboamico.control.facade.OrdinaProdottoFacade;
import it.uniroma2.ispw.ciboamico.enums.ExceptionMessagesEnum;
import it.uniroma2.ispw.ciboamico.enums.UserErrorMessagesEnum;
import it.uniroma2.ispw.ciboamico.exception.BusinessValidationException;
import it.uniroma2.ispw.ciboamico.exception.DAOException;
import it.uniroma2.ispw.ciboamico.pattern.singleton.SessionManager;
import it.uniroma2.ispw.ciboamico.persistence.factory.DAOFactory;

import java.util.List;

/**
 * Controller di presentazione (UI) della MarketPlace (UC-04 Ordina Prodotto),
 * condiviso da GUI e CLI.
 *
 * <p>Coordina l'interazione tra
 * la {@code MarketplaceView} (boundary/UI) e il controller applicativo
 * <b>disaccoppiato</b>: raccoglie gli input dell'utente, costruisce la mappa
 * input → processi di business (via {@link OrdinaProdottoFacade}), applica la
 * validazione di presentazione e restituisce il risultato in una forma pronta
 * per la View. La view, così, resta un puro layout.</p>
 *
 * <p>Non dipende da JavaFX: è testabile in isolamento e riusabile sia dalla
 * vista grafica sia da quella testuale (CLI). Se la view evolve, si tocca solo
 * questo controller di presentazione.</p>
 */
public class MarketplaceUIController {

    private final OrdinaProdottoFacade facade;
    private final UtenteBean utente;

    public MarketplaceUIController(DAOFactory factory, UtenteBean utente) {
        this.facade = new OrdinaProdottoFacade(factory);
        this.utente = utente;
    }

    /** Costruttore di comodo per la boundary: factory attiva di runtime. */
    public MarketplaceUIController(UtenteBean utente) {
        this.facade = new OrdinaProdottoFacade();
        this.utente = utente;
    }

    /** Catalogo dei prodotti disponibili (per la View). */
    public List<ProdottoBean> catalogoProdotti() throws DAOException {
        return facade.getProdottiDisponibili();
    }

    /**
     * Mappa l'azione "aggiorna catalogo": recupera i prodotti e restituisce la
     * riga di stato da mostrare sulla UI.
     *
     * @return messaggio di esito da presentare nella view
     */
    public String aggiornaCatalogo() throws DAOException {
        return catalogoProdotti().size() + " prodotti disponibili nel marketplace locale.";
    }

    /**
     * Mappa l'azione "ordina il prodotto selezionato": converte il nome
     * prodotto (formato esterno) nell'ordine in checkout via
     * {@link OrdineBean#fromCheckout(String)}, poi avvia il checkout sul Facade.
     *
     * @param nomeProdotto prodotto scelto dall'utente
     * @return l'ordine in corso se la selezione è valida, {@code null} altrimenti
     */
    public OrdineBean ordinaProdotto(String nomeProdotto)
            throws BusinessValidationException, DAOException {
        if (nomeProdotto == null || nomeProdotto.isBlank()) {
            throw new BusinessValidationException(
                    UserErrorMessagesEnum.PRODOTTO_NON_SELEZIONATO_MSG.message,
                    ExceptionMessagesEnum.PRODOTTO_NON_SELEZIONATO.message,
                    "ERR-PRODOTTO-NON-SELEZIONATO");
        }
        OrdineBean inCorso = OrdineBean.fromCheckout(nomeProdotto);
        return facade.avviaCheckout(inCorso);
    }

    /**
     * Mappa l'azione "applica buono promozionale": convierte il codice buono e
     * applica lo sconto sull'ordine in checkout (da {@link SessionManager}).
     * La costruzione del bean resta nel grafico; il Facade delega al controller
     * applicativo state-less.
     *
     * @param codiceBuono codice buono inserito
     * @param nomeProdotto prodotto selezionato (usato se non c'è checkout in corso)
     * @return l'ordine aggiornato dopo lo sconto
     */
    public OrdineBean applicaBuono(String codiceBuono, String nomeProdotto)
            throws BusinessValidationException, DAOException {
        OrdineBean inCorso = SessionManager.getInstance().getOrdineInCorso();
        if (inCorso == null) {
            inCorso = OrdineBean.fromCheckout(nomeProdotto);
        }
        return facade.applicaBuono(codiceBuono, inCorso, utente);
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
