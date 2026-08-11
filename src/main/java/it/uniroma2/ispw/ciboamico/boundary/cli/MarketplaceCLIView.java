package it.uniroma2.ispw.ciboamico.boundary.cli;

import it.uniroma2.ispw.ciboamico.bean.OrdineBean;
import it.uniroma2.ispw.ciboamico.bean.ProdottoBean;
import it.uniroma2.ispw.ciboamico.bean.UtenteBean;
import it.uniroma2.ispw.ciboamico.boundary.IView;
import it.uniroma2.ispw.ciboamico.control.facade.OrdinaProdottoFacade;
import it.uniroma2.ispw.ciboamico.exception.BusinessValidationException;
import it.uniroma2.ispw.ciboamico.pattern.singleton.SessionManager;

import java.util.List;

/**
 * Boundary CLI — Marketplace (UC-04 Ordina un Prodotto). Mostra i prodotti
 * disponibili, applica un eventuale buono promozionale (estensione 4a) e crea
 * l'ordine delegando al {@link OrdinaProdottoFacade} (scambio solo Bean).
 */
public class MarketplaceCLIView implements IView {

    private final CLIContext ctx;
    private final OrdinaProdottoFacade facade;

    public MarketplaceCLIView(CLIContext ctx) {
        this.ctx = ctx;
        this.facade = new OrdinaProdottoFacade();
    }

    @Override
    public void display() {
        UtenteBean utente = ctx.getLoggedUser();
        if (utente == null) {
            System.out.println("Nessun utente loggato.");
            return;
        }
        System.out.println("\n=== Marketplace (UC-04) ===");
        List<ProdottoBean> prodotti = facade.getProdottiDisponibili();
        if (prodotti.isEmpty()) {
            System.out.println("Nessun prodotto in vendita.");
            return;
        }
        for (ProdottoBean p : prodotti) {
            System.out.printf("- %s — %.2f EUR — %.0f disponibili%n",
                    p.getNome(), p.getPrezzo(), p.getQuantita());
        }
        String nomeProdotto = ctx.leggiStringa("Nome prodotto da ordinare (invio = annulla): ");
        if (nomeProdotto.isEmpty()) {
            return;
        }
        try {
            // Delego al Grafico la creazione dell'ordine in corso (checkout)
            facade.avviaCheckout(nomeProdotto);

            // Estensione 4a: buono promozionale opzionale (invio = nessuno)
            String codiceBuono = ctx.leggiStringa("Codice buono (opzionale, invio per saltare): ").trim();
            if (!codiceBuono.isEmpty()) {
                OrdineBean scontato = facade.applicaBuono(codiceBuono, nomeProdotto, utente);
                System.out.printf("✓ Buono \"%s\" applicato — totale %.2f EUR%n",
                        scontato.getCodiceBuono(), scontato.getTotale());
            }
        } catch (BusinessValidationException e) {
            System.out.println("Problema: " + e.getUserMessage());
            return;
        }
        new PaymentCLIView(ctx).display();
    }
}
