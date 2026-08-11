package it.uniroma2.ispw.ciboamico.control.facade;

import it.uniroma2.ispw.ciboamico.bean.OrdineBean;
import it.uniroma2.ispw.ciboamico.bean.ProdottoBean;
import it.uniroma2.ispw.ciboamico.bean.UtenteBean;
import it.uniroma2.ispw.ciboamico.control.OrdinaProdottoController;
import it.uniroma2.ispw.ciboamico.exception.BusinessValidationException;
import it.uniroma2.ispw.ciboamico.persistence.factory.DAOFactory;

import java.util.List;

/**
 * Facade (GoF - Structural) per lo use case UC-04 "Ordina un Prodotto".
 *
 * <p>Espone alla boundary (GUI e CLI) un'unica interfaccia semplificata per
 * il checkout: catalogo, avvio del checkout, buono promozionale e pagamento.
 * È <b>stateless</b>: non mantiene stato di business, delega la logica al
 * {@link OrdinaProdottoController} (GRASP Controller) e lascia la sessione
 * all'utente autenticato in {@code SessionManager}.</p>
 *
 * <p>La boundary non conosce i metodi del controller né l'ordine di chiamata:
 * parla solo con il Facade, che orchestra la sequenza sottostante. Scambia
 * esclusivamente Bean (BCE).</p>
 */
public final class OrdinaProdottoFacade {

    private final OrdinaProdottoController controller;

    /** Facade costruita con il controller iniettato (testabile). */
    public OrdinaProdottoFacade(OrdinaProdottoController controller) {
        this.controller = controller;
    }

    /** Costruttore di comodo: risolve la factory attiva dalla modalità. */
    public OrdinaProdottoFacade(DAOFactory factory) {
        this(new OrdinaProdottoController(factory));
    }

    /** Costruttore di comodo per la boundary: usa la factory attiva di runtime. */
    public OrdinaProdottoFacade() {
        this(new OrdinaProdottoController());
    }

    /** Catalogo dei prodotti disponibili (UC-04, precondizione). */
    public List<ProdottoBean> getProdottiDisponibili() {
        return controller.getProdottiDisponibili();
    }

    /** Avvia il checkout per il prodotto selezionato (passo 2 UC-04). */
    public OrdineBean avviaCheckout(String nomeProdotto) throws BusinessValidationException {
        return controller.avviaCheckout(nomeProdotto);
    }

    /** Estensione 4a: applica il buono promozionale se presente. */
    public OrdineBean applicaBuono(String codiceBuono, String nomeProdotto, UtenteBean utente)
            throws BusinessValidationException {
        return controller.applicaBuono(codiceBuono, nomeProdotto, utente);
    }

    /** Passo 6: autorizza l'addebito e sottomette l'ordine. */
    public OrdineBean processaPagamento(OrdineBean ordine, UtenteBean utente,
                                        String numeroCarta, String intestatario,
                                        String scadenza, String cvv)
            throws BusinessValidationException {
        return controller.processaPagamento(ordine, utente, numeroCarta, intestatario, scadenza, cvv);
    }
}