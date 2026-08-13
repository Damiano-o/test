package it.uniroma2.ispw.ciboamico.boundary.cli;

import it.uniroma2.ispw.ciboamico.bean.OrdineBean;
import it.uniroma2.ispw.ciboamico.bean.ProdottoBean;
import it.uniroma2.ispw.ciboamico.bean.UtenteBean;
import it.uniroma2.ispw.ciboamico.boundary.IView;
import it.uniroma2.ispw.ciboamico.control.ui.MarketplaceUIController;
import it.uniroma2.ispw.ciboamico.exception.BusinessValidationException;

import java.util.List;

// Boundary CLI — Marketplace (UC-04 Ordina un Prodotto)

public class MarketplaceCLIView implements IView {

    private final CLIContext ctx;
    private final MarketplaceUIController controller;
    private final IView payment;

    public MarketplaceCLIView(CLIContext ctx, IView payment) {
        this.ctx = ctx;
        this.payment = payment;
        this.controller = new MarketplaceUIController();
    }

    @Override
    public void display() {
        UtenteBean utente = ctx.getLoggedUser();
        if (utente == null) {
            System.out.println("Nessun utente loggato.");
            return;
        }
        // Guardia: il venditore non accede al marketplace (funzione cliente)
        if ("VENDITORE".equalsIgnoreCase(utente.getRuoloAttivo())) {
            System.out.println("Il marketplace è riservato ai clienti.");
            return;
        }
        this.controller.setUtente(utente);
        System.out.println("\n=== Marketplace (UC-04) ===");
        List<ProdottoBean> prodotti;
        try {
            prodotti = controller.catalogoProdotti();
        } catch (Exception e) {
            System.out.println("Problema di accesso al catalogo: riprovare più tardi.");
            return;
        }
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
            // Delego al Grafico la conversione esterno→interno + checkout
            controller.ordinaProdotto(nomeProdotto);

            // Estensione 4a: buono promozionale opzionale (invio = nessuno)
            String codiceBuono = ctx.leggiStringa("Codice buono (opzionale, invio per saltare): ").trim();
            if (!codiceBuono.isEmpty()) {
                OrdineBean scontato = controller.applicaBuono(codiceBuono, nomeProdotto);
                System.out.printf("✓ Buono \"%s\" applicato — totale %.2f EUR%n",
                        scontato.getCodiceBuono(), scontato.getTotale());
            }
        } catch (BusinessValidationException e) {
            System.out.println("Problema: " + e.getUserMessage());
            return;
        } catch (Exception e) {
            System.out.println("Errore di sistema: riprovare più tardi.");
            return;
        }
        payment.display();
    }
}
