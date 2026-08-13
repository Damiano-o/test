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

// Facade (GoF - Structural) per lo use case UC-04 "Ordina un

public final class OrdinaProdottoFacade {

    private final OrdinaProdottoController controller;
    private final PagamentoController pagamentoController;
    private final ApplicaBuonoPromozionaleController buonoController;

    public OrdinaProdottoFacade(OrdinaProdottoController controller,
                                PagamentoController pagamentoController,
                                ApplicaBuonoPromozionaleController buonoController) {
        this.controller = controller;
        this.pagamentoController = pagamentoController;
        this.buonoController = buonoController;
    }

    // Facade costruita con i controller iniettati

    public OrdinaProdottoFacade(OrdinaProdottoController controller) {
        this(controller,
                new PagamentoController(),
                new ApplicaBuonoPromozionaleController());
    }

    public OrdinaProdottoFacade(DAOFactory factory) {
        this(new OrdinaProdottoController(factory),
                new PagamentoController(factory),
                new ApplicaBuonoPromozionaleController(factory));
    }

    public OrdinaProdottoFacade() {
        this(new OrdinaProdottoController());
    }

    public List<ProdottoBean> getProdottiDisponibili() throws DAOException {
        return controller.getProdottiDisponibili();
    }

    // controller di presentazione ha già costruito l'ordine in

    public OrdineBean avviaCheckout(OrdineBean inCorso)
            throws BusinessValidationException, DAOException {
        OrdineBean avviato = controller.avviaCheckout(inCorso);
        SessionManager.getInstance().setOrdineInCorso(avviato);
        return avviato;
    }

    // l'ordine in checkout (costruito dal controller di presentazione)

    public OrdineBean applicaBuono(String codiceBuono, OrdineBean bean, UtenteBean utente)
            throws BusinessValidationException, DAOException {
        OrdineBean scontato = buonoController.applicaBuonoPromozionale(codiceBuono, bean, utente);
        SessionManager.getInstance().setOrdineInCorso(scontato);
        return scontato;
    }

    // PaymentInfoBean già costruito dal controller di presentazione

    public OrdineBean processaPagamento(OrdineBean ordine, UtenteBean utente, PaymentInfoBean payment)
            throws BusinessValidationException, DAOException {
        return pagamentoController.processaPagamento(ordine, utente, payment);
    }
}