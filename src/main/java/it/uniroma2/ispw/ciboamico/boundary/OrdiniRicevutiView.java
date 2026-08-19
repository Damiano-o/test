package it.uniroma2.ispw.ciboamico.boundary;

import it.uniroma2.ispw.ciboamico.bean.OrdineBean;
import it.uniroma2.ispw.ciboamico.exception.InvalidStateTransitionException;
import it.uniroma2.ispw.ciboamico.bean.UtenteBean;
import it.uniroma2.ispw.ciboamico.control.GestisciOrdiniRicevutiController;
import it.uniroma2.ispw.ciboamico.pattern.singleton.SessionManager;
import it.uniroma2.ispw.ciboamico.persistence.factory.DAOFactory;
import it.uniroma2.ispw.ciboamico.bootstrap.ApplicationModeManager;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.util.List;

/**
 * Boundary JavaFX — Ordini Ricevuti (UC-06).
 * Il venditore visualizza e aggiorna lo stato degli ordini (BR-04).
 * Scambia SOLO OrdineBean con il controller.
 */
public class OrdiniRicevutiView {

    private final GestisciOrdiniRicevutiController controller;

    public OrdiniRicevutiView() {
        this.controller = new GestisciOrdiniRicevutiController(
                ApplicationModeManager.getInstance().getDAOFactory());
    }

    public OrdiniRicevutiView(DAOFactory factory) {
        this.controller = new GestisciOrdiniRicevutiController(factory);
    }

    public Parent build() {
        UtenteBean utente = SessionManager.getInstance().getLoggedUser();
        ListView<String> elenco = new ListView<>();
        TextField idOrdine = new TextField();
        idOrdine.setPromptText("ID ordine");
        ComboBox<String> nuovoStato = new ComboBox<>();
        nuovoStato.getItems().addAll(controller.getStatiAggiornabili());
        nuovoStato.setValue("CONFIRMED");
        Label messaggio = new Label();

        Button aggiorna = new Button("Visualizza ordini");
        aggiorna.setId("btn-ordini");
        aggiorna.setOnAction(e -> {
            List<OrdineBean> ordini = controller.visualizzaOrdiniRicevuti(utente.getEmail());
            elenco.getItems().setAll(ordini.stream()
                    .map(o -> "Ordine " + o.getIdOrdine() + " — " + o.getStato()
                            + " — " + String.format("%.2f", o.getTotale()) + " EUR")
                    .toList());
        });

        Button cambia = new Button("Aggiorna stato");
        cambia.setOnAction(e -> {
            try {
                OrdineBean o = controller.aggiornaStato(
                        Long.parseLong(idOrdine.getText()),
                        nuovoStato.getValue());
                messaggio.setText("Stato aggiornato → " + o.getStato());
                aggiorna.fire();
            } catch (InvalidStateTransitionException ex) {
                messaggio.setText(ex.getMessage());
            } catch (Exception ex) {
                messaggio.setText("Problema tecnico: riprovare più tardi.");
            }
        });

        Button indietro = new Button("← Home");
        indietro.setOnAction(e -> Navigator.getInstance().switchTo("home"));

        VBox root = new VBox(10, new Label("Ordini ricevuti (UC-06)"),
                aggiorna, elenco, new Label("Aggiorna stato:"), idOrdine,
                nuovoStato, cambia, messaggio, indietro);
        root.setPrefSize(900, 640);
        return root;
    }
}
