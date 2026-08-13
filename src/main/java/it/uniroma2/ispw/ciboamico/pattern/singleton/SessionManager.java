package it.uniroma2.ispw.ciboamico.pattern.singleton;

import it.uniroma2.ispw.ciboamico.bean.UtenteBean;
import it.uniroma2.ispw.ciboamico.bean.OrdineBean;

// Singleton: custode dell'utente loggato e del checkout in corso

public final class SessionManager {

    private static SessionManager instance;

    private UtenteBean loggedUser;
    private OrdineBean ordineInCorso;

    private SessionManager() { }

    public static synchronized SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    public UtenteBean getLoggedUser() { return loggedUser; }
    public void setLoggedUser(UtenteBean loggedUser) { this.loggedUser = loggedUser; }

    // Chiude la sessione: pulisce utente loggato e anche l'eventuale

    public void logout() {
        this.loggedUser = null;
        this.ordineInCorso = null;
    }

    public OrdineBean getOrdineInCorso() { return ordineInCorso; }
    public void setOrdineInCorso(OrdineBean ordineInCorso) { this.ordineInCorso = ordineInCorso; }
}
