package it.uniroma2.ispw.ciboamico.boundary;

import it.uniroma2.ispw.ciboamico.bean.AutenticazioneBean;
import it.uniroma2.ispw.ciboamico.bean.UtenteBean;
import it.uniroma2.ispw.ciboamico.control.facade.AutenticazioneFacade;
import it.uniroma2.ispw.ciboamico.enums.UserErrorMessagesEnum;
import it.uniroma2.ispw.ciboamico.exception.AutenticazioneException;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

// Boundary JavaFX — Login (UC-11)

public class LoginView {

    private final AutenticazioneFacade facade;

    public LoginView() {
        this.facade = new AutenticazioneFacade();
    }

    public Parent build() {
        // Card contenitore (stile .login-card dal CSS)
        VBox card = new VBox();
        card.getStyleClass().add("login-card");

        // Brand
        Label logo = new Label("🍀");
        logo.getStyleClass().add("icon");
        logo.setStyle("-fx-font-size: 34px;");
        Label brand = new Label("CiboAmico");
        brand.getStyleClass().add("brand-title");
        Label tagline = new Label("Riduci lo spreco, scopri ricette");
        tagline.getStyleClass().add("page-subtitle");
        VBox brandBlock = new VBox(2, logo, brand, tagline);
        brandBlock.getStyleClass().add("login-brand");

        // Campi
        TextField email = new TextField();
        email.setPromptText("nome@email.it");
        PasswordField password = new PasswordField();
        password.setPromptText("password");
        Label messaggio = new Label();
        messaggio.getStyleClass().add("error-msg");
        messaggio.setVisible(false);
        messaggio.setManaged(false);

        Button login = new Button("Accedi");
        login.setId("btn-login");
        login.setMaxWidth(Double.MAX_VALUE);

        login.setOnAction(e -> onLogin(email, password, messaggio));

        card.getChildren().addAll(brandBlock, email, password, login, messaggio);

        // Sfondo: StackPane centra la card
        StackPane root = new StackPane();
        root.getStyleClass().add("login-screen");
        root.getChildren().add(card);
        root.setPrefSize(900, 640);
        return root;
    }

    // -------- Gestore UI (stile on...) --------

    private void onLogin(TextField email, PasswordField password, Label messaggio) {
        try {
            // Conversione esterno→interno a carico della view: le
            // stringhe del form diventano il bean credenziali passato al Facade.
            UtenteBean utente = facade.login(
                    AutenticazioneBean.fromCredenziali(email.getText(), password.getText()));
            messaggio.setText("Benvenuto, " + utente.getUsername() + "!");
            Navigator.getInstance().switchTo("home");
        } catch (AutenticazioneException ex) {
            messaggio.setText(UserErrorMessagesEnum.WRONG_PASSWORD_MSG.message);
            messaggio.setVisible(true);
            messaggio.setManaged(true);
        } catch (Exception ex) {
            messaggio.setText("Errore di sistema: riprovare più tardi.");
            messaggio.setVisible(true);
            messaggio.setManaged(true);
        }
    }
}
