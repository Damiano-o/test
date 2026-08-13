package it.uniroma2.ispw.ciboamico.control.facade;

import it.uniroma2.ispw.ciboamico.bean.AutenticazioneBean;
import it.uniroma2.ispw.ciboamico.bean.UtenteBean;
import it.uniroma2.ispw.ciboamico.control.AutenticazioneController;
import it.uniroma2.ispw.ciboamico.exception.AutenticazioneException;
import it.uniroma2.ispw.ciboamico.exception.DAOException;
import it.uniroma2.ispw.ciboamico.pattern.singleton.SessionManager;
import it.uniroma2.ispw.ciboamico.persistence.factory.DAOFactory;

// Facade (GoF - Structural) dello Use Case "Login" (UC-11)

public final class AutenticazioneFacade {

    private final AutenticazioneController controller;

    public AutenticazioneFacade(AutenticazioneController controller) {
        this.controller = controller;
    }

    public AutenticazioneFacade(DAOFactory factory) {
        this(new AutenticazioneController(factory));
    }

    public AutenticazioneFacade() {
        this(new AutenticazioneController());
    }

    // Autentica un utente (UC-11): delega al controller la verifica

    public UtenteBean login(AutenticazioneBean credenziali)
            throws AutenticazioneException, DAOException {
        UtenteBean autenticato = controller.login(credenziali);
        SessionManager.getInstance().setLoggedUser(autenticato);
        return autenticato;
    }

    public void logout() {
        SessionManager.getInstance().logout();
    }
}
