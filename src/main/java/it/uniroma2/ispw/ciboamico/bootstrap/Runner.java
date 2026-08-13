package it.uniroma2.ispw.ciboamico.bootstrap;

import it.uniroma2.ispw.ciboamico.exception.DAOException;
import it.uniroma2.ispw.ciboamico.pattern.factory.OrdineLazyFactory;
import it.uniroma2.ispw.ciboamico.pattern.observer.OrdineEventPublisher;
import it.uniroma2.ispw.ciboamico.pattern.observer.UtenteNotifier;
import it.uniroma2.ispw.ciboamico.pattern.observer.VenditoreNotifier;
import it.uniroma2.ispw.ciboamico.boundary.OrderNotificationAlert;
import it.uniroma2.ispw.ciboamico.boundary.OrdiniRicevutiStore;
import it.uniroma2.ispw.ciboamico.boundary.ViewFactory;
import it.uniroma2.ispw.ciboamico.persistence.factory.DemoDAOFactory;

// Runner: unico punto di composizione dell'applicazione (in stile

public final class Runner {

    private static boolean composto = false;

    private Runner() {

    }

    // Compone il sistema dalla scelta dell'utente e innesca

    public static synchronized void avvia(ApplicationModeBean scelta, String[] args, Runnable startUp) {
        if (composto) {
            System.err.println("Runner.avvia chiamato due volte: composizione già eseguita.");
            return;
        }
        composto = true;

        ApplicationModeManager modeManager = ApplicationModeManager.getInstance();

        // 1) Modalità di persistenza scelta dall'utente.
        modeManager.setActiveMode(scelta.getPersistenza());

        System.out.println("\nCiboAmico avviato in modalità: "
                + modeManager.getActiveMode());
        System.out.println("DAOFactory: "
                + modeManager.getDAOFactory().getClass().getSimpleName());

        // 2) LazyFactory degli ordini con la DAOFactory attiva (UC-04).
        OrdineLazyFactory.configure(modeManager.getDAOFactory());

        // 3) Seed DEMO se la modalità lo richiede
        seedDemoDataSeNecessario(modeManager);

        // 4) Registra i listener Observer (notifier + store presentazione)
        OrdineEventPublisher publisher = OrdineEventPublisher.getInstance();
        publisher.addListener(new UtenteNotifier());
        publisher.addListener(new VenditoreNotifier());
        // Store di presentazione: accumula gli eventi per le view
        publisher.addListener(OrdiniRicevutiStore.getInstance());
        OrdiniRicevutiStore.getInstance().clear();
        // 4b) GUI: alert di notifica visibile
        if (scelta.gui()) {
            publisher.addListener(new OrderNotificationAlert());
        }

        // 5) Famiglia di boundary (GUI o CLI)
        ViewFactory.configure(scelta.getInterfaccia());

        // 6) Avvia l'interfaccia scelta
        startUp.run();
    }

    private static void seedDemoDataSeNecessario(ApplicationModeManager modeManager) {
        if (ApplicationModeManager.MODE_DEMO.equals(modeManager.getActiveMode())
                && modeManager.getDAOFactory() instanceof DemoDAOFactory demo) {
            try {
                demo.seedDemoData();
            } catch (DAOException e) {
                System.err.println("Seed DEMO fallito: " + e.getTechnicalMessage());
            }
        }
    }
}
