package it.uniroma2.ispw.ciboamico.pattern.singleton;

import it.uniroma2.ispw.ciboamico.bean.UtenteBean;
import it.uniroma2.ispw.ciboamico.bean.OrdineBean;

/**
 * Singleton: custode dell'utente loggato e del checkout in corso (UC-04).
 * I controller applicativi sono stateless e leggono la sessione da qui.
 * L'istanza è creata lazy e thread-safe con {@code synchronized} (singleton
 * classico), senza ricorrere a commenti/soppressioni per SonarCloud.
 */
public final class SessionManager {

    private static SessionManager instance;

    private UtenteBean loggedUser;
    private OrdineBean ordineInCorso;

    private SessionManager() { }

    /** Istanza unica (lazy, thread-safe via synchronized). */
    public static synchronized SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    public UtenteBean getLoggedUser() { return loggedUser; }
    public void setLoggedUser(UtenteBean loggedUser) { this.loggedUser = loggedUser; }

    /**
     * Chiude la sessione: pulisce utente loggato e anche l'eventuale checkout
     * in corso, così un nuovo utente non eredita l'ordine del precedente.
     */
    public void logout() {
        this.loggedUser = null;
        this.ordineInCorso = null;
    }

    /** Ordine in checkout UC-04 (contiene il totale, scontato o pieno). */
    public OrdineBean getOrdineInCorso() { return ordineInCorso; }
    public void setOrdineInCorso(OrdineBean ordineInCorso) { this.ordineInCorso = ordineInCorso; }
}
