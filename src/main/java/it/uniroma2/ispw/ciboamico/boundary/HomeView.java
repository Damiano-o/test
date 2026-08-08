package it.uniroma2.ispw.ciboamico.boundary;

import it.uniroma2.ispw.ciboamico.bean.UtenteBean;
import it.uniroma2.ispw.ciboamico.pattern.singleton.SessionManager;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

/**
 * Boundary JavaFX — Home (atterraggio post-login), scope UC-04.
 * Centralizza l'accesso al Marketplace per la navigazione verso
 * "Ordina un Prodotto" e l'uscita. Sidebar minima: Dashboard e Marketplace.
 */
public class HomeView {

    public Parent build() {
        UtenteBean utente = SessionManager.getInstance().getLoggedUser();

        Label brand = new Label("CiboAmico");
        brand.getStyleClass().add("brand-title");

        // Sidebar: Dashboard + Marketplace + Esci
        Button itemMarket = new Button("Marketplace");
        itemMarket.setMaxWidth(Double.MAX_VALUE);
        itemMarket.getStyleClass().add("nav-item");
        itemMarket.setOnAction(e -> Navigator.getInstance().switchTo("marketplace"));

        Button esci = new Button("Esci");
        esci.getStyleClass().add("button-outline");
        esci.setMaxWidth(Double.MAX_VALUE);
        esci.setOnAction(e -> {
            SessionManager.getInstance().logout();
            Navigator.getInstance().switchTo("login");
        });

        VBox sidebar = new VBox(2, brand, itemMarket, esci);
        sidebar.getStyleClass().add("sidebar");
        VBox.setMargin(esci, new Insets(14, 0, 0, 0));

        // Corpo principale: benvenuto + card Marketplace
        Label titolo = new Label("Dashboard");
        titolo.getStyleClass().add("screen-title");
        Label sottotitolo = new Label("Riduci lo spreco alimentare. Scegli cosa fare.");
        sottotitolo.getStyleClass().add("page-subtitle");

        Button marketplace = new Button("Ordina dalla vendita locale");
        marketplace.setMaxWidth(280);
        marketplace.getStyleClass().add("card");
        marketplace.setOnAction(e -> Navigator.getInstance().switchTo("marketplace"));

        VBox main = new VBox(12, titolo, sottotitolo, marketplace);
        main.setPadding(new Insets(20, 24, 20, 24));

        BorderPane root = new BorderPane();
        root.setLeft(sidebar);
        root.setCenter(main);
        root.getStyleClass().add("home-root");
        root.setPrefSize(900, 640);
        return root;
    }
}