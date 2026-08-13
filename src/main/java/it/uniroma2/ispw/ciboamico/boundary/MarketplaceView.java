package it.uniroma2.ispw.ciboamico.boundary;

import it.uniroma2.ispw.ciboamico.bean.OrdineBean;
import it.uniroma2.ispw.ciboamico.bean.ProdottoBean;
import it.uniroma2.ispw.ciboamico.bean.UtenteBean;
import it.uniroma2.ispw.ciboamico.control.ui.MarketplaceUIController;
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

// Boundary JavaFX — Marketplace (UC-04 Ordina Prodotto)

public class MarketplaceView {

    private final MarketplaceUIController controller;

    // Controlli della vista
    private Button aggiorna;
    private Button ordina;
    private Button applicaBuono;
    private TextField buonoField;
    private ComboBox<String> prodSelez;
    private FlowPane catalogo;
    private Label messaggio;

    public MarketplaceView() {
        // L'utente va letto al momento di build(), non nel costruttore:
        // la view è istanziata all'avvio (prima del login) dal Navigator.
        this.controller = new MarketplaceUIController();
    }

    public Parent build() {
        UtenteBean utente = SessionManager.getInstance().getLoggedUser();
        // Guardia: il venditore non accede al marketplace (funzione cliente)
        if (utente == null) {
            Navigator.getInstance().switchTo("login");
            return new javafx.scene.layout.VBox();
        }
        if ("VENDITORE".equalsIgnoreCase(utente.getRuoloAttivo())) {
            Navigator.getInstance().switchTo("home");
            return new javafx.scene.layout.VBox();
        }
        // Aggiorna l'utente del controller di presentazione al login corrente.
        this.controller.setUtente(utente);
        inizializzaControlli();
        aggiorna.setOnAction(e -> onAggiornaCatalogo());
        ordina.setOnAction(e -> onOrdinaProdotto());
        applicaBuono.setOnAction(e -> onApplicaBuono());

        BorderPane area = new BorderPane();
        VBox ordinePanel = new VBox(8,
                UiKit.field("Prodotto da ordinare"), prodSelez,
                new Label("Buono promozionale (opzionale)"), buonoField, applicaBuono,
                ordina);
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

    // -------- Listener: rinvio al controller di presentazione

    private void onAggiornaCatalogo() {
        try {
            List<ProdottoBean> prodotti = controller.catalogoProdotti();
            catalogo.getChildren().clear();
            prodotti.forEach(p -> catalogo.getChildren()
                    .add(UiKit.card(p.getNome(),
                            String.format("%.2f EUR · %s disponibili",
                                    p.getPrezzo(), p.getQuantita().intValue()))));
            prodSelez.getItems().setAll(prodotti.stream()
                    .map(ProdottoBean::getNome).collect(Collectors.toList()));
            messaggio.setText(controller.aggiornaCatalogo());
        } catch (Exception ex) {
            messaggio.setText("Problema di accesso ai dati: riprovare più tardi.");
        }
    }

    private void onOrdinaProdotto() {
        try {
            controller.ordinaProdotto(prodSelez.getValue());
            Navigator.getInstance().switchTo("payment");
        } catch (BusinessValidationException ex) {
            messaggio.setText(ex.getUserMessage());
        } catch (Exception ex) {
            messaggio.setText("Problema tecnico: riprovare più tardi.");
        }
    }

    private void onApplicaBuono() {
        try {
            OrdineBean ris = controller.applicaBuono(
                    buonoField.getText(), prodSelez.getValue());
            messaggio.setText(MarketplaceUIController.formattaEsitoBuono(ris));
        } catch (BusinessValidationException ex) {
            messaggio.setText(ex.getUserMessage());
        } catch (Exception ex) {
            ex.printStackTrace();
            messaggio.setText("Problema tecnico: " + ex.getClass().getSimpleName());
        }
    }
}
