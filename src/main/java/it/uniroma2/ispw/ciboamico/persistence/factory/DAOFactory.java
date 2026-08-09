package it.uniroma2.ispw.ciboamico.persistence.factory;

import it.uniroma2.ispw.ciboamico.persistence.dao.BuonoDAO;
import it.uniroma2.ispw.ciboamico.persistence.dao.OrdineDAO;
import it.uniroma2.ispw.ciboamico.persistence.dao.ProdottoDAO;
import it.uniroma2.ispw.ciboamico.persistence.dao.UtenteDAO;

/**
 * Abstract Factory (GoF): famiglia coerente di DAO.
 * Il contratto è esposto come interfaccia: le concrete factory (JDBC/FS/Demo)
 * sono scelte a runtime da {@link it.uniroma2.ispw.ciboamico.bootstrap.ApplicationModeManager}.
 */
public interface DAOFactory {

    UtenteDAO getUtenteDAO();
    ProdottoDAO getProdottoDAO();
    OrdineDAO getOrdineDAO();
    BuonoDAO getBuonoDAO();
}
