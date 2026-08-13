package it.uniroma2.ispw.ciboamico.boundary;

import it.uniroma2.ispw.ciboamico.pattern.singleton.SessionManager;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * Componenti UI riutilizzabili del design system minimalista.
 * Sidebar di navigazione, header di pagina e card: unica fonte di un layout
 * coerente tra le Boundary JavaFX. Nessuna logica di business: si limita a
 * costruire nodi e gestire la navigazione via {@link Navigator}.
 */
public final class UiKit {

    private UiKit() {
        // utility di layout, non istanziabile
    }

    /** Root standard: sidebar a sinistra + corpo centrale. */
    public static BorderPane root(Node corpo) {
        BorderPane root = new BorderPane();
        root.setLeft(sidebar());
        root.setCenter(corpo);
        root.getStyleClass().add("home-root");
        root.setPrefSize(900, 640);
        return root;
    }

    /** Pagina con header (titolo+sottotitolo) e corpo scrollabile. */
    public static BorderPane pagina(String titolo, String sottotitolo, Node corpo, String attiva) {
        Label t = new Label(titolo);
        t.getStyleClass().add("screen-title");
        Label s = new Label(sottotitolo);
        s.getStyleClass().add("page-subtitle");
        VBox header = new VBox(2, t, s);
        header.setPadding(new Insets(16, 24, 0, 24));

        VBox center = new VBox(12, header, corpo);
        center.setPadding(new Insets(0, 24, 20, 24));

        BorderPane root = new BorderPane();
        root.setLeft(sidebar(attiva));
        root.setCenter(center);
        root.getStyleClass().add("home-root");
        root.setPrefSize(900, 640);
        return root;
    }

    /** Sidebar con voce Home attiva. */
    public static VBox sidebar() {
        return sidebar("home");
    }

    /** Pagina con header; la voce di sidebar evidenziata è {@code attiva}. */
    public static BorderPane pagina(String titolo, String sottotitolo, Node corpo) {
        return pagina(titolo, sottotitolo, corpo, "home");
    }

    /** Sidebar di navigazione con la voce {@code attiva}, adattata al ruolo loggato. */
    public static VBox sidebar(String attiva) {
        Label brand = new Label("CiboAmico");
        brand.getStyleClass().add("brand-title");

        VBox s = new VBox(2, brand);
        s.getStyleClass().add("sidebar");

        s.getChildren().add(nav("Dashboard", "home", attiva));
        s.getChildren().add(nav("Marketplace", "marketplace", attiva));
        s.getChildren().add(nav("Ricette", "ricette", attiva));
        s.getChildren().add(nav("Inventario", "inventario", attiva));
        s.getChildren().add(nav("Lista Spesa", "listaspesa", attiva));
        Button esci = new Button("Esci");
        esci.getStyleClass().add("button-outline");
        esci.setMaxWidth(Double.MAX_VALUE);
        esci.setOnAction(e -> {
            SessionManager.getInstance().logout();
            Navigator.getInstance().switchTo("login");
        });
        s.getChildren().add(esci);
        VBox.setMargin(esci, new Insets(14, 0, 0, 0));
        return s;
    }

    private static Button nav(String testo, String vista, String attiva) {
        Button b = new Button(testo);
        b.setMaxWidth(Double.MAX_VALUE);
        b.getStyleClass().add("nav-item");
        if (vista.equals(attiva)) {
            b.getStyleClass().add("active");
        }
        b.setOnAction(e -> Navigator.getInstance().switchTo(vista));
        return b;
    }

    /** Etichetta di sezione in un form. */
    public static Label field(String testo) {
        Label l = new Label(testo);
        l.getStyleClass().add("field-label");
        return l;
    }

    /** Card riutilizzabile: nome, dettaglio, badge colorato di stato. */
    public static VBox card(String nome, String dettaglio, String badge, String badgeClasse) {
        Label n = new Label(nome);
        n.getStyleClass().add("prodotto-nome");
        Label b = new Label(badge);
        b.getStyleClass().addAll("badge", badgeClasse);
        Region spazio = new Region();
        HBox.setHgrow(spazio, Priority.ALWAYS);
        HBox head = new HBox(8, n, spazio, b);
        Label d = new Label(dettaglio);
        d.getStyleClass().add("prodotto-dett");
        d.setWrapText(true);
        VBox card = new VBox(6, head, d);
        card.getStyleClass().add("prodotto-card");
        card.setPrefWidth(320);
        return card;
    }

    /** Card semplice (nome + dettaglio, senza badge). */
    public static VBox card(String nome, String dettaglio) {
        Label n = new Label(nome);
        n.getStyleClass().add("prodotto-nome");
        Label d = new Label(dettaglio);
        d.getStyleClass().add("prodotto-dett");
        d.setWrapText(true);
        VBox card = new VBox(6, n, d);
        card.getStyleClass().add("prodotto-card");
        card.setPrefWidth(320);
        return card;
    }
}