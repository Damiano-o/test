package it.uniroma2.ispw.ciboamico.boundary;

import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;

// Boundary JavaFX — Inventario (demo)

public class InventarioView {

    public static class VoceInventarioDemo {
        private final String nome;
        private final String quantita;
        private final String scadenza;

        public VoceInventarioDemo(String nome, String quantita, String scadenza) {
            this.nome = nome;
            this.quantita = quantita;
            this.scadenza = scadenza;
        }

        public String getNome() { return nome; }
        public String getQuantita() { return quantita; }
        public String getScadenza() { return scadenza; }
    }

    // Nota: PropertyValueFactory usa reflection sulla property

    @SuppressWarnings("unchecked")
    public Parent build() {
        TableView<VoceInventarioDemo> tabella = new TableView<>();

        TableColumn<VoceInventarioDemo, String> colNome = new TableColumn<>("Prodotto");
        colNome.setCellValueFactory(new PropertyValueFactory<>("nome"));
        colNome.setPrefWidth(200);

        TableColumn<VoceInventarioDemo, String> colQuant = new TableColumn<>("Quantità");
        colQuant.setCellValueFactory(new PropertyValueFactory<>("quantita"));
        colQuant.setPrefWidth(120);

        TableColumn<VoceInventarioDemo, String> colScad = new TableColumn<>("Scadenza");
        colScad.setCellValueFactory(new PropertyValueFactory<>("scadenza"));
        colScad.setPrefWidth(120);

        tabella.getColumns().addAll(colNome, colQuant, colScad);
        tabella.getItems().addAll(
                new VoceInventarioDemo("Miele locale", "1 vasetto", "20/02/2027"),
                new VoceInventarioDemo("Pomodori", "500 g", "21/08/2026"),
                new VoceInventarioDemo("Latte", "1 l", "12/08/2026"),
                new VoceInventarioDemo("Uova", "6 pezzi", "05/08/2026"));

        tabella.setPrefHeight(360);

        Label nota = new Label("Demo — dati di esempio. La gestione reale dell'inventario "
                + "è una funzionalità futura.");
        nota.getStyleClass().add("page-subtitle");
        nota.setWrapText(true);

        VBox corpo = new VBox(10, nota, tabella);
        return UiKit.pagina("Inventario", "Prodotti in dispensa", corpo, "inventario");
    }
}
