package it.uniroma2.ispw.ciboamico.boundary;

import it.uniroma2.ispw.ciboamico.bean.OrdineBean;
import it.uniroma2.ispw.ciboamico.exception.BusinessValidationException;
import it.uniroma2.ispw.ciboamico.bean.ProdottoBean;
import it.uniroma2.ispw.ciboamico.bean.UtenteBean;
import it.uniroma2.ispw.ciboamico.control.OrdinaProdottoController;
import it.uniroma2.ispw.ciboamico.pattern.singleton.SessionManager;
import it.uniroma2.ispw.ciboamico.persistence.factory.DAOFactory;
import it.uniroma2.ispw.ciboamico.bootstrap.ApplicationModeManager;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import java.util.List;

/**
 * Boundary JavaFX — Marketplace (UC-04 Ordina Prodotto).
 * Elenca i prodotti pubblicati e crea un ordine singolo diretto (D-03).
 * Scambia SOLO OrdineBean con il controller.
 */
public class MarketplaceView {

    private final OrdinaProdottoController ordinaController;

    public MarketplaceView() {
        DAOFactory factory = ApplicationModeManager.getInstance().getDAOFactory();
        this.ordinaController = new OrdinaProdottoController(factory);
    }

    public MarketplaceView(DAOFactory factory) {
        this.ordinaController = new OrdinaProdottoController(factory);
    }

    public Parent build() {
        UtenteBean utente = SessionManager.getInstance().getLoggedUser();
        Label titolo = new Label("Marketplace locale (UC-04)");
        ListView<String> elenco = new ListView<>();
        TextField nomeProdotto = new TextField();
        nomeProdotto.setPromptText("Nome prodotto da ordinare");
        Label messaggio = new Label();

        Button aggiorna = new Button("Aggiorna catalogo");
        aggiorna.setId("btn-catalogo");
        aggiorna.setOnAction(e -> {
            List<ProdottoBean> prodotti = ordinaController.getProdottiDisponibili();
            elenco.getItems().setAll(prodotti.stream()
                    .map(p -> p.getNome() + " — " + p.getPrezzo() + " EUR — "
                            + p.getQuantita() + " disponibili")
                    .toList());
        });

        Button ordina = new Button("Ordina");
        ordina.setId("btn-ordina");
        ordina.setOnAction(e -> {
            try {
                // id simbolico del prodotto = hash del nome (coerente con DAO demo)
                OrdineBean bean = new OrdineBean();
                bean.setIdOrdine((long) nomeProdotto.getText().hashCode());
                OrdineBean risultato = ordinaController.submitOrdine(bean, utente);
                messaggio.setText("Ordine creato ✓ — stato " + risultato.getStato()
                        + ", totale " + String.format("%.2f", risultato.getTotale()) + " EUR");
            } catch (BusinessValidationException ex) {
                messaggio.setText(ex.getMessage());
            } catch (Exception ex) {
                messaggio.setText("Problema tecnico: riprovare più tardi.");
            }
        });

        Button indietro = new Button("← Home");
        indietro.setOnAction(e -> Navigator.getInstance().switchTo("home"));

        VBox root = new VBox(10, titolo, aggiorna, elenco,
                nomeProdotto, ordina, messaggio, indietro);
        root.setPrefSize(900, 640);
        return root;
    }
}
