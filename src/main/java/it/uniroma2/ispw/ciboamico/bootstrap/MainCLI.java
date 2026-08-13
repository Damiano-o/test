package it.uniroma2.ispw.ciboamico.bootstrap;

import it.uniroma2.ispw.ciboamico.boundary.ViewFactory;

// Entry point della modalità CLI

public final class MainCLI {

    private MainCLI() { }

    public static void main(String[] args) {
        ApplicationModeBean bean = new ApplicationModeBean();
        bean.setInterfaccia("cli");
        bean.setPersistenza(ApplicationModeManager.getInstance().getActiveMode());
        Runner.avvia(bean, args, MainCLI::avviaViaRunner);
    }

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
