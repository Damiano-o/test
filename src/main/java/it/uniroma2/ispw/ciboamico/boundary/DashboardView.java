package it.uniroma2.ispw.ciboamico.boundary;

import it.uniroma2.ispw.ciboamico.bean.UtenteBean;
import it.uniroma2.ispw.ciboamico.pattern.observer.OrdineEvent;
import it.uniroma2.ispw.ciboamico.pattern.singleton.SessionManager;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.List;

// Boundary JavaFX — Dashboard (home)

public class DashboardView {

    public Parent build() {
        UtenteBean utente = SessionManager.getInstance().getLoggedUser();
        String nome = utente != null ? utente.getUsername() : "ospite";
        String ruolo = utente != null && utente.getRuoloAttivo() != null
                ? utente.getRuoloAttivo() : "—";
        boolean venditore = "VENDITORE".equalsIgnoreCase(ruolo);

        // Avatar circolare con iniziale
        Label avatar = new Label(nome.isEmpty() ? "?" : nome.substring(0, 1).toUpperCase());
        avatar.getStyleClass().add("avatar");
        Label benvenuto = new Label("Benvenuto, " + nome);
        benvenuto.getStyleClass().add("screen-title");
        Label ruoloLabel = new Label("Ruolo attivo: " + ruolo);
        ruoloLabel.getStyleClass().add("page-subtitle");

        HBox header = new HBox(14, avatar,
                new VBox(2, benvenuto, ruoloLabel));
        header.setAlignment(Pos.CENTER_LEFT);

        // Corpo diverso in base al ruolo
        // approvato non opera come cliente: vede il proprio pannello, non
        // schede di acquisto lato cliente.
        VBox corpo;
        if (venditore) {
            corpo = corpoVenditore(header);
        } else {
            corpo = corpoCliente(header);
        }

        return UiKit.pagina("Dashboard", "Cosa vuoi fare?", corpo, "home");
    }

    private VBox corpoCliente(HBox header) {
        HBox kpiRow = new HBox(14,
                kpi("🍅 Spreco evitato", "12,4 kg"),
                kpi("⏳ In scadenza", "3 prodotti"),
                kpi("🛒 Spesa al mese", "236 €"));
        kpiRow.setPadding(new Insets(14, 0, 6, 0));

        VBox menu = new VBox(12,
                card("🍽️", "Trova ricette", "Ricette compatibili con la dispensa", "ricette"),
                card("📦", "Inventario", "Gestisci i prodotti e le scadenze", "inventario"),
                card("🛍️", "Lista spesa", "Crea la lista dagli ingredienti", "listaspesa"),
                card("🏪", "Marketplace", "Ordina dalla vendita locale", "marketplace"));

        Button esci = new Button("Esci");
        esci.getStyleClass().add("button-outline");
        esci.setMaxWidth(Double.MAX_VALUE);
        esci.setOnAction(e -> {
            SessionManager.getInstance().logout();
            Navigator.getInstance().switchTo("login");
        });

        VBox corpo = new VBox(12, header, kpiRow, menu, esci);
        corpo.setPadding(new Insets(20, 24, 20, 24));
        return corpo;
    }

    private VBox corpoVenditore(HBox header) {
        Label nota = new Label("Sei un venditore approvato: i tuoi prodotti sono "
                + "pubblicati nel marketplace locale e puoi ricevere ordini dai clienti.");
        nota.getStyleClass().add("page-subtitle");
        nota.setWrapText(true);

        // Elenco ordini ricevuti, auto-aggiornante (Observer: push, non
        Label titoloOrdini = new Label("Ordini ricevuti");
        titoloOrdini.getStyleClass().add("field-label");
        VBox elencoOrdini = new VBox(6);
        elencoOrdini.getStyleClass().add("form-panel");
        elencoOrdini.setPadding(new Insets(10, 12, 10, 12));

        String venditoreEmail = SessionManager.getInstance().getLoggedUser() != null
                ? SessionManager.getInstance().getLoggedUser().getEmail() : null;

        // Popolamento iniziale + aggiornamento automatico quando arriva un
        // ordine: la notifica attiva (Observer) aggiorna la vista da sola.
        Runnable refresh = () -> {
            List<OrdineEvent> ricevuti = OrdiniRicevutiStore.getInstance()
                    .getOrdiniPerVenditore(venditoreEmail);
            elencoOrdini.getChildren().clear();
            if (ricevuti.isEmpty()) {
                Label vuoto = new Label("Nessun ordine ricevuto finora.");
                vuoto.getStyleClass().add("page-subtitle");
                elencoOrdini.getChildren().add(vuoto);
            } else {
                for (OrdineEvent o : ricevuti) {
                    Label riga = new Label(String.format(
                            "Ordine #%d · cliente %s · %.2f EUR",
                            o.getNumeroOrdine(), o.getClienteId(), o.getTotale()));
                    riga.getStyleClass().add("prodotto-dett");
                    elencoOrdini.getChildren().add(riga);
                }
            }
        };
        refresh.run();
        OrdiniRicevutiStore.getInstance().addOrdineArrivatoListener(
                ev -> Platform.runLater(refresh));

        Button esci = new Button("Esci");
        esci.getStyleClass().add("button-outline");
        esci.setMaxWidth(Double.MAX_VALUE);
        esci.setOnAction(e -> {
            SessionManager.getInstance().logout();
            Navigator.getInstance().switchTo("login");
        });

        VBox corpo = new VBox(12, header, nota, titoloOrdini, elencoOrdini, esci);
        corpo.setPadding(new Insets(20, 24, 20, 24));
        return corpo;
    }

    private VBox kpi(String testo, String valore) {
        Label v = new Label(valore);
        v.getStyleClass().add("kpi-value");
        Label t = new Label(testo);
        t.getStyleClass().add("card-subtitle");
        VBox box = new VBox(4, v, t);
        box.getStyleClass().add("card");
        box.setPrefWidth(180);
        box.setAlignment(Pos.CENTER);
        return box;
    }

    private Button card(String icona, String titolo, String sottotitolo, String vista) {
        Label ic = new Label(icona);
        ic.getStyleClass().add("icon");
        Label ti = new Label(titolo);
        ti.getStyleClass().add("card-title");
        Label su = new Label(sottotitolo);
        su.getStyleClass().add("card-subtitle");
        Region spazio = new Region();
        HBox.setHgrow(spazio, Priority.ALWAYS);
        Label freccia = new Label("→");
        freccia.getStyleClass().add("card-arrow");

        HBox testo = new HBox(12, new VBox(2, ti, su), spazio, freccia);
        testo.setAlignment(Pos.CENTER_LEFT);

        HBox content = new HBox(14, ic, testo);
        content.setAlignment(Pos.CENTER_LEFT);
        content.setMaxWidth(Double.MAX_VALUE);

        Button b = new Button();
        b.setGraphic(content);
        b.setMaxWidth(Double.MAX_VALUE);
        b.getStyleClass().add("card");
        b.setOnAction(e -> Navigator.getInstance().switchTo(vista));
        return b;
    }
}
