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

/**
 * Runner: unico punto di composizione dell'applicazione (in stile Layered /
 * Abstract Factory). Applica la scelta dell'utente
 * (ApplicationModeBean), compone il wiring di avvio e innesca l'interfaccia
 * selezionata tramite un callback ({@link Runnable}).
 *
 * <p>Responsabilità del compositore: modalità di persistenza, factory dei
 * dati (OrdineLazyFactory), pattern Observer (le notifiche raggiungono in
 * produzione sia compratore che venditore), famiglia di boundary
 * (ViewFactory, Abstract Factory) e seed DEMO. Nessuna di queste azioni va
 * duplicata negli entry point: essi si limitano a selezionare l'interfaccia
 * (GUI/CLI), che viene avviata DOPO che la composizione è completa.</p>
 *
 * <p>Separazione BCE: AvvioMenu (boundary, input) -&gt; Runner (control,
 * coordinamento) -&gt; MainApplication/MainCLI (avvio interfaccia).</p>
 */
public final class Runner {

    /** Protegge da doppie composizioni (idempotenza di avvio). */
    private static boolean composto = false;

    private Runner() {
        // utility, non istanziabile
    }

    /**
     * Compone il sistema dalla scelta dell'utente e innesca l'interfaccia.
     * Idempotente: una seconda chiamata è un no-op con avviso.
     *
     * @param scelta  interfaccia (GUI/CLI) e modalità di persistenza (DEMO/FS/JDBC)
     * @param args    argomenti della riga di comando (passati all'interfaccia)
     * @param startUp callback che avvia l'interfaccia selezionata (dopo la composizione)
     */
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

        // 3) Seed DEMO (utenti, prodotti, ricette) se la modalità lo richiede,
        //    indipendente dall'interfaccia scelta.
        seedDemoDataSeNecessario(modeManager);

        // 4) Pattern Observer: registra i listener una sola volta, così la
        //    notifica attiva alla conferma dell'ordine raggiunge in produzione
        //    compratore e venditore (e non solo nei test).
        OrdineEventPublisher publisher = OrdineEventPublisher.getInstance();
        publisher.addListener(new UtenteNotifier());
        publisher.addListener(new VenditoreNotifier());
        // Store di presentazione: accumula gli eventi confermati così le view
        // (GUI/CLI) possono mostrare al venditore gli ordini ricevuti.
        publisher.addListener(OrdiniRicevutiStore.getInstance());
        OrdiniRicevutiStore.getInstance().clear();
        // 4b) Per la GUI si registra anche l'observer di presentazione: così la
        //    notifica attiva diventa visibile (Alert) oltre che loggata. La CLI
        //    ne fa a meno: usa i notifier testuali.
        if (scelta.gui()) {
            publisher.addListener(new OrderNotificationAlert());
        }

        // 5) Famiglia di boundary (Abstract Factory): GUI o CLI, una sola volta.
        ViewFactory.configure(scelta.getInterfaccia());

        // 6) Avvia l'interfaccia scelta.
        startUp.run();
    }

    /** Seed DEMO condiviso tra GUI e CLI (doppia interfaccia). */
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
