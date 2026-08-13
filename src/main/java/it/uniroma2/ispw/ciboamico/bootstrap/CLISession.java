package it.uniroma2.ispw.ciboamico.bootstrap;

import it.uniroma2.ispw.ciboamico.boundary.cli.CLIContext;
import it.uniroma2.ispw.ciboamico.pattern.singleton.SessionManager;

// Helper della modalità CLI: verifica se esiste un utente loggato

public final class CLISession {

    private CLISession() { }

    public static boolean hasUser() {
        return SessionManager.getInstance().getLoggedUser() != null;
    }

    public static boolean wantsToContinue() {
        System.out.print("\nVuoi continuare? (s/n): ");
        // Riutilizza lo scanner condiviso di CLIContext
        // EOF/assenza input = esci (mai NoSuchElementException). Il `&&`
        // leggiRiga() è chiamato solo se c'è davvero un'ultima riga.
        return CLIContext.scannerCondiviso().hasNextLine()
                && "s".equalsIgnoreCase(CLIContext.leggiRiga());
    }
}
