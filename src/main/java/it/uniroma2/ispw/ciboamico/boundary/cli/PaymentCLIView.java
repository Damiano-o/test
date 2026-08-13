package it.uniroma2.ispw.ciboamico.boundary.cli;

import it.uniroma2.ispw.ciboamico.bean.OrdineBean;
import it.uniroma2.ispw.ciboamico.bean.UtenteBean;
import it.uniroma2.ispw.ciboamico.boundary.IView;
import it.uniroma2.ispw.ciboamico.control.ui.PaymentUIController;
import it.uniroma2.ispw.ciboamico.exception.BusinessValidationException;
import it.uniroma2.ispw.ciboamico.pattern.singleton.SessionManager;

// Boundary CLI — Pagamento (passo 6 + estensione 6a UC-04)

public class PaymentCLIView implements IView {

    private final CLIContext ctx;
    private final PaymentUIController controller;

    public PaymentCLIView(CLIContext ctx) {
        this.ctx = ctx;
        this.controller = new PaymentUIController();
    }

    @Override
    public void display() {
        UtenteBean utente = ctx.getLoggedUser();
        OrdineBean ordine = SessionManager.getInstance().getOrdineInCorso();
        if (utente == null || ordine == null) {
            System.out.println("Sessione o ordine in checkout non disponibile.");
            return;
        }
        System.out.println("\n=== Pagamento (UC-04) ===");
        System.out.printf("Prodotto: %s — %.2f EUR%n", ordine.getNomeProdotto(), ordine.getTotale());
        String numero = ctx.leggiStringa("Numero carta (invio = 0000000000000000): ");
        String cvv = ctx.leggiStringa("CVV (3 cifre): ");
        if (numero.isEmpty()) {
            numero = "0000000000000000";
        }
        String intestatario = utente.getUsername() != null ? utente.getUsername() : "";

        try {
            // Conversione esterno→interno + autorizzazione delegate al controller
            // di presentazione (DRY con la vista grafica), non alla CLI.
            OrdineBean risultato = controller.paga(
                    ordine, utente, numero, intestatario, "12/29", cvv);
            System.out.printf("Pagamento riuscito ✓ — ordine %s, totale %.2f EUR%n",
                    risultato.getStato(), risultato.getTotale());
        } catch (BusinessValidationException e) {
            System.out.println("Pagamento negato: " + e.getUserMessage());
        } catch (Exception e) {
            System.out.println("Errore di sistema durante il pagamento: riprovare.");
        }
    }
}