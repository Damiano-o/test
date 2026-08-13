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

// Controller di presentazione (UI) della MarketPlace (UC-04 Ordina Prodotto), condiviso d...

public class MarketplaceUIController {

    private final OrdinaProdottoFacade facade;
    private final UtenteBean utente;

    public MarketplaceUIController(DAOFactory factory, UtenteBean utente) {
        this.facade = new OrdinaProdottoFacade(factory);
        this.utente = utente;
    }

    public MarketplaceUIController(UtenteBean utente) {
        this.facade = new OrdinaProdottoFacade();
        this.utente = utente;
    }

    public List<ProdottoBean> catalogoProdotti() throws DAOException {
        return facade.getProdottiDisponibili();
    }

    // Mappa l'azione "aggiorna catalogo": recupera i prodotti e restituisce la riga di stato...

    public String aggiornaCatalogo() throws DAOException {
        return catalogoProdotti().size() + " prodotti disponibili nel marketplace locale.";
    }

    // Mappa l'azione "ordina il prodotto selezionato": converte il nome prodotto (formato est...

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

    // Mappa l'azione "applica buono promozionale": convierte il codice buono e applica lo sco...

    public OrdineBean applicaBuono(String codiceBuono, String nomeProdotto)
            throws BusinessValidationException, DAOException {
        OrdineBean inCorso = SessionManager.getInstance().getOrdineInCorso();
        if (inCorso == null) {
            inCorso = OrdineBean.fromCheckout(nomeProdotto);
        }
        return facade.applicaBuono(codiceBuono, inCorso, utente);
    }

    // Formatta l'esito dell'operazione "applica buono" per la presentazione

    public static String formattaEsitoBuono(OrdineBean ris) {
        return "Buono \"" + ris.getCodiceBuono()
                + "\" applicato ✓ — totale " + String.format(java.util.Locale.ROOT, "%.2f", ris.getTotale()) + " EUR";
    }
}
