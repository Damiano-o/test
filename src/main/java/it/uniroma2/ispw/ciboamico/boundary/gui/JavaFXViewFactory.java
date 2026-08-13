package it.uniroma2.ispw.ciboamico.boundary.gui;

import it.uniroma2.ispw.ciboamico.boundary.IView;
import it.uniroma2.ispw.ciboamico.boundary.Navigator;
import it.uniroma2.ispw.ciboamico.boundary.ViewFactory;

// Abstract Factory concreta — famiglia GUI (JavaFX) delle Boundary

public class JavaFXViewFactory extends ViewFactory {

    @Override
    public IView createLoginView() {
        return () -> Navigator.getInstance().switchTo("login");
    }

    @Override
    public IView createHomeView() {
        return () -> Navigator.getInstance().switchTo("home");
    }

    @Override
    public IView createMarketplaceView() {
        return () -> Navigator.getInstance().switchTo("marketplace");
    }

    @Override
    public IView createPaymentView() {
        return () -> Navigator.getInstance().switchTo("payment");
    }
}
