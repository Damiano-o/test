package it.uniroma2.ispw.ciboamico.boundary;

import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;

// Boundary JavaFX — Ricette (demo)

public class RicetteView {

    public Parent build() {
        // Card ricette demo (UI pura, nessuna logica)
        FlowPane card = new FlowPane(16, 16);
        card.setPadding(new Insets(8, 0, 0, 0));
        card.getChildren().addAll(
                cardRicetta("Uova strapazzate al latte",
                        "Ingredienti: uova, latte. ~8 min.",
                        "100% ingredienti in dispensa"),
                cardRicetta("Frittata al pomodoro",
                        "Ingredienti: uova, pomodori. ~15 min.",
                        "100% ingredienti in dispensa"),
                cardRicetta("Pomodori al forno con miele",
                        "Ingredienti: pomodori, miele locale. ~20 min.",
                        "100% ingredienti in dispensa"),
                cardRicetta("Latte e miele caldo",
                        "Ingredienti: latte, miele locale. ~5 min.",
                        "100% ingredienti in dispensa"));

        VBox corpo = new VBox(10, card);
        return UiKit.pagina("Ricette", "Piatti compatibili con la tua dispensa (demo)",
                corpo, "ricette");
    }

    private VBox cardRicetta(String titolo, String dettaglio, String badge) {
        Label n = new Label(titolo);
        n.getStyleClass().add("prodotto-nome");
        Label b = new Label(badge);
        b.getStyleClass().addAll("badge", "badge-ok");
        Label d = new Label(dettaglio);
        d.getStyleClass().add("prodotto-dett");
        d.setWrapText(true);
        VBox v = new VBox(6, n, b, d);
        v.getStyleClass().add("prodotto-card");
        v.setPrefWidth(400);
        return v;
    }
}
