package it.uniroma2.ispw.ciboamico.boundary.cli;

import it.uniroma2.ispw.ciboamico.bean.UtenteBean;
import it.uniroma2.ispw.ciboamico.boundary.IView;

/**
 * Boundary CLI — Home (atterraggio post-login), scope UC-04 (Ordina un Prodotto).
 * Menu testuale minimale: accesso al Marketplace per ordinare un prodotto
 * oppure uscita. L'applicazione implementa il solo caso d'uso Ordina.
 */
public class HomeCLIView implements IView {

    private final CLIContext ctx;
    private final IView marketplace;
    private final IView payment;

    public HomeCLIView(CLIContext ctx, IView marketplace, IView payment) {
        this.ctx = ctx;
        this.marketplace = marketplace;
        this.payment = payment;
    }

    @Override
    public void display() {
        UtenteBean utente = ctx.getLoggedUser();
        System.out.println("\n=== CiboAmico — Home ===");
        System.out.println("Benvenuto, " + (utente != null ? utente.getUsername() : "ospite")
                + (utente != null ? " (ruolo: " + utente.getRuoloAttivo() + ")" : ""));
        System.out.println("[1] Marketplace (Ordina un prodotto)");
        System.out.println("[2] Pagamento");
        System.out.println("[3] Esci");
        String scelta = ctx.leggiStringa("Scelta: ");

        switch (scelta) {
            case "1" -> marketplace.display();
            case "2" -> payment.display();
            case "3" -> System.out.println("Arrivederci!");
            default -> System.out.println("Scelta non valida.");
        }
    }
}