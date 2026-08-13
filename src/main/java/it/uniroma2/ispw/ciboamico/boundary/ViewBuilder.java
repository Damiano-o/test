package it.uniroma2.ispw.ciboamico.boundary;

import javafx.scene.Parent;

// Builder delle Boundary JavaFX — ogni vista costruisce il proprio Parent

@FunctionalInterface
public interface ViewBuilder {
    Parent build();
}
