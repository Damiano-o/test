package it.uniroma2.ispw.ciboamico.control.facade;

import it.uniroma2.ispw.ciboamico.bean.AutenticazioneBean;
import it.uniroma2.ispw.ciboamico.bean.UtenteBean;
import it.uniroma2.ispw.ciboamico.control.AutenticazioneController;
import it.uniroma2.ispw.ciboamico.exception.AutenticazioneException;
import it.uniroma2.ispw.ciboamico.persistence.factory.DAOFactory;

/**
 * Facade (GoF - Structural) dello Use Case "Login" (UC-11).
 *
 * <p>Espone alla boundary (GUI e CLI) un'unica interfaccia semplificata per
 * l'autenticazione. È <b>stateless</b>: non mantiene stato, delega la logica
 * al {@link AutenticazioneController} (GRASP Controller).</p>
 *
 * <p>Come da lezione sul pattern Façade, il <em>client</em> (boundary)
 * comunica con il sottosistema di business <b>solo</b> attraverso il Facade:
 * non ha accesso diretto al controller applicativo. La boundary scambia
 * esclusivamente Bean (BCE).</p>
 */
public final class AutenticazioneFacade {

    private final AutenticazioneController controller;

    /** Facade costruita con il controller iniettato (testabile). */
    public AutenticazioneFacade(AutenticazioneController controller) {
        this.controller = controller;
    }

    /** Costruttore di comodo: risolve la factory attiva dalla modalità. */
    public AutenticazioneFacade(DAOFactory factory) {
        this(new AutenticazioneController(factory));
    }

    /** Costruttore di comodo per la boundary: usa la factory attiva di runtime. */
    public AutenticazioneFacade() {
        this(new AutenticazioneController());
    }

    /** Autentica un utente a partire da email e password (UC-11). */
    public UtenteBean login(String email, String password)
            throws AutenticazioneException, it.uniroma2.ispw.ciboamico.exception.DAOException {
        return controller.login(email, password);
    }

    /** Autentica un utente a partire da un bean già costruito. */
    public UtenteBean login(AutenticazioneBean credenziali)
            throws AutenticazioneException, it.uniroma2.ispw.ciboamico.exception.DAOException {
        return controller.login(credenziali);
    }

    /** Termina la sessione dell'utente autenticato. */
    public void logout() {
        controller.logout();
    }
}
