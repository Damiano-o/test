package it.uniroma2.ispw.ciboamico.control.facade;

import it.uniroma2.ispw.ciboamico.bean.OrdineBean;
import it.uniroma2.ispw.ciboamico.bean.PaymentInfoBean;
import it.uniroma2.ispw.ciboamico.bean.ProdottoBean;
import it.uniroma2.ispw.ciboamico.bean.UtenteBean;
import it.uniroma2.ispw.ciboamico.control.ApplicaBuonoPromozionaleController;
import it.uniroma2.ispw.ciboamico.control.OrdinaProdottoController;
import it.uniroma2.ispw.ciboamico.control.PagamentoController;
import it.uniroma2.ispw.ciboamico.exception.BusinessValidationException;
import it.uniroma2.ispw.ciboamico.exception.DAOException;
import it.uniroma2.ispw.ciboamico.pattern.singleton.SessionManager;
import it.uniroma2.ispw.ciboamico.persistence.factory.DAOFactory;

import java.util.List;

/**
 * Facade (GoF - Structural) per lo use case UC-04 "Ordina un Prodotto".
 *
 * <p>Espone alla boundary (GUI e CLI) un'unica interfaccia semplificata per
 * il checkout: catalogo, avvio del checkout, buono promozionale e pagamento.
 * È <b>stateless</b>: non mantiene stato di business e delega la logica ai
 * controller applicativi del sottosistema UC-04 —
 * {@link OrdinaProdottoController} (GRASP Controller del caso d'uso base),
 * {@link PagamentoController} (passo 6/estensione 6a) e
 * {@link ApplicaBuonoPromozionaleController} (estensione 4a) — lasciando la
 * sessione all'utente autenticato in {@code SessionManager}.</p>
 *
 * <p>La boundary non conosce i metodi del controller né l'ordine di chiamata:
 * parla solo con il Facade, che orchestra la sequenza sottostante. Scambia
 * esclusivamente Bean (BCE).</p>
 */
public final class OrdinaProdottoFacade {

    private final OrdinaProdottoController controller;
    private final PagamentoController pagamentoController;
    private final ApplicaBuonoPromozionaleController buonoController;

    /** Facade costruita con i controller applicativi iniettati (testabile). */
    public OrdinaProdottoFacade(OrdinaProdottoController controller,
                                PagamentoController pagamentoController,
                                ApplicaBuonoPromozionaleController buonoController) {
        this.controller = controller;
        this.pagamentoController = pagamentoController;
        this.buonoController = buonoController;
    }

    /**
     * Facade costruita risolvendo i controller applicativi a parte: il
     * controller principale di UC-04 più i sottocontroller delle estensioni.
     */
    public OrdinaProdottoFacade(OrdinaProdottoController controller) {
        this(controller,
                new PagamentoController(),
                new ApplicaBuonoPromozionaleController());
    }

    /** Costruttore di comodo: risolve la factory attiva dalla modalità. */
    public OrdinaProdottoFacade(DAOFactory factory) {
        this(new OrdinaProdottoController(factory),
                new PagamentoController(factory),
                new ApplicaBuonoPromozionaleController(factory));
    }

    /** Costruttore di comodo per la boundary: usa la factory attiva di runtime. */
    public OrdinaProdottoFacade() {
        this(new OrdinaProdottoController());
    }

    /** Catalogo dei prodotti disponibili (UC-04, precondizione). */
    public List<ProdottoBean> getProdottiDisponibili() throws DAOException {
        return controller.getProdottiDisponibili();
    }

    /** Avvia il checkout per il prodotto selezionato (passo 2 UC-04). Il
     * controller di presentazione ha già costruito l'ordine in checkout via
     * {@link OrdineBean#fromCheckout(String)} (conversione esterno→interno); il
     * Facade orchestra e aggiorna l'ordine in corso in
     * {@link SessionManager}. */
    public OrdineBean avviaCheckout(OrdineBean inCorso)
            throws BusinessValidationException, DAOException {
        OrdineBean avviato = controller.avviaCheckout(inCorso);
        SessionManager.getInstance().setOrdineInCorso(avviato);
        return avviato;
    }

    /** Estensione 4a: applica il buono promozionale se presente. Il bean è già
     * l'ordine in checkout (costruito dal controller di presentazione); il Facade
     * orchestra e aggiorna il totale scontato in {@link SessionManager}. */
    public OrdineBean applicaBuono(String codiceBuono, OrdineBean bean, UtenteBean utente)
            throws BusinessValidationException, DAOException {
        OrdineBean scontato = buonoController.applicaBuonoPromozionale(codiceBuono, bean, utente);
        SessionManager.getInstance().setOrdineInCorso(scontato);
        return scontato;
    }

    /** Passo 6: autorizza l'addebito e sottomette l'ordine. Il
     * {@link PaymentInfoBean} è già costruito dal controller di presentazione/boundary
     * (conversione esterno→interno). */
    public OrdineBean processaPagamento(OrdineBean ordine, UtenteBean utente, PaymentInfoBean payment)
            throws BusinessValidationException, DAOException {
        return pagamentoController.processaPagamento(ordine, utente, payment);
    }
}