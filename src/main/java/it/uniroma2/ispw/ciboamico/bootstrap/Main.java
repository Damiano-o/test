package it.uniroma2.ispw.ciboamico.bootstrap;

import java.util.Locale;

// Entry point dell'applicazione (doppia interfaccia CLI/GUI)

public final class Main {

    private Main() {

    }

    public static void main(String[] args) {
        String ui = args.length > 0 ? args[0].toLowerCase(Locale.ROOT) : null;

        ApplicationModeBean bean;
        if ("gui".equals(ui) || "cli".equals(ui)) {
            bean = new ApplicationModeBean();
            bean.setInterfaccia(ui);
            bean.setPersistenza(ApplicationModeManager.getInstance().getActiveMode());
        } else {

            bean = AvvioMenu.chiediScelta();
        }

        Runner.avvia(bean, args, () -> avviaInterfaccia(bean));
    }

    private static void avviaInterfaccia(ApplicationModeBean bean) {
        if (bean.gui()) {
            MainApplication.avviaViaRunner();
        } else {
            MainCLI.avviaViaRunner();
        }
    }
}
