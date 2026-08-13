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

// Controller di UC-04 Ordina un Prodotto

public class OrdinaProdottoController {

    private final ProdottoDAO prodottoDAO;

    public OrdinaProdottoController(DAOFactory factory) {
        this.prodottoDAO = factory.getProdottoDAO();
    }

    public OrdinaProdottoController() {
        this(ApplicationModeManager.getInstance().getDAOFactory());
    }

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

    // Avvia il checkout per il prodotto selezionato: verifica

    public OrdineBean avviaCheckout(OrdineBean inCorso)
            throws BusinessValidationException, DAOException {
        inCorso.validate();
        String nomeProdotto = inCorso.getNomeProdotto();
        // Lookup mirato (no findAll + filtro): l'entità è l'Information
        // per la sua disponibilità (BR-03). Il checkout di un prodotto
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
        // Se c'è già un buono applicato (totale scontato valorizzato), non
        // sovrascriverlo col prezzo pieno: lo sconto resta valido.
        if (inCorso.getCodiceBuono() == null || inCorso.getCodiceBuono().isBlank()) {
            inCorso.setTotale(prodotto.getPrezzo());
        }
        return inCorso;
    }
}
