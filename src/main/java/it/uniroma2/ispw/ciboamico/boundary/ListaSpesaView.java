package it.uniroma2.ispw.ciboamico.boundary;

import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

// Boundary JavaFX — Lista Spesa (demo)

public class ListaSpesaView {

    public Parent build() {
        VBox lista = new VBox(8);
        lista.setPadding(new Insets(8, 0, 0, 0));

        voceSpesa(lista, "Miele locale", "1 vasetto");
        voceSpesa(lista, "Pomodori", "500 g");
        voceSpesa(lista, "Latte", "1 l");
        voceSpesa(lista, "Uova", "6 pezzi");

        Label nota = new Label("Demo — dati di esempio. Spunta gli acquisti fatti.");
        nota.getStyleClass().add("page-subtitle");
        nota.setWrapText(true);

        VBox corpo = new VBox(10, nota, lista);
        return UiKit.pagina("Lista Spesa", "Cosa acquistare al mercato", corpo, "listaspesa");
    }

    private void voceSpesa(VBox lista, String nome, String dettaglio) {
        CheckBox cb = new CheckBox();
        cb.setId("cb-" + nome.toLowerCase().replace(" ", "-"));

        Label n = new Label(nome);
        n.getStyleClass().add("prodotto-nome");
        Label d = new Label(dettaglio);
        d.getStyleClass().add("prodotto-dett");
        Region spazio = new Region();
        HBox.setHgrow(spazio, Priority.ALWAYS);
        HBox testo = new HBox(8, n, spazio, d);
        testo.setMaxWidth(Double.MAX_VALUE);

        HBox riga = new HBox(10, cb, testo);
        riga.getStyleClass().add("prodotto-card");
        riga.setPadding(new Insets(10, 12, 10, 12));
        riga.setMaxWidth(Double.MAX_VALUE);
        lista.getChildren().add(riga);
    }
}
