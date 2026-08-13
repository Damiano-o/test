package it.uniroma2.ispw.ciboamico.boundary;

import it.uniroma2.ispw.ciboamico.bootstrap.ApplicationModeManager;
import it.uniroma2.ispw.ciboamico.boundary.cli.CLIViewFactory;
import it.uniroma2.ispw.ciboamico.boundary.gui.JavaFXViewFactory;

import java.util.Locale;

/**
 * Abstract Factory delle Boundary (pattern GoF, doppia interfaccia CLI/GUI).
 * Classe astratta con metodo statico {@code getFactory()} (integra Singleton):
 * legge la modalità di avvio e istanzia a runtime l'unica factory concreta
 * corretta (GUI o CLI). I controller applicativi restano invariati: cambiano
 * solo le view (prodotti astratti {@code IView}).
 *
 * <p>Scope UC-04 (Ordina un Prodotto): sono esposte le sole boundary che
 * partecipano al caso d'uso — autenticazione, area personale, marketplace
 * e pagamento. Le versioni inventario/ricetta/lista-spesa/catalogo/ordini/
 * admin sono state escluse dalla riduzione a caso d'uso singolo.</p>
 */
public abstract class ViewFactory {

    private static ViewFactory instance;

    /** Family selezionata a runtime (default: GUI/JavaFX). */
    private static String family = "gui";

    /** Costruttore protetto: classe astratta, istanziata solo dalle sottoclassi (GUI/CLI). */
    protected ViewFactory() {
        // niente lavoro: classe astratta con solo state statico (family)
    }

    /**
     * Punto d'accesso globale (Singleton lazy): restituisce la factory della
     * family attiva. Il Main/MainCLI imposta la family con {@link #configure}.
     */
    public static synchronized ViewFactory getFactory() {
        if (instance == null) {
            instance = "cli".equalsIgnoreCase(family)
                    ? new CLIViewFactory(ApplicationModeManager.getInstance().getDAOFactory())
                    : new JavaFXViewFactory();
        }
        return instance;
    }

    /** Seleziona la family di view (\"gui\" o \"cli\") prima del primo accesso. */
    public static synchronized void configure(String uiFamily) {
        family = uiFamily == null ? "gui" : uiFamily.toLowerCase(Locale.ROOT);
        instance = null; // invalidate: la prossima getFactory() ricrea la factory giusta
    }

    public abstract IView createLoginView();

    public abstract IView createHomeView();

    public abstract IView createMarketplaceView();

    public abstract IView createPaymentView();
}