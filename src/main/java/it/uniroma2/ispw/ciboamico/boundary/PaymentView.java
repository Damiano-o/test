package it.uniroma2.ispw.ciboamico.boundary;

import it.uniroma2.ispw.ciboamico.bean.OrdineBean;
import it.uniroma2.ispw.ciboamico.bean.UtenteBean;
import it.uniroma2.ispw.ciboamico.control.graphic.PaymentGraphicController;
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
 * <p>È un puro layout: legge l'ordine in checkout dalla sessione, raccoglie i
 * dati carta e delega l'autorizzazione al
 * {@link PaymentGraphicController} (controller grafico disaccoppiato), che
 * invoca il controller applicativo via Facade. La view scambia solo Bean ed
 * applica l'esito ai widget.</p>
 */
public class PaymentView {

    private final PaymentGraphicController controller;

    public PaymentView() {
        this.controller = new PaymentGraphicController();
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

    // -------- Listener: rinvio al controller grafico --------

    private void onPaga(UtenteBean utente, OrdineBean ordine,
                        TextField numeroCarta, TextField intestatario,
                        TextField scadenza, TextField cvv, Label esito) {
        try {
            OrdineBean risultato = controller.paga(
                    ordine, utente,
                    numeroCarta.getText(), intestatario.getText(),
                    scadenza.getText(), cvv.getText());
            esito.setText(PaymentGraphicController.formattaEsitoPagamento(risultato));
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
