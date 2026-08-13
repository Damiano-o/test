package it.uniroma2.ispw.ciboamico.bootstrap;

import java.util.Locale;

/**
 * Entry point dell'applicazione (doppia interfaccia CLI/GUI).
 *
 * <p>Raccoglie la scelta che l'utente fa (interfaccia GUI/CLI e modalità di
 * persistenza DEMO/FS/JDBC) in un {@link ApplicationModeBean} e la inoltra a
 * {@link Runner}, il punto unico di composizione del sistema. Runner esegue
 * il wiring e avvia l'interfaccia scelta tramite il callback.</p>
 *
 * <p>Backward-compat: un argomento "gui" o "cli" avvia direttamente
 * quell'interfaccia in modalità config.properties, senza menu (utile per
 * automazione test/demo).</p>
 */
public final class Main {

    private Main() {
        // classe utility: solo entry point statico
    }

    public static void main(String[] args) {
        String ui = args.length > 0 ? args[0].toLowerCase(Locale.ROOT) : null;

        ApplicationModeBean bean;
        if ("gui".equals(ui) || "cli".equals(ui)) {
            // Avvio diretto (senza menu): interfaccia dall'argomento,
            // persistenza da ApplicationModeManager (file config, default DEMO).
            bean = new ApplicationModeBean();
            bean.setInterfaccia(ui);
            bean.setPersistenza(ApplicationModeManager.getInstance().getActiveMode());
        } else {
            // Avvio normale: l'utente sceglie dal menu interattivo.
            bean = AvvioMenu.chiediScelta();
        }

        Runner.avvia(bean, args, () -> avviaInterfaccia(bean));
    }

    /** Seleziona l'interfaccia (GUI/CLI) una volta che Runner ha composto. */
    private static void avviaInterfaccia(ApplicationModeBean bean) {
        if (bean.gui()) {
            MainApplication.avviaViaRunner();
        } else {
            MainCLI.avviaViaRunner();
        }
    }
}
