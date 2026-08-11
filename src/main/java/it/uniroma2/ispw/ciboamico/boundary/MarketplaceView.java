package it.uniroma2.ispw.ciboamico.boundary;

import it.uniroma2.ispw.ciboamico.bean.OrdineBean;
import it.uniroma2.ispw.ciboamico.bean.ProdottoBean;
import it.uniroma2.ispw.ciboamico.bean.UtenteBean;
import it.uniroma2.ispw.ciboamico.control.facade.OrdinaProdottoFacade;
import it.uniroma2.ispw.ciboamico.exception.BusinessValidationException;
import it.uniroma2.ispw.ciboamico.pattern.singleton.SessionManager;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Boundary JavaFX — Marketplace (UC-04 Ordina Prodotto).
 *
 * <p>Gestisce l'interazione della UI tramite metodi privati {@code on...}
 * (coerente con lo stile delle Boundary del pattern di riferimento) e delega
 * la logica di business al {@link OrdinaProdottoFacade}. Scambia solo
 * Bean con il Facade.</p>
 */
public class MarketplaceView {

    private final OrdinaProdottoFacade facade;
    private final UtenteBean utente;

    // Controlli della vista
    private Button aggiorna;
    private Button ordina;
    private Button applicaBuono;
    private TextField buonoField;
    private ComboBox<String> prodSelez;
    private FlowPane catalogo;
    private Label messaggio;

    public MarketplaceView() {
        this.facade = new OrdinaProdottoFacade();
        this.utente = SessionManager.getInstance().getLoggedUser();
    }

    public Parent build() {
        inizializzaControlli();
        aggiorna.setOnAction(e -> onAggiornaCatalogo());
        ordina.setOnAction(e -> onOrdinaProdotto());
        applicaBuono.setOnAction(e -> onApplicaBuono());

        BorderPane area = new BorderPane();
        VBox ordinePanel = new VBox(8,
                UiKit.field("Prodotto da ordinare"), prodSelez, ordina,
                new Label("Buono promozionale"), buonoField, applicaBuono);
        ordinePanel.getStyleClass().add("form-panel");
        BorderPane.setMargin(ordinePanel, new Insets(0, 0, 0, 16));
        area.setCenter(catalogo);
        area.setRight(ordinePanel);

        VBox corpo = new VBox(10, aggiorna, messaggio,
                new Label("Catalogo prodotti"), area);
        corpo.setPadding(new Insets(16, 0, 0, 0));
        return UiKit.pagina("Marketplace locale", "UC-04 · ordina dalla vendita locale", corpo, "marketplace");
    }

    private void inizializzaControlli() {
        aggiorna = new Button("Aggiorna catalogo");
        aggiorna.setId("btn-catalogo");
        aggiorna.setMaxWidth(Double.MAX_VALUE);

        catalogo = new FlowPane(16, 16);
        catalogo.setPadding(new Insets(4, 0, 20, 0));

        prodSelez = new ComboBox<>();
        prodSelez.setPromptText("Scegli un prodotto");
        prodSelez.setMaxWidth(Double.MAX_VALUE);

        ordina = new Button("Ordina prodotto");
        ordina.setId("btn-ordina");
        ordina.setMaxWidth(Double.MAX_VALUE);

        messaggio = new Label("Carica il catalogo e scegli un prodotto.");
        messaggio.getStyleClass().add("page-subtitle");
        messaggio.setWrapText(true);

        buonoField = new TextField();
        buonoField.setId("buonoField");
        buonoField.setPromptText("Codice buono (opzionale)");
        buonoField.setMaxWidth(Double.MAX_VALUE);

        applicaBuono = new Button("Applica buono");
        applicaBuono.setId("btn-applica-buono");
        applicaBuono.setMaxWidth(Double.MAX_VALUE);
    }

    // -------- Gestori UI (stile on...) --------

    private void onAggiornaCatalogo() {
        List<ProdottoBean> prodotti = facade.getProdottiDisponibili();
        catalogo.getChildren().clear();
        prodotti.forEach(p -> catalogo.getChildren()
                .add(UiKit.card(p.getNome(),
                        String.format("%.2f EUR · %s disponibili",
                                p.getPrezzo(), p.getQuantita().intValue()))));
        prodSelez.getItems().setAll(prodotti.stream()
                .map(ProdottoBean::getNome).collect(Collectors.toList()));
        messaggio.setText(prodotti.size() + " prodotti disponibili nel marketplace locale.");
    }

    private void onOrdinaProdotto() {
        String nome = prodSelez.getValue();
        if (nome == null || nome.isBlank()) {
            messaggio.setText("Seleziona un prodotto dal catalogo.");
            return;
        }
        try {
            facade.avviaCheckout(nome);
            Navigator.getInstance().switchTo("payment");
        } catch (BusinessValidationException ex) {
            messaggio.setText(ex.getUserMessage());
        } catch (Exception ex) {
            messaggio.setText("Problema tecnico: riprovare più tardi.");
        }
    }

    private void onApplicaBuono() {
        try {
            OrdineBean ris = facade.applicaBuono(
                    buonoField.getText(), prodSelez.getValue(), utente);
            messaggio.setText("Buono \"" + ris.getCodiceBuono()
                    + "\" applicato ✓ — totale " + String.format("%.2f", ris.getTotale()) + " EUR");
        } catch (BusinessValidationException ex) {
            messaggio.setText(ex.getUserMessage());
        } catch (Exception ex) {
            messaggio.setText("Problema tecnico: verificare il codice buono.");
        }
    }
}
