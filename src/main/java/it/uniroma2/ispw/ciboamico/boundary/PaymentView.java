package it.uniroma2.ispw.ciboamico.boundary;

import it.uniroma2.ispw.ciboamico.bean.OrdineBean;
import it.uniroma2.ispw.ciboamico.bean.UtenteBean;
import it.uniroma2.ispw.ciboamico.control.facade.OrdinaProdottoFacade;
import it.uniroma2.ispw.ciboamico.exception.BusinessValidationException;
import it.uniroma2.ispw.ciboamico.pattern.singleton.SessionManager;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

/**
 * Boundary JavaFX — Schermata di Pagamento (passo 6 + estensione 6a UC-04).
 *
 * <p>Legge l'ordine in checkout da {@link SessionManager#getOrdineInCorso()},
 * raccoglie i dati carta e delega l'autorizzazione al
 * {@link OrdinaProdottoFacade}. La conversione dei dati carta e l'addebito
 * sono gestiti dal controller (via Facade); qui risiede solo la UI (metodi
 * {@code on...}).</p>
 */
public class PaymentView {

    private final OrdinaProdottoFacade facade;

    public PaymentView() {
        this.facade = new OrdinaProdottoFacade();
    }

    public Parent build() {
        UtenteBean utente = SessionManager.getInstance().getLoggedUser();
        OrdineBean ordine = SessionManager.getInstance().getOrdineInCorso();

        Label messaggio = new Label(ordine != null
                ? "Riepilogo: " + ordine.getNomeProdotto() + " — " + String.format("%.2f EUR", ordine.getTotale())
                : "Nessun ordine in checkout per il pagamento.");
        messaggio.getStyleClass().add("page-subtitle");
        messaggio.setWrapText(true);

        TextField numeroCarta = new TextField();
        numeroCarta.setId("numCarta");
        numeroCarta.setPromptText("Numero carta");
        numeroCarta.setMaxWidth(Double.MAX_VALUE);

        TextField intestatario = new TextField();
        intestatario.setPromptText("Intestatario");
        intestatario.setMaxWidth(Double.MAX_VALUE);

        TextField scadenza = new TextField();
        scadenza.setPromptText("Scadenza (MM/AA)");
        scadenza.setMaxWidth(Double.MAX_VALUE);

        TextField cvv = new TextField();
        cvv.setId("cvvField");
        cvv.setPromptText("CVV");
        cvv.setMaxWidth(Double.MAX_VALUE);

        Button paga = new Button("Paga");
        paga.setId("btn-paga");
        paga.setMaxWidth(Double.MAX_VALUE);
        Label esito = new Label(" ");
        esito.getStyleClass().add("page-subtitle");
        esito.setWrapText(true);

        paga.setOnAction(e -> onPaga(utente, ordine, numeroCarta, intestatario, scadenza, cvv, esito));

        Button annulla = new Button("Annulla");
        annulla.setId("btn-annulla-pagamento");
        annulla.setMaxWidth(Double.MAX_VALUE);
        annulla.setOnAction(e -> onAnnulla());

        VBox corpo = new VBox(10,
                messaggio,
                UiKit.field("Numero carta"), numeroCarta,
                UiKit.field("Intestatario"), intestatario,
                UiKit.field("Scadenza"), scadenza,
                UiKit.field("CVV"), cvv,
                esito, paga, annulla);
        corpo.setPadding(new Insets(16, 0, 0, 0));
        corpo.getStyleClass().add("form-panel");
        return UiKit.pagina("Pagamento", "UC-04 · autorizzazione all'addebito", corpo, "marketplace");
    }

    // -------- Gestori UI (stile on...) --------

    private void onPaga(UtenteBean utente, OrdineBean ordine,
                        TextField numeroCarta, TextField intestatario,
                        TextField scadenza, TextField cvv, Label esito) {
        try {
            if (ordine == null) {
                throw new BusinessValidationException("Nessun ordine in checkout.");
            }
            OrdineBean risultato = facade.processaPagamento(
                    ordine, utente,
                    numeroCarta.getText(), intestatario.getText(),
                    scadenza.getText(), cvv.getText());
            esito.setText("Pagamento riuscito ✓ — ordine " + risultato.getStato()
                    + ", totale " + String.format("%.2f EUR", risultato.getTotale()));
            Navigator.getInstance().switchTo("marketplace");
        } catch (BusinessValidationException ex) {
            esito.setText(ex.getUserMessage());
        } catch (Exception ex) {
            esito.setText("Problema tecnico: riprovare più tardi.");
        }
    }

    private void onAnnulla() {
        SessionManager.getInstance().setOrdineInCorso(null);
        Navigator.getInstance().switchTo("marketplace");
    }
}
