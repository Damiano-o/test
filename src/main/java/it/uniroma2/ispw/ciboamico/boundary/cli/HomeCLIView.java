package it.uniroma2.ispw.ciboamico.boundary.cli;

import it.uniroma2.ispw.ciboamico.bean.UtenteBean;
import it.uniroma2.ispw.ciboamico.boundary.IView;
import it.uniroma2.ispw.ciboamico.boundary.OrdiniRicevutiStore;
import it.uniroma2.ispw.ciboamico.pattern.observer.OrdineEvent;
import it.uniroma2.ispw.ciboamico.pattern.singleton.SessionManager;

import java.util.List;

// Boundary CLI — Home (atterraggio post-login), scope UC-04

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
        String ruolo = utente != null ? utente.getRuoloAttivo() : null;
        boolean venditore = "VENDITORE".equalsIgnoreCase(ruolo);

        System.out.println("\n=== CiboAmico — Home ===");
        System.out.println("Benvenuto, " + (utente != null ? utente.getUsername() : "ospite")
                + (utente != null ? " (ruolo: " + ruolo + ")" : ""));

        if (venditore) {
            System.out.println("[1] Ordini ricevuti");
            System.out.println("[2] Esci");
        } else {
            System.out.println("[1] Marketplace (Ordina un prodotto)");
            System.out.println("[2] Pagamento");
            System.out.println("[3] Esci");
        }
        String scelta = ctx.leggiStringa("Scelta: ");

        if (venditore) {
            switch (scelta) {
                case "1" -> mostraOrdiniRicevuti(utente);
                case "2" -> esci();
                default -> System.out.println("Scelta non valida.");
            }
        } else {
            switch (scelta) {
                case "1" -> marketplace.display();
                case "2" -> payment.display();
                case "3" -> esci();
                default -> System.out.println("Scelta non valida.");
            }
        }
    }

    // ripassa dal login e permette di cambiare utente

    private void esci() {
        SessionManager.getInstance().logout();
        System.out.println("Arrivederci!");
    }

    private void mostraOrdiniRicevuti(UtenteBean utente) {
        String email = utente != null ? utente.getEmail() : null;
        List<OrdineEvent> ricevuti = OrdiniRicevutiStore.getInstance()
                .getOrdiniPerVenditore(email);
        System.out.println("\n--- Ordini ricevuti ---");
        if (ricevuti.isEmpty()) {
            System.out.println("Nessun ordine ricevuto finora.");
        } else {
            for (OrdineEvent o : ricevuti) {
                System.out.printf("Ordine #%d · cliente %s · %.2f EUR%n",
                        o.getNumeroOrdine(), o.getClienteId(), o.getTotale());
            }
        }
    }
}
