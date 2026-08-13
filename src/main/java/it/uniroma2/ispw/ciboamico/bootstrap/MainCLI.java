package it.uniroma2.ispw.ciboamico.bootstrap;

import it.uniroma2.ispw.ciboamico.boundary.ViewFactory;

/**
 * Entry point della modalità CLI.
 *
 * <p>Come per {@link MainApplication}, non è un compositore: la composizione
 * (DAOFactory, seed, Observer, famiglia CLI) è in {@link Runner}. Qui c'è solo
 * l'innesco del loop testuale. I controller applicativi sono gli stessi della
 * GUI: cambia solo la famiglia di boundary (Abstract Factory, scelta da
 * Runner).</p>
 *
 * <p>Avvio: Main -&gt; Runner (composizione) -&gt; MainCLI.avviaViaRunner -&gt;
 * loop login/home.</p>
 */
public final class MainCLI {

    private MainCLI() { }

    /** Entry point standalone: delega la composizione completa a Runner. */
    public static void main(String[] args) {
        ApplicationModeBean bean = new ApplicationModeBean();
        bean.setInterfaccia("cli");
        bean.setPersistenza(ApplicationModeManager.getInstance().getActiveMode());
        Runner.avvia(bean, args, MainCLI::avviaViaRunner);
    }

    /** Innesco del loop CLI dopo che {@link Runner} ha composto il sistema. */
    public static void avviaViaRunner() {
        ViewFactory factory = ViewFactory.getFactory();
        boolean running = true;
        while (running) {
            factory.createLoginView().display();
            if (CLISession.hasUser()) {
                factory.createHomeView().display();
            }
            running = CLISession.wantsToContinue();
        }
        System.out.println("Arrivederci!");
    }
}
