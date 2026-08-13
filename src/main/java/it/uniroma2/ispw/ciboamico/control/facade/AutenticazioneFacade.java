package it.uniroma2.ispw.ciboamico.control.facade;

import it.uniroma2.ispw.ciboamico.bean.AutenticazioneBean;
import it.uniroma2.ispw.ciboamico.bean.UtenteBean;
import it.uniroma2.ispw.ciboamico.control.AutenticazioneController;
import it.uniroma2.ispw.ciboamico.exception.AutenticazioneException;
import it.uniroma2.ispw.ciboamico.exception.DAOException;
import it.uniroma2.ispw.ciboamico.pattern.singleton.SessionManager;
import it.uniroma2.ispw.ciboamico.persistence.factory.DAOFactory;

/**
 * Facade (GoF - Structural) dello Use Case "Login" (UC-11).
 *
 * <p>Espone alla boundary (GUI e CLI) un'unica interfaccia semplificata per
 * l'autenticazione. Come il Facade di checkout, <b>orchestra</b> il {@link AutenticazioneController} e gestisce la sessione
 * in {@link SessionManager}: riceve il bean credenziali già costruito dalla
 * boundary (conversione esterno→interno a carico della View), non
 * costruisce bean al suo interno.</p>
 *
 * <p>Il client (boundary) comunica con il sottosistema di business <b>solo</b>
 * attraverso il Facade, scambiando esclusivamente Bean (BCE).</p>
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

    /**
     * Autentica un utente (UC-11): delega al controller la verifica delle
     * credenziali (bean già costruito dalla view) e, a buon fine, aggiorna la
     * sessione con l'utente autenticato. Orchestrazione e gestione stato nel
     * Facade, controller applicativo state-less.
     *
     * @param credenziali credenziali inserite nella view (formato interno)
     * @return UtenteBean autenticato
     * @throws AutenticazioneException se le credenziali non sono corrette
     * @throws DAOException se l'accesso ai dati fallisce
     */
    public UtenteBean login(AutenticazioneBean credenziali)
            throws AutenticazioneException, DAOException {
        UtenteBean autenticato = controller.login(credenziali);
        SessionManager.getInstance().setLoggedUser(autenticato);
        return autenticato;
    }

    /** Termina la sessione dell'utente autenticato. */
    public void logout() {
        SessionManager.getInstance().logout();
    }
}
