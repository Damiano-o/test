package it.uniroma2.ispw.ciboamico.bootstrap;

import it.uniroma2.ispw.ciboamico.persistence.factory.DAOFactory;
import it.uniroma2.ispw.ciboamico.persistence.factory.DemoDAOFactory;
import it.uniroma2.ispw.ciboamico.persistence.factory.FSDAOFactory;
import it.uniroma2.ispw.ciboamico.persistence.factory.JDBCDAOFactory;

/**
 * Singleton (Holder Idiom): seleziona la modalità applicativa a runtime
 * (JDBC | FS | DEMO) e fornisce la DAOFactory corretta — switch senza
 * modificare la logica di business (NFR-01, milestone M1/M2).
 */
public final class ApplicationModeManager {

    public static final String MODE_JDBC = "JDBC";
    public static final String MODE_FS = "FS";
    public static final String MODE_DEMO = "DEMO";

    private String activeMode = MODE_DEMO; // default: demo in-memory (milestone M1)
    private DAOFactory factory;            // cache: stessa istanza per tutte le view

    private ApplicationModeManager() {
        caricaConfigurazione();
    }

    /**
     * NFR-01: legge config.properties (risorse) e imposta la modalità di
     * persistenza all'avvio, senza ricompilare (fallback silenzioso a DEMO).
     */
    private void caricaConfigurazione() {
        try (var in = ApplicationModeManager.class.getClassLoader()
                .getResourceAsStream("config.properties")) {
            if (in == null) {
                return; // risorsa assente: resta DEMO
            }
            var props = new java.util.Properties();
            props.load(in);
            String mode = props.getProperty("persistence_type", MODE_DEMO);
            setActiveMode(mode);
        } catch (Exception e) {
            // configurazione non valida: non bloccare l'avvio, resta DEMO
            this.activeMode = MODE_DEMO;
            this.factory = null;
        }
    }

    private static class Container {
        private static final ApplicationModeManager INSTANCE = new ApplicationModeManager();
    }

    public static ApplicationModeManager getInstance() {
        return Container.INSTANCE;
    }

    public String getActiveMode() { return activeMode; }

    public void setActiveMode(String mode) {
        if (!MODE_JDBC.equals(mode) && !MODE_FS.equals(mode) && !MODE_DEMO.equals(mode)) {
            throw new IllegalArgumentException("Modalità sconosciuta: " + mode);
        }
        this.activeMode = mode;
        this.factory = null; // invalidate cache on mode switch
    }

    /** Factory della modalità attiva (Abstract Factory) — cache condivisa. */
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
