package it.uniroma2.ispw.ciboamico.control;

import it.uniroma2.ispw.ciboamico.bootstrap.ApplicationModeManager;
import it.uniroma2.ispw.ciboamico.bean.OrdineBean;
import it.uniroma2.ispw.ciboamico.bean.UtenteBean;
import it.uniroma2.ispw.ciboamico.entity.BuonoPromozionale;
import it.uniroma2.ispw.ciboamico.entity.Prodotto;
import it.uniroma2.ispw.ciboamico.entity.Utente;
import it.uniroma2.ispw.ciboamico.exception.BusinessValidationException;
import it.uniroma2.ispw.ciboamico.exception.DAOException;
import it.uniroma2.ispw.ciboamico.enums.ExceptionMessagesEnum;
import it.uniroma2.ispw.ciboamico.enums.UserErrorMessagesEnum;
import it.uniroma2.ispw.ciboamico.persistence.dao.BuonoDAO;
import it.uniroma2.ispw.ciboamico.persistence.dao.ProdottoDAO;
import it.uniroma2.ispw.ciboamico.persistence.dao.UtenteDAO;
import it.uniroma2.ispw.ciboamico.persistence.factory.DAOFactory;

// Controller dello Use Case estensione "Applica Buono

public class ApplicaBuonoPromozionaleController {

    private final BuonoDAO buonoDAO;
    private final ProdottoDAO prodottoDAO;
    private final UtenteDAO utenteDAO;

    // Costruttore principale: riceve la DAOFactory attiva (testabile)

    public ApplicaBuonoPromozionaleController(DAOFactory factory) {
        this.buonoDAO = factory.getBuonoDAO();
        this.prodottoDAO = factory.getProdottoDAO();
        this.utenteDAO = factory.getUtenteDAO();
    }

    public ApplicaBuonoPromozionaleController() {
        this(ApplicationModeManager.getInstance().getDAOFactory());
    }

    // Estensione 4a: applica un buono valido all'ordine corrente

    public OrdineBean applicaBuonoPromozionale(String codiceBuono, OrdineBean bean, UtenteBean utente)
            throws BusinessValidationException, DAOException {
        if (codiceBuono == null || codiceBuono.isBlank()) {
            throw new BusinessValidationException(
                    UserErrorMessagesEnum.BUONO_CODICE_OBBLIGATORIO_MSG.message,
                    ExceptionMessagesEnum.BUONO_CODICE_OBBLIGATORIO.message,
                    "ERR-BUONO-CODICE");
        }
        if (utente == null) {
            throw new IllegalStateException("Utente non autenticato");
        }
        bean.validate();

        BuonoPromozionale buono = buonoDAO.findByCodice(codiceBuono);
        if (buono == null) {
            throw new BusinessValidationException(
                    UserErrorMessagesEnum.BUONO_NON_VALIDO_MSG.message,
                    ExceptionMessagesEnum.BUONO_NON_VALIDO.message,
                    "ERR-BUONO-NON-VALIDO");
        }
        if (!buono.isValido()) {
            throw new BusinessValidationException(
                    UserErrorMessagesEnum.BUONO_NON_ATTIVO_MSG.message,
                    ExceptionMessagesEnum.BUONO_NON_ATTIVO.message,
                    "ERR-BUONO-NON-ATTIVO");
        }
        if (utente.getEmail() != null && haGiaUsatoBuono(utente.getEmail(), codiceBuono)) {
            throw new BusinessValidationException(
                    UserErrorMessagesEnum.BUONO_GIÀ_USATO_MSG.message,
                    ExceptionMessagesEnum.BUONO_GIÀ_USATO.message,
                    "ERR-BUONO-GIA-USATO");
        }

        Prodotto prodotto = prodottoDAO.findByNome(bean.getNomeProdotto());
        if (prodotto == null || prodotto.getVenditore() == null
                || prodotto.getVenditore().getUtente() == null) {
            throw new IllegalStateException("Prodotto o venditore non risolvibile");
        }
        String emailVenditoreBuono = buono.getVenditore().getUtente() != null
                ? buono.getVenditore().getUtente().getEmail()
                : buono.getVenditore().getRecapito();
        String emailVenditoreOrdine = prodotto.getVenditore().getUtente().getEmail();
        if (!emailVenditoreBuono.equals(emailVenditoreOrdine)) {
            throw new BusinessValidationException(
                    UserErrorMessagesEnum.BUONO_VENDITORE_ERRATO_MSG.message,
                    ExceptionMessagesEnum.BUONO_VENDITORE_ERRATO.message,
                    "ERR-BUONO-VENDITORE");
        }

        Utente compratore = (utente.getEmail() != null) ? utenteDAO.findByEmail(utente.getEmail()) : null;
        if (compratore == null) {
            compratore = new Utente(utente.getUsername(), utente.getEmail(), "");
        }

        // Information Expert: il buono applica da sé lo sconto sul prezzo del
        // prodotto (non serve creare un Ordine completo per mostrare il totale).
        double totaleScontato = buono.applicaSconto(prodotto.getPrezzo());

        if (utente.getEmail() != null) {
            Utente persona = utenteDAO.findByEmail(utente.getEmail());
            if (persona != null && persona != compratore) {
                persona.registraBuonoUtilizzato(buono.getCodice());
                utenteDAO.save(persona);
            } else if (compratore != null && compratore.getEmail() != null) {
                compratore.registraBuonoUtilizzato(buono.getCodice());
                utenteDAO.save(compratore);
            }
        }

        OrdineBean risultato = new OrdineBean();
        risultato.setIdOrdine(bean.getIdOrdine());
        risultato.setNomeProdotto(bean.getNomeProdotto());
        risultato.setTotale(totaleScontato);
        risultato.setCodiceBuono(buono.getCodice());
        return risultato;
    }

    private boolean haGiaUsatoBuono(String email, String codiceBuono)
            throws DAOException {
        Utente u = utenteDAO.findByEmail(email);
        return u != null && u.haUsatoBuono(codiceBuono);
    }
}
