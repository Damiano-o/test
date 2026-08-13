package it.uniroma2.ispw.ciboamico.boundary.cli;

import it.uniroma2.ispw.ciboamico.bean.UtenteBean;
import it.uniroma2.ispw.ciboamico.pattern.singleton.SessionManager;
import it.uniroma2.ispw.ciboamico.persistence.factory.DAOFactory;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

// Contesto condiviso delle view CLI: input da tastiera (Scanner), la DAOFactory attiva e...

public final class CLIContext {

    private static final Scanner SCANNER =
            new Scanner(new InputStreamReader(System.in, StandardCharsets.UTF_8));

    private final DAOFactory factory;

    public CLIContext(DAOFactory factory) {
        this.factory = factory;
    }

    public DAOFactory getFactory() {
        return factory;
    }

    public UtenteBean getLoggedUser() {
        return SessionManager.getInstance().getLoggedUser();
    }

    public String leggiStringa(String prompt) {
        System.out.print(prompt);
        return SCANNER.nextLine().trim();
    }

    public Double leggiDouble(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                return Double.parseDouble(SCANNER.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Numero non valido, riprova.");
            }
        }
    }

    public Long leggiLong(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                return Long.parseLong(SCANNER.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Numero non valido, riprova.");
            }
        }
    }

    public boolean leggiSiNo(String prompt) {
        System.out.print(prompt + " (s/n): ");
        return "s".equalsIgnoreCase(SCANNER.nextLine().trim());
    }

    public String prossimaRiga() {
        return SCANNER.nextLine().trim();
    }

    public static String leggiRiga() {
        return SCANNER.nextLine().trim();
    }

    public static Scanner scannerCondiviso() {
        return SCANNER;
    }
}
