package it.uniroma2.ispw.ciboamico.bootstrap;

import it.uniroma2.ispw.ciboamico.config.AppConfig;
import it.uniroma2.ispw.ciboamico.persistence.factory.DAOFactory;
import it.uniroma2.ispw.ciboamico.persistence.factory.DemoDAOFactory;
import it.uniroma2.ispw.ciboamico.persistence.factory.FSDAOFactory;
import it.uniroma2.ispw.ciboamico.persistence.factory.JDBCDAOFactory;

// Singleton: seleziona la modalità applicativa a runtime (JDBC | FS | DEMO) e fornisce la...

public final class ApplicationModeManager {

    private static ApplicationModeManager instance;

    public static final String MODE_JDBC = AppConfig.MODE_JDBC;
    public static final String MODE_FS = AppConfig.MODE_FS;
    public static final String MODE_DEMO = AppConfig.MODE_DEMO;

    private String activeMode = MODE_DEMO; // default: demo in-memory (milestone M1)
    private DAOFactory factory;            // cache: stessa istanza per tutte le view

    private ApplicationModeManager() {
        // NFR-01: legge la modalità di persistenza da AppConfig (unico punto
        // d'accesso a config.properties), senza ricompilare; fallback a DEMO.
        String mode = AppConfig.getInstance().getPersistenceType();
        try {
            setActiveMode(mode);
        } catch (IllegalArgumentException e) {
            // configurazione non valida: non bloccare l'avvio, resta DEMO
            this.activeMode = MODE_DEMO;
            this.factory = null;
        }
    }

    public static synchronized ApplicationModeManager getInstance() {
        if (instance == null) {
            instance = new ApplicationModeManager();
        }
        return instance;
    }

    public String getActiveMode() { return activeMode; }

    public void setActiveMode(String mode) {
        if (!MODE_JDBC.equals(mode) && !MODE_FS.equals(mode) && !MODE_DEMO.equals(mode)) {
            throw new IllegalArgumentException("Modalità sconosciuta: " + mode);
        }
        this.activeMode = mode;
        this.factory = null; // invalidate cache on mode switch
    }

    public DAOFactory getDAOFactory() {
        if (factory == null) {
            factory = switch (activeMode) {
                case MODE_JDBC -> new JDBCDAOFactory();
                case MODE_FS -> new FSDAOFactory();
                default -> new DemoDAOFactory();
            };
        }
        return factory;
    }
}
