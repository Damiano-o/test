package it.uniroma2.ispw.ciboamico.control;

import it.uniroma2.ispw.ciboamico.bootstrap.ApplicationModeManager;
import it.uniroma2.ispw.ciboamico.bean.AutenticazioneBean;
import it.uniroma2.ispw.ciboamico.bean.UtenteBean;
import it.uniroma2.ispw.ciboamico.entity.Utente;
import it.uniroma2.ispw.ciboamico.entity.RuoloVenditore;
import it.uniroma2.ispw.ciboamico.enums.ExceptionMessagesEnum;
import it.uniroma2.ispw.ciboamico.enums.UserErrorMessagesEnum;
import it.uniroma2.ispw.ciboamico.exception.AutenticazioneException;
import it.uniroma2.ispw.ciboamico.exception.DAOException;
import it.uniroma2.ispw.ciboamico.persistence.dao.UtenteDAO;
import it.uniroma2.ispw.ciboamico.persistence.factory.DAOFactory;

// Controller di UC-11 Autenticazione (incluso dagli altri UC)

public class AutenticazioneController {

    private final UtenteDAO utenteDAO;

    public AutenticazioneController(DAOFactory factory) {
        this.utenteDAO = factory.getUtenteDAO();
    }

    public AutenticazioneController() {
        this(ApplicationModeManager.getInstance().getDAOFactory());
    }

    // Tenta l'accesso: verifica esistenza account e password (Request del bean credenziali gi...

    public UtenteBean login(AutenticazioneBean credenziali)
            throws AutenticazioneException, DAOException {
        return autentica(credenziali);
    }

    private UtenteBean autentica(AutenticazioneBean credenziali)
            throws AutenticazioneException, DAOException {
        Utente utente = utenteDAO.findByEmail(credenziali.getEmail());
        if (utente == null || !utente.checkPassword(credenziali.getPassword())) {
            throw new AutenticazioneException(
                    UserErrorMessagesEnum.WRONG_PASSWORD_MSG.message,
                    ExceptionMessagesEnum.WRONG_PASSWORD.message + " (" + mask(credenziali.getEmail()) + ")",
                    "ERR-CREDENZIALI");
        }

        // Bean di output (entity→bean, come getProdottiDisponibili): la
        // costruzione dell'UtenteBean risultante è ammessa nel controller
        // (formato interno→esterno); la sessione la gestisce il Facade.
        // Il ruolo attivo è espresso in forma SEMANTICA ("CLIENTE"/"VENDITORE"),
        // NON come nome della classe di implementazione: l'entity è l'Information
        // Expert del proprio ruolo (Liskov: il bean non deve conoscere la gerarchia).
        UtenteBean bean = new UtenteBean();
        bean.setUsername(utente.getNome());
        bean.setEmail(utente.getEmail());
        bean.setRuoloAttivo(ruoloAttivoDi(utente));
        return bean;
    }

    // Traduce i ruoli dell'utente in una etichetta semantica stabile per la presentazione

    private String ruoloAttivoDi(Utente utente) {
        if (utente.isVenditoreApprovato()) {
            return "VENDITORE";
        }
        if (utente.haRuolo(RuoloVenditore.class)) {
            return "VENDITORE";
        }
        return "CLIENTE";
    }

    private String mask(String email) {
        if (email == null) {
            return "null";
        }
        int at = email.indexOf('@');
        if (at <= 1) {
            return "***@" + (at >= 0 ? email.substring(at + 1) : "?");
        }
        return email.substring(0, 2) + "***@" + email.substring(at + 1);
    }
}
