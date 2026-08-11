package it.uniroma2.ispw.ciboamico.boundary.cli;

import it.uniroma2.ispw.ciboamico.bean.OrdineBean;
import it.uniroma2.ispw.ciboamico.bean.UtenteBean;
import it.uniroma2.ispw.ciboamico.boundary.IView;
import it.uniroma2.ispw.ciboamico.control.facade.OrdinaProdottoFacade;
import it.uniroma2.ispw.ciboamico.exception.BusinessValidationException;
import it.uniroma2.ispw.ciboamico.pattern.singleton.SessionManager;

/**
 * Boundary CLI — Pagamento (passo 6 + estensione 6a UC-04).
 * Legge il prodotto in checkout da {@link SessionManager#getOrdineInCorso()},
 * raccoglie i dati carta e delega il processo al {@link OrdinaProdottoFacade}.
 */
public class PaymentCLIView implements IView {

    private final CLIContext ctx;
    private final OrdinaProdottoFacade facade;

    public PaymentCLIView(CLIContext ctx) {
        this.ctx = ctx;
        this.facade = new OrdinaProdottoFacade();
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
        String intestatario = ctx.getLoggedUser() != null ? ctx.getLoggedUser().getUsername() : "";

        try {
            // Il Grafico valida, converte in PaymentInfoBean e delega all'Applicativo.
            OrdineBean risultato = facade.processaPagamento(
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