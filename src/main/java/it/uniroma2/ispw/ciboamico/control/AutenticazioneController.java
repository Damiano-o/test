package it.uniroma2.ispw.ciboamico.control;

import it.uniroma2.ispw.ciboamico.bean.AutenticazioneBean;
import it.uniroma2.ispw.ciboamico.bean.UtenteBean;
import it.uniroma2.ispw.ciboamico.entity.Utente;
import it.uniroma2.ispw.ciboamico.enums.ExceptionMessagesEnum;
import it.uniroma2.ispw.ciboamico.enums.UserErrorMessagesEnum;
import it.uniroma2.ispw.ciboamico.exception.AutenticazioneException;
import it.uniroma2.ispw.ciboamico.pattern.singleton.SessionManager;
import it.uniroma2.ispw.ciboamico.persistence.dao.UtenteDAO;
import it.uniroma2.ispw.ciboamico.persistence.factory.DAOFactory;

/**
 * Controller di UC-11 Autenticazione (incluso dagli altri UC).
 *
 * <p>Gestisce la verifica delle credenziali (delegata alla Entity Utente,
 * Information Expert) e della sessione in {@link SessionManager}. La
 * validazione sintattica avviene nei setter dell'{@link AutenticazioneBean}
 * (Fail Fast); qui resta la verifica semantica (esistenza account + password).
 * È il controller unico: la boundary (LoginView) delega a questo senza livelli
 * intermedi.</p>
 */
public class AutenticazioneController {

    private final UtenteDAO utenteDAO;

    public AutenticazioneController(DAOFactory factory) {
        this.utenteDAO = factory.getUtenteDAO();
    }

    /** Costruttore no-arg: factory risolta dal gestore della modalità. */
    public AutenticazioneController() {
        this(it.uniroma2.ispw.ciboamico.bootstrap.ApplicationModeManager.getInstance().getDAOFactory());
    }

    /** Tenta l'accesso con le credenziali inserite (Bean). */
    public UtenteBean login(AutenticazioneBean credenziali)
            throws AutenticazioneException, it.uniroma2.ispw.ciboamico.exception.DAOException {
        return autentica(credenziali);
    }

    /** Tenta l'accesso con email e password come stringhe del form. */
    public UtenteBean login(String email, String password)
            throws AutenticazioneException, it.uniroma2.ispw.ciboamico.exception.DAOException {
        AutenticazioneBean credenziali = new AutenticazioneBean();
        credenziali.setEmail(email);
        credenziali.setPassword(password);
        return autentica(credenziali);
    }

    /** Autentica email+password già validate: lookup DAO e verifica password. */
    private UtenteBean autentica(AutenticazioneBean credenziali)
            throws AutenticazioneException, it.uniroma2.ispw.ciboamico.exception.DAOException {
        Utente utente = utenteDAO.findByEmail(credenziali.getEmail());
        if (utente == null || !utente.checkPassword(credenziali.getPassword())) {
            throw new AutenticazioneException(
                    UserErrorMessagesEnum.WRONG_PASSWORD_MSG.message,
                    ExceptionMessagesEnum.WRONG_PASSWORD.message + " (" + mask(credenziali.getEmail()) + ")",
                    "ERR-CREDENZIALI");
        }

        UtenteBean bean = new UtenteBean();
        bean.setUsername(utente.getNome());
        bean.setEmail(utente.getEmail());
        bean.setRuoloAttivo(utente.getRuoli().isEmpty() ? "CLIENTE" : utente.getRuoli().get(0).getClass().getSimpleName());
        SessionManager.getInstance().setLoggedUser(bean);
        return bean;
    }

    /** Offusca l'email nei log (privacy). */
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

    /** Termina la sessione corrente. */
    public void logout() {
        SessionManager.getInstance().logout();
    }
}
