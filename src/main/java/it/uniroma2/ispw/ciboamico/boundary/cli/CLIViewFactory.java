package it.uniroma2.ispw.ciboamico.boundary.cli;

import it.uniroma2.ispw.ciboamico.boundary.IView;
import it.uniroma2.ispw.ciboamico.boundary.ViewFactory;
import it.uniroma2.ispw.ciboamico.persistence.factory.DAOFactory;

// Abstract Factory concreta — famiglia CLI delle Boundary (scope

public class CLIViewFactory extends ViewFactory {

    private final CLIContext ctx;

    public CLIViewFactory(DAOFactory factory) {
        this.ctx = new CLIContext(factory);
    }

    @Override
    public IView createLoginView() {
        return new LoginCLIView(ctx);
    }

    @Override
    public IView createHomeView() {
        return new HomeCLIView(ctx, createMarketplaceView(), createPaymentView());
    }

    @Override
    public IView createMarketplaceView() {
        return new MarketplaceCLIView(ctx, createPaymentView());
    }

    @Override
    public IView createPaymentView() {
        return new PaymentCLIView(ctx);
    }
}