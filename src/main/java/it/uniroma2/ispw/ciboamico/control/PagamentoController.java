package it.uniroma2.ispw.ciboamico.control;

import it.uniroma2.ispw.ciboamico.bean.OrdineBean;
import it.uniroma2.ispw.ciboamico.bean.PaymentInfoBean;
import it.uniroma2.ispw.ciboamico.bean.UtenteBean;
import it.uniroma2.ispw.ciboamico.entity.Ordine;
import it.uniroma2.ispw.ciboamico.entity.Prodotto;
import it.uniroma2.ispw.ciboamico.entity.Utente;
import it.uniroma2.ispw.ciboamico.entity.VoceOrdine;
import it.uniroma2.ispw.ciboamico.exception.BusinessValidationException;
import it.uniroma2.ispw.ciboamico.pattern.factory.OrdineLazyFactory;
import it.uniroma2.ispw.ciboamico.pattern.observer.OrdineEvent;
import it.uniroma2.ispw.ciboamico.pattern.observer.OrdineEventPublisher;
import it.uniroma2.ispw.ciboamico.pattern.payment.PaymentGateway;
import it.uniroma2.ispw.ciboamico.pattern.payment.PaymentGatewayFactory;
import it.uniroma2.ispw.ciboamico.pattern.payment.PaymentRejectedException;
import it.uniroma2.ispw.ciboamico.pattern.singleton.SessionManager;
import it.uniroma2.ispw.ciboamico.persistence.dao.OrdineDAO;
import it.uniroma2.ispw.ciboamico.persistence.dao.ProdottoDAO;
import it.uniroma2.ispw.ciboamico.persistence.factory.DAOFactory;

/**
 * Controller dello Use Case estensione "Pay" (passo 6 / estensione 6a di
 * UC-04 Ordina un Prodotto).
 *
 * <p>Separa l'autorizzazione dell'addebito dal successivo submit dell'ordine
 * dal controller principale di UC-04, rendendo esplicita l'estensione del
 * pagamento nel modello dei casi d'uso. Il controller principale lo istanzia
 * on-demand (GRASP: Low Coupling), come nel pattern di riferimento. La
 * boundary scambia esclusivamente Bean (BCE): da un lato {@link OrdineBean}
 * e {@link UtenteBean}, dall'altro i {@link PaymentInfoBean}.</p>
 */
public class PagamentoController {

    private final OrdineDAO ordineDAO;
    private final ProdottoDAO prodottoDAO;
    private final PaymentGateway paymentGateway;

    /**
     * Costruttore principale: riceve la {@link DAOFactory} attiva (testabile).
     *
     * @param factory factory di persistenza da cui risolvere i DAO
     */
    public PagamentoController(DAOFactory factory) {
        this.ordineDAO = factory.getOrdineDAO();
        this.prodottoDAO = factory.getProdottoDAO();
        this.paymentGateway = PaymentGatewayFactory.createGateway();
    }

    /** Costruttore no-arg: factory risolta dal gestore della modalità. */
    public PagamentoController() {
        this(it.uniroma2.ispw.ciboamico.bootstrap.ApplicationModeManager.getInstance().getDAOFactory());
    }

    /**
     * Completa il checkout dai dati carta grezzi (vista): costruisce il
     * {@link PaymentInfoBean}, autorizza l'addebito (passo 6/6a) e sottomette.
     *
     * @param ordine       ordine in corso (dalla sessione)
     * @param utente       utente autenticato
     * @param carta        numero di carta
     * @param intestatario intestatario della carta
     * @param scadenza     scadenza della carta
     * @param cvv          codice di sicurezza
     * @return OrdineBean con l'esito del pagamento
     */
    public OrdineBean processaPagamento(OrdineBean ordine, UtenteBean utente,
                                        String carta, String intestatario,
                                        String scadenza, String cvv)
            throws BusinessValidationException, it.uniroma2.ispw.ciboamico.exception.DAOException {
        if (ordine == null || ordine.getTotale() == null) {
            throw new BusinessValidationException(
                    "Nessun ordine in checkout.",
                    "processaPagamento chiamato senza ordine in corso.",
                    "ERR-ORDINE-MANCANTE");
        }
        PaymentInfoBean payment = new PaymentInfoBean();
        payment.setImportoInCent(Math.round(ordine.getTotale() * 100));
        payment.setNumeroCarta(carta);
        payment.setIntestatario(intestatario);
        payment.setScadenza(scadenza);
        payment.setCvv(cvv);

        OrdineBean risultato = processaPagamento(ordine, utente, payment);
        SessionManager.getInstance().setOrdineInCorso(null);
        return risultato;
    }

    /**
     * Autorizza l'addebito (passo 6 / estensione 6a) e sottomette l'ordine.
     * Variante usata dal Facade e dai test, con {@link PaymentInfoBean} già
     * costruito.
     *
     * @param bean    ordine da sottomettere dopo l'autorizzazione
     * @param utente  utente autenticato
     * @param payment dati di pagamento validati
     * @return OrdineBean con l'esito della transazione
     */
    public OrdineBean processaPagamento(OrdineBean bean, UtenteBean utente, PaymentInfoBean payment)
            throws BusinessValidationException, it.uniroma2.ispw.ciboamico.exception.DAOException {
        if (payment == null || payment.getImportoInCent() <= 0) {
            throw new BusinessValidationException("Dati di pagamento non validi");
        }
        try {
            paymentGateway.autorizza(payment.getImportoInCent());
        } catch (PaymentRejectedException e) {
            throw new BusinessValidationException(e.getMessage());
        }
        return submitOrdine(bean, utente);
    }

    /**
     * Flusso UC-04: verifica disponibilità → riepilogo → conferma → ordine
     * CREATED + notifica. Invocato dopo l'autorizzazione dell'addebito.
     *
     * @param bean   ordine da sottomettere
     * @param utente utente autenticato
     * @return OrdineBean con id, totale e stato dell'ordine creato
     */
    public OrdineBean submitOrdine(OrdineBean bean, UtenteBean utente)
            throws BusinessValidationException, it.uniroma2.ispw.ciboamico.exception.DAOException {
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

        ordineDAO.save(ordine);
        // NOTIFICA ATTIVA (Pattern Observer): il control pubblica l'evento di
        // conferma sul publisher singleton, il quale notifica i listener registrati
        // (compratore e venditore) che ricevono il DTO OrdineEvent in sola lettura,
        // mai l'entità.
        OrdineEventPublisher.getInstance().notifyOrdineConfermato(
                new OrdineEvent(ordine.getIdOrdine(), compratore.getEmail(), ordine.getTotale()));

        OrdineBean risultato = new OrdineBean();
        risultato.setIdOrdine(ordine.getIdOrdine());
        risultato.setTotale(ordine.getTotale());
        risultato.setStato(ordine.getStato().name());
        return risultato;
    }
}
