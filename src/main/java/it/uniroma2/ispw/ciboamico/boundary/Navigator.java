package it.uniroma2.ispw.ciboamico.boundary;

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.HashMap;
import java.util.Map;

// Singleton: isola lo Stage JavaFX e gestisce il cambio schermata

public final class Navigator {

    private static Navigator instance;
    private Stage stage;
    private final Map<String, ViewBuilder> registry = new HashMap<>();

    private Navigator() { }

    public static synchronized Navigator getInstance() {
        if (instance == null) {
            instance = new Navigator();
        }
        return instance;
    }

    public void init(Stage stage) {
        this.stage = stage;
    }

    public void register(String viewName, ViewBuilder factory) {
        registry.put(viewName, factory);
    }

    public void switchTo(String viewName) {
        ViewBuilder factory = registry.get(viewName);
        if (factory == null) {
            throw new IllegalArgumentException("View non registrata: " + viewName);
        }
        Parent root = factory.build();
        if (stage.getScene() == null) {
            Scene scene = new Scene(root, 900, 640);
            scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
            stage.setScene(scene);
        } else {
            stage.getScene().setRoot(root);
        }
        stage.show();
    }

    public Scene getScene() {
        return stage.getScene();
    }
}
