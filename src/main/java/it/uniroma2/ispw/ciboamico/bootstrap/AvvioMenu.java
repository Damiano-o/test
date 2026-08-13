package it.uniroma2.ispw.ciboamico.bootstrap;

import it.uniroma2.ispw.ciboamico.boundary.cli.CLIContext;

import java.util.Scanner;

// Boundary di avvio: menu interattivo con cui l'utente sceglie l'interfaccia e la modalit...

public final class AvvioMenu {

    private AvvioMenu() {

    }

    public static ApplicationModeBean chiediScelta() {
        System.out.println("=========================================");
        System.out.println("         CIBOAMICO — avvio");
        System.out.println("=========================================");

        String interfaccia = scegliInterfaccia();
        String persistenza = scegliPersistenza();

        ApplicationModeBean bean = new ApplicationModeBean();
        bean.setInterfaccia(interfaccia);
        bean.setPersistenza(persistenza);
        return bean;
    }

    private static String scegliInterfaccia() {
        Scanner scanner = CLIContext.scannerCondiviso();
        while (true) {
            System.out.println("\nInterfaccia:");
            System.out.println("  1) Grafica (GUI JavaFX)");
            System.out.println("  2) Linea di comando (CLI)");
            System.out.print("Scegli (1-2): ");
            String scelta = scanner.nextLine().trim();
            switch (scelta) {
                case "1": return "gui";
                case "2": return "cli";
                default:
                    System.out.println("  ! Scelta non valida, riprova.");
            }
        }
    }

    private static String scegliPersistenza() {
        Scanner scanner = CLIContext.scannerCondiviso();
        while (true) {
            System.out.println("\nModalità di persistenza:");
            System.out.println("  1) DEMO — dati in memoria (nessuna persistenza)");
            System.out.println("  2) FS   — salvataggio su file JSON locale");
            System.out.println("  3) JDBC — database MySQL");
            System.out.print("Scegli (1-3): ");
            String scelta = scanner.nextLine().trim();
            switch (scelta) {
                case "1": return ApplicationModeManager.MODE_DEMO;
                case "2": return ApplicationModeManager.MODE_FS;
                case "3": return ApplicationModeManager.MODE_JDBC;
                default:
                    System.out.println("  ! Scelta non valida, riprova.");
            }
        }
    }
}