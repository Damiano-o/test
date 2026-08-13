package it.uniroma2.ispw.ciboamico.bootstrap;

import it.uniroma2.ispw.ciboamico.boundary.DashboardView;
import it.uniroma2.ispw.ciboamico.boundary.InventarioView;
import it.uniroma2.ispw.ciboamico.boundary.ListaSpesaView;
import it.uniroma2.ispw.ciboamico.boundary.LoginView;
import it.uniroma2.ispw.ciboamico.boundary.MarketplaceView;
import it.uniroma2.ispw.ciboamico.boundary.Navigator;
import it.uniroma2.ispw.ciboamico.boundary.PaymentView;
import it.uniroma2.ispw.ciboamico.boundary.RicetteView;
import javafx.application.Application;
import javafx.stage.Stage;

// Entry point JavaFX dell'applicazione

public final class MainApplication extends Application {

    private final ApplicationModeManager modeManager;

    public MainApplication() {
        this.modeManager = ApplicationModeManager.getInstance();
    }

    @Override
    public void start(Stage stage) {
        Navigator navigator = Navigator.getInstance();
        navigator.init(stage);

        // Registrazione view (nomi simbolici, factory programmatiche).
        // Le Boundary costruiscono i propri controller tramite il registro
        // applicativo, senza ricevere la DAOFactory: la View non conosce
        // persistenza.
        navigator.register("login", new LoginView()::build);
        navigator.register("home", new DashboardView()::build);
        navigator.register("marketplace", new MarketplaceView()::build);
        navigator.register("payment", new PaymentView()::build);
        navigator.register("ricette", new RicetteView()::build);
        navigator.register("inventario", new InventarioView()::build);
        navigator.register("listaspesa", new ListaSpesaView()::build);

        stage.setTitle("CiboAmico — " + modeManager.getActiveMode());
        navigator.switchTo("login");
    }

    public static void main(String[] args) {
        ApplicationModeBean bean = new ApplicationModeBean();
        bean.setInterfaccia("gui");
        bean.setPersistenza(ApplicationModeManager.getInstance().getActiveMode());
        Runner.avvia(bean, args, MainApplication::avviaViaRunner);

        // In DEMO il seed è già a carico di Runner
        // composizione; qui si riporta solo la modalità per informazione.
        ApplicationModeManager manager = ApplicationModeManager.getInstance();
        System.out.println("CiboAmico in modalità: " + manager.getActiveMode());
    }

    // Innesco della GUI dopo la composizione di Runner

    public static void avviaViaRunner() {
        launch(new String[0]);
    }
}
