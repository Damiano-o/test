package it.uniroma2.ispw.ciboamico.control;

import it.uniroma2.ispw.ciboamico.bootstrap.ApplicationModeManager;
import it.uniroma2.ispw.ciboamico.bean.OrdineBean;
import it.uniroma2.ispw.ciboamico.bean.PaymentInfoBean;
import it.uniroma2.ispw.ciboamico.bean.UtenteBean;
import it.uniroma2.ispw.ciboamico.entity.BuonoPromozionale;
import it.uniroma2.ispw.ciboamico.entity.Ordine;
import it.uniroma2.ispw.ciboamico.entity.Prodotto;
import it.uniroma2.ispw.ciboamico.entity.Utente;
import it.uniroma2.ispw.ciboamico.entity.VoceOrdine;
import it.uniroma2.ispw.ciboamico.exception.BusinessValidationException;
import it.uniroma2.ispw.ciboamico.exception.DAOException;
import it.uniroma2.ispw.ciboamico.enums.ExceptionMessagesEnum;
import it.uniroma2.ispw.ciboamico.enums.UserErrorMessagesEnum;
import it.uniroma2.ispw.ciboamico.pattern.factory.OrdineLazyFactory;
import it.uniroma2.ispw.ciboamico.pattern.observer.OrdineEvent;
import it.uniroma2.ispw.ciboamico.pattern.observer.OrdineEventPublisher;
import it.uniroma2.ispw.ciboamico.pattern.payment.PaymentGateway;
import it.uniroma2.ispw.ciboamico.pattern.payment.PaymentGatewayFactory;
import it.uniroma2.ispw.ciboamico.exception.PaymentRejectedException;
import it.uniroma2.ispw.ciboamico.persistence.dao.BuonoDAO;
import it.uniroma2.ispw.ciboamico.persistence.dao.OrdineDAO;
import it.uniroma2.ispw.ciboamico.persistence.dao.ProdottoDAO;
import it.uniroma2.ispw.ciboamico.persistence.factory.DAOFactory;

// Controller dello Use Case estensione "Pay" (passo 6 / estensione 6a di UC-04 Ordina un...

public class PagamentoController {

    private final OrdineDAO ordineDAO;
    private final ProdottoDAO prodottoDAO;
    private final BuonoDAO buonoDAO;
    private final PaymentGateway paymentGateway;

    // Costruttore principale: riceve la DAOFactory attiva (testabile)

    public PagamentoController(DAOFactory factory) {
        this.ordineDAO = factory.getOrdineDAO();
        this.prodottoDAO = factory.getProdottoDAO();
        this.buonoDAO = factory.getBuonoDAO();
        this.paymentGateway = PaymentGatewayFactory.createGateway();
    }

    public PagamentoController() {
        this(ApplicationModeManager.getInstance().getDAOFactory());
    }

    // Autorizza l'addebito (passo 6 / estensione 6a) e sottomette l'ordine

    public OrdineBean processaPagamento(OrdineBean bean, UtenteBean utente, PaymentInfoBean payment)
            throws BusinessValidationException, DAOException {
        if (bean == null) {
            throw new BusinessValidationException(
                    UserErrorMessagesEnum.ORDINE_MANCANTE_MSG.message,
                    ExceptionMessagesEnum.ORDINE_MANCANTE.message,
                    "ERR-ORDINE-MANCANTE");
        }
        if (payment == null || payment.getImportoInCent() <= 0) {
            throw new BusinessValidationException(
                    UserErrorMessagesEnum.DATI_PAGAMENTO_NON_VALIDI_MSG.message,
                    ExceptionMessagesEnum.DATI_PAGAMENTO_NON_VALIDI.message,
                    "ERR-PAGAMENTO-DATI");
        }
        try {
            paymentGateway.autorizza(payment.getImportoInCent());
        } catch (PaymentRejectedException e) {
            // Exception chaining: si propaga un errore di dominio mantenendo
            // traccia della causa interna.
            throw new BusinessValidationException(e.getMessage(), e);
        }
        return submitOrdine(bean, utente);
    }

    // Flusso UC-04: verifica disponibilità → riepilogo → conferma → ordine CREATED + notifica

    public OrdineBean submitOrdine(OrdineBean bean, UtenteBean utente)
            throws BusinessValidationException, DAOException {
        if (utente == null) {
            throw new IllegalStateException("Utente non autenticato");
        }
        bean.validate();

        Prodotto prodotto = prodottoDAO.findByNome(bean.getNomeProdotto());
        if (prodotto == null) {
            throw new IllegalStateException("Prodotto non disponibile (2.c)");
        }

        // Estensione 2a (out of stock): riduce la disponibilità.
        prodotto.riduciDisponibilita(1);
        prodottoDAO.update(prodotto);

        Utente compratore = new Utente(utente.getUsername(), utente.getEmail(), "");
        Utente venditore = prodotto.getVenditore() != null && prodotto.getVenditore().getUtente() != null
                ? prodotto.getVenditore().getUtente()
                : compratore;

        Ordine ordine = OrdineLazyFactory.getInstance().newOrdine(compratore, venditore);
        ordine.aggiungiVoce(new VoceOrdine(prodotto, 1));

        // Estensione 4a: se al checkout è stato applicato un buono promozionale,
        // lo si applica all'ordine definitivo prima del salvataggio. Ordine.applicaBuono
        // valida (Information Expert) che il buono appartenga al venditore dell'ordine.
        String codiceBuono = bean.getCodiceBuono();
        if (codiceBuono != null && !codiceBuono.isBlank()) {
            BuonoPromozionale buono = buonoDAO.findByCodice(codiceBuono);
            if (buono == null) {
                throw new BusinessValidationException(
                        UserErrorMessagesEnum.BUONO_NON_VALIDO_MSG.message,
                        ExceptionMessagesEnum.BUONO_NON_VALIDO.message,
                        "ERR-BUONO-NON-VALIDO");
            }
            ordine.applicaBuono(buono);
        }

        ordineDAO.save(ordine);
        // NOTIFICA ATTIVA (Pattern Observer): il control pubblica l'evento di
        // conferma sul publisher singleton, il quale notifica i listener registrati
        // (compratore e venditore) che ricevono il DTO OrdineEvent in sola lettura,
        // mai l'entità.
        OrdineEventPublisher.getInstance().notifyOrdineConfermato(
                new OrdineEvent(ordine.getIdOrdine(), compratore.getEmail(),
                        venditore.getEmail(), ordine.getTotale()));

        OrdineBean risultato = new OrdineBean();
        risultato.setIdOrdine(ordine.getIdOrdine());
        risultato.setTotale(ordine.getTotale());
        risultato.setStato(ordine.getStato().name());
        return risultato;
    }
}
