package it.uniroma2.ispw.ciboamico.boundary;

import it.uniroma2.ispw.ciboamico.bootstrap.ApplicationModeManager;
import it.uniroma2.ispw.ciboamico.boundary.cli.CLIViewFactory;
import it.uniroma2.ispw.ciboamico.boundary.gui.JavaFXViewFactory;

import java.util.Locale;

// Abstract Factory delle Boundary (pattern GoF, doppia interfaccia CLI/GUI)

public abstract class ViewFactory {

    private static ViewFactory instance;

    private static String family = "gui";

    protected ViewFactory() {

    }

    // Punto d'accesso globale (Singleton lazy): restituisce la factory della family attiva

    public static synchronized ViewFactory getFactory() {
        if (instance == null) {
            instance = "cli".equalsIgnoreCase(family)
                    ? new CLIViewFactory(ApplicationModeManager.getInstance().getDAOFactory())
                    : new JavaFXViewFactory();
        }
        return instance;
    }

    public static synchronized void configure(String uiFamily) {
        family = uiFamily == null ? "gui" : uiFamily.toLowerCase(Locale.ROOT);
        instance = null; // invalidate: la prossima getFactory() ricrea la factory giusta
    }

    public abstract IView createLoginView();

    public abstract IView createHomeView();

    public abstract IView createMarketplaceView();

    public abstract IView createPaymentView();
}