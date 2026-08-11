package it.uniroma2.ispw.ciboamico.control;

import it.uniroma2.ispw.ciboamico.bean.OrdineBean;
import it.uniroma2.ispw.ciboamico.bean.PaymentInfoBean;
import it.uniroma2.ispw.ciboamico.bean.ProdottoBean;
import it.uniroma2.ispw.ciboamico.bean.UtenteBean;
import it.uniroma2.ispw.ciboamico.exception.BusinessValidationException;
import it.uniroma2.ispw.ciboamico.pattern.singleton.SessionManager;
import it.uniroma2.ispw.ciboamico.persistence.dao.ProdottoDAO;
import it.uniroma2.ispw.ciboamico.persistence.factory.DAOFactory;

import java.util.List;

/**
 * Controller di UC-04 Ordina un Prodotto (stile a un livello di controllo).
 *
 * <p>Coordina la logica di business del caso d'uso base (verifica
 * disponibilità, costruzione leggera del checkout) ed espone le
 * <em>estensioni</em> del caso d'uso delegano a sottocontroller dedicati:
 * {@link ApplicaBuonoPromozionaleController} (estensione 4a) e
 * {@link PagamentoController} (passo 6 / estensione 6a). I sottocontroller
 * sono creati on-demand attraverso helper (GRASP: Low Coupling), come nel
 * pattern di riferimento. La view (boundary) delega le operazioni di dominio
 * esclusivamente tramite Scambi su Bean (BCE).</p>
 */
public class OrdinaProdottoController {

    private final DAOFactory factory;
    private final ProdottoDAO prodottoDAO;

    public OrdinaProdottoController(DAOFactory factory) {
        this.factory = factory;
        this.prodottoDAO = factory.getProdottoDAO();
    }

    /** Costruttore no-arg: factory risolta dal gestore della modalità. */
    public OrdinaProdottoController() {
        this(it.uniroma2.ispw.ciboamico.bootstrap.ApplicationModeManager.getInstance().getDAOFactory());
    }

    /**
     * Helper per ottenere il controller dell'estensione buono promozionale.
     * Creato on-demand (stessa factory del controller principale) per mantenere
     * il controller principale stateless.
     *
     * @return nuova istanza del controller del buono
     */
    private ApplicaBuonoPromozionaleController getBuonoController() {
        return new ApplicaBuonoPromozionaleController(factory);
    }

    /**
     * Helper per ottenere il controller dell'estensione pagamento.
     * Creato on-demand (stessa factory del controller principale) per mantenere
     * il controller principale stateless.
     *
     * @return nuova istanza del controller del pagamento
     */
    private PagamentoController getPagamentoController() {
        return new PagamentoController(factory);
    }

    /** Catalogo dei prodotti disponibili come Bean. */
    public List<ProdottoBean> getProdottiDisponibili() throws it.uniroma2.ispw.ciboamico.exception.DAOException {
        return prodottoDAO.findAll().stream()
                .map(p -> {
                    ProdottoBean bean = new ProdottoBean();
                    bean.setNome(p.getNome());
                    bean.setPrezzo(p.getPrezzo());
                    bean.setQuantita((double) p.getQuantitaDisponibile());
                    return bean;
                })
                .toList();
    }

    /**
     * Avvia il checkout per il prodotto selezionato: salva l'ordine in corso
     * in {@link SessionManager} con il prezzo pieno (prima di eventuali sconti).
     *
     * @param nomeProdotto prodotto scelto sulla schermata marketplace
     * @return l'ordine in corso
     * @throws BusinessValidationException se il prodotto non è selezionato
     */
    public OrdineBean avviaCheckout(String nomeProdotto)
            throws BusinessValidationException, it.uniroma2.ispw.ciboamico.exception.DAOException {
        if (nomeProdotto == null || nomeProdotto.isBlank()) {
            throw new BusinessValidationException("Nessun prodotto selezionato.");
        }
        ProdottoBean prodotto = getProdottiDisponibili().stream()
                .filter(p -> nomeProdotto.equals(p.getNome()))
                .findFirst()
                .orElse(null);
        if (prodotto == null) {
            throw new BusinessValidationException("Prodotto non più disponibile.");
        }
        OrdineBean inCorso = new OrdineBean();
        inCorso.setNomeProdotto(prodotto.getNome());
        inCorso.setTotale(prodotto.getPrezzo());
        SessionManager.getInstance().setOrdineInCorso(inCorso);
        return inCorso;
    }

    // ============ Estensione 4a "Applica Buono Promozionale" ============

    /**
     * Applica un buono promozionale all'ordine (estensione 4a). Delega al
     * {@link ApplicaBuonoPromozionaleController}.
     */
    public OrdineBean applicaBuono(String codiceBuono, String nomeProdotto, UtenteBean utente)
            throws BusinessValidationException, it.uniroma2.ispw.ciboamico.exception.DAOException {
        return getBuonoController().applicaBuono(codiceBuono, nomeProdotto, utente);
    }

    /**
     * Estensione 4a: applica un buono valido all'ordine corrente. Delega al
     * {@link ApplicaBuonoPromozionaleController}.
     */
    public OrdineBean applicaBuonoPromozionale(String codiceBuono, OrdineBean bean, UtenteBean utente)
            throws BusinessValidationException, it.uniroma2.ispw.ciboamico.exception.DAOException {
        return getBuonoController().applicaBuonoPromozionale(codiceBuono, bean, utente);
    }

    // ============ Passo 6 / estensione 6a "Pay" ============

    /**
     * Completa il checkout dai dati carta grezzi (passo 6/6a). Delega al
     * {@link PagamentoController}.
     */
    public OrdineBean processaPagamento(OrdineBean ordine, UtenteBean utente,
                                        String carta, String intestatario,
                                        String scadenza, String cvv)
            throws BusinessValidationException, it.uniroma2.ispw.ciboamico.exception.DAOException {
        return getPagamentoController()
                .processaPagamento(ordine, utente, carta, intestatario, scadenza, cvv);
    }

    /**
     * Autorizza l'addebito (passo 6 / estensione 6a) e sottomette. Delega al
     * {@link PagamentoController}.
     */
    public OrdineBean processaPagamento(OrdineBean bean, UtenteBean utente, PaymentInfoBean payment)
            throws BusinessValidationException, it.uniroma2.ispw.ciboamico.exception.DAOException {
        return getPagamentoController().processaPagamento(bean, utente, payment);
    }

    /**
     * Flusso UC-04: verifica disponibilità → riepilogo → conferma → ordine
     * CREATED + notifica. Delega al {@link PagamentoController}.
     */
    public OrdineBean submitOrdine(OrdineBean bean, UtenteBean utente)
            throws BusinessValidationException, it.uniroma2.ispw.ciboamico.exception.DAOException {
        return getPagamentoController().submitOrdine(bean, utente);
    }
}
