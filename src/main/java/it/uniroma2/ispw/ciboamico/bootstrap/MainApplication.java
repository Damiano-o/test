package it.uniroma2.ispw.ciboamico.bootstrap;

import it.uniroma2.ispw.ciboamico.boundary.*;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Entry point JavaFX dell'applicazione.
 * Registra tutte le Boundary nel Navigator e avvia la Login.
 * Flusso: Main → ApplicationModeManager → DAOFactory → Navigator → Login.
 */
public final class MainApplication extends Application {

    private final ApplicationModeManager modeManager;

    public MainApplication() {
        this.modeManager = ApplicationModeManager.getInstance();
    }

    @Override
    public void start(Stage stage) {
        // In modalità DEMO carica i dati seed (utenti, prodotti, ricette).
        seedDemoDataSeNecessario();

        Navigator navigator = Navigator.getInstance();
        navigator.init(stage);

        // Registrazione view (nomi simbolici, factory programmatiche)
        // Le Boundary costruiscono i propri controller tramite il registro applicativo,
        // senza ricevere la DAOFactory: la View non conosce la persistenza.
        navigator.register("login", new LoginView()::build);
        navigator.register("home", new HomeView()::build);
        navigator.register("marketplace", new MarketplaceView()::build);
        navigator.register("payment", new PaymentView()::build);
        stage.setTitle("CiboAmico — " + modeManager.getActiveMode());
        navigator.switchTo("login");
    }

    public static void main(String[] args) {
        ApplicationModeManager manager = ApplicationModeManager.getInstance();
        System.out.println("CiboAmico in modalità: " + manager.getActiveMode());
        launch(args);
    }

    /** Seed DEMO condiviso tra GUI e CLI (doppia interfaccia). */
    public static void seedDemoDataSeNecessario() {
        ApplicationModeManager modeManager = ApplicationModeManager.getInstance();
        if (ApplicationModeManager.MODE_DEMO.equals(modeManager.getActiveMode())
                && modeManager.getDAOFactory() instanceof it.uniroma2.ispw.ciboamico.persistence.factory.DemoDAOFactory demo) {
            demo.seedDemoData();
        }
    }
}
