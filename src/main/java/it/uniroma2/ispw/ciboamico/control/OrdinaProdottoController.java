package it.uniroma2.ispw.ciboamico.control;

import it.uniroma2.ispw.ciboamico.bootstrap.ApplicationModeManager;
import it.uniroma2.ispw.ciboamico.bean.OrdineBean;
import it.uniroma2.ispw.ciboamico.bean.ProdottoBean;
import it.uniroma2.ispw.ciboamico.entity.Prodotto;
import it.uniroma2.ispw.ciboamico.exception.BusinessValidationException;
import it.uniroma2.ispw.ciboamico.exception.DAOException;
import it.uniroma2.ispw.ciboamico.enums.ExceptionMessagesEnum;
import it.uniroma2.ispw.ciboamico.enums.UserErrorMessagesEnum;
import it.uniroma2.ispw.ciboamico.persistence.dao.ProdottoDAO;
import it.uniroma2.ispw.ciboamico.persistence.factory.DAOFactory;

import java.util.List;

/**
 * Controller di UC-04 Ordina un Prodotto (stile a un livello di controllo).
 *
 * <p>Coordina la logica di business del caso d'uso base (verifica
 * disponibilità, costruzione leggera del checkout). Le <em>estensioni</em>
 * del caso d'uso sono delegate a sottocontroller dedicati:
 * {@link ApplicaBuonoPromozionaleController} (estensione 4a) e
 * {@link PagamentoController} (passo 6 / estensione 6a), di cui il Facade
 * espone le operazioni direttamente, senza re-esporsi sul controller
 * principale (niente pass-through / indirection inutile). La view (boundary)
 * delega le operazioni di dominio esclusivamente tramite Scambi su Bean
 * (BCE).</p>
 *
 * <p>Controller applicativo <b>state-less</b>: riceve i bean già costruiti
 * dal controller di presentazione e non tocca la sessione (di pertinenza del Facade).</p>
 */
public class OrdinaProdottoController {

    private final ProdottoDAO prodottoDAO;

    public OrdinaProdottoController(DAOFactory factory) {
        this.prodottoDAO = factory.getProdottoDAO();
    }

    /** Costruttore no-arg: factory risolta dal gestore della modalità. */
    public OrdinaProdottoController() {
        this(ApplicationModeManager.getInstance().getDAOFactory());
    }

    /** Catalogo dei prodotti disponibili come Bean. */
    public List<ProdottoBean> getProdottiDisponibili() throws DAOException {
        return prodottoDAO.findAll().stream()
                .map(p -> {
                    ProdottoBean bean = new ProdottoBean();
                    try {
                        bean.setNome(p.getNome());
                        bean.setPrezzo(p.getPrezzo());
                        bean.setQuantita((double) p.getQuantitaDisponibile());
                    } catch (BusinessValidationException e) {
                        // Le invarianti di Prodotto (Info Expert) garantiscono dati
                        // non-null/positivi: qui non può scattare; per sicurezza non si
                        // mascherano errori, si rilancia come stato invalido.
                        throw new IllegalStateException("Prodotto del catalogo con dati inconsistenti", e);
                    }
                    return bean;
                })
                .toList();
    }

    /**
     * Avvia il checkout per il prodotto selezionato: verifica disponibilità
     * (BR-03) e valorizza l'ordine in checkout con il prezzo pieno. Riceve il
     * bean già costruito dal controller di presentazione via
     * {@link OrdineBean#fromCheckout(String)} e non tocca {@code SessionManager}
     * (la sessione è di competenza del Facade che orchestra lo use case).
     *
     * @param inCorso ordine in checkout costruito dal controller di presentazione
     * @return l'ordine valorizzato col totale (prezzo pieno, prima degli sconti)
     * @throws BusinessValidationException se il prodotto non è selezionato/disponibile
     */
    public OrdineBean avviaCheckout(OrdineBean inCorso)
            throws BusinessValidationException, DAOException {
        inCorso.validate();
        String nomeProdotto = inCorso.getNomeProdotto();
        // Lookup mirato (no findAll + filtro): l'entità è l'Information Expert
        // per la sua disponibilità (BR-03). Il checkout di un prodotto esaurito
        // è rifiutato qui, coerente con l'estensione 2a (out of stock).
        Prodotto prodotto = prodottoDAO.findByNome(nomeProdotto);
        if (prodotto == null) {
            throw new BusinessValidationException(
                    UserErrorMessagesEnum.PRODOTTO_NON_DISPONIBILE_MSG.message,
                    ExceptionMessagesEnum.PRODOTTO_NON_DISPONIBILE.message,
                    "ERR-PRODOTTO-NON-DISPONIBILE");
        }
        if (prodotto.getQuantitaDisponibile() <= 0) {
            throw new BusinessValidationException(
                    UserErrorMessagesEnum.PRODOTTO_NON_DISPONIBILE_MSG.message,
                    ExceptionMessagesEnum.PRODOTTO_NON_DISPONIBILE.message,
                    "ERR-PRODOTTO-NON-DISPONIBILE");
        }
        inCorso.setTotale(prodotto.getPrezzo());
        return inCorso;
    }
}
